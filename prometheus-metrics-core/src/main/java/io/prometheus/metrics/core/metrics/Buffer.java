package io.prometheus.metrics.core.metrics;

import static java.util.Objects.requireNonNull;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Coordinates concurrent metric observations with collection.
 *
 * <p>Collection activates a generation. Observations that start after activation are appended to
 * that generation while the collector waits for observations from the previous phase to finish. The
 * collector then creates a snapshot, deactivates the generation, and replays its buffered
 * observations into the live metric state.
 *
 * <p>The default collection wait is five seconds. A generation is capped at one million buffered
 * entries (about eight MiB of double storage, plus eight MiB of weights once a batched observation
 * has been buffered) to keep a stalled collection from growing without bound; the cap applies
 * backpressure rather than dropping observations.
 *
 * <p>Batched observations ({@code weight} identical values recorded as one operation) are tracked
 * with the same ticket protocol: one atomic add claims the whole ticket range of the batch, so a
 * batch is either entirely inside a collection's expected count or entirely outside it.
 */
class Buffer {
  private static final long BUFFER_ACTIVE_BIT = 1L << 63;
  private static final double[] EMPTY_BUFFER = new double[0];

  // Keep collection bounded without failing healthy scrapes during short periods of scheduler or
  // CI-host contention. The one-million-observation cap uses at most 8 MiB for one generation;
  // it is deliberately an internal safeguard rather than a data-loss policy.
  private static final long DEFAULT_MAX_SPIN_WAIT_NANOS = TimeUnit.SECONDS.toNanos(5);
  private static final int DEFAULT_MAX_BUFFER_SIZE = 1_000_000;
  private static final int INITIAL_BUFFER_SIZE = 128;

  /** Observations buffered during one collection cycle. */
  private static final class Generation {
    private double[] values = EMPTY_BUFFER;
    // Multiplicity of each buffered value. Allocated on the first weighted append only; null means
    // every buffered value has weight 1, which keeps single observations free of the extra array.
    @Nullable private long[] weights;
    private int size;
    private boolean active = true;
  }

  /** Replays one buffered entry, {@code value} observed {@code weight} times, into the metric. */
  interface WeightedObserver {
    void observe(double value, long weight);
  }

  // Tracking observation counts requires an AtomicLong for coordination between recording and
  // collecting. AtomicLong does much worse under contention than the LongAdder instances used
  // elsewhere to hold aggregated state. To reduce contention, the count is striped across the
  // available processors. This is simpler than the striping used by LongAdder, so hot spots remain
  // possible when several recording threads resolve to the same stripe.
  private final AtomicLong[] stripedObservationCounts;
  // Protected by appendLock. These are absolute per-stripe observation counts at activation, not
  // the reset-adjusted count used by complete. Reused across generations to avoid scrape
  // allocations.
  private final long[] generationStartCounts;
  private final ReentrantLock observationLock = new ReentrantLock();
  private boolean reset;
  private long observationCountOffset;
  @Nullable private volatile Generation activeGeneration;
  ReentrantLock appendLock = new ReentrantLock();
  ReentrantLock runLock = new ReentrantLock();
  private final Condition bufferSpaceAvailable = appendLock.newCondition();
  private final long maxSpinWaitNanos;
  private final int maxBufferSize;
  // These hooks are test seams only; production buffers use no-op callbacks.
  private final Runnable beforeGenerationRead;
  private final Runnable afterGenerationRead;

  Buffer() {
    this(DEFAULT_MAX_SPIN_WAIT_NANOS, DEFAULT_MAX_BUFFER_SIZE, () -> {}, () -> {});
  }

  Buffer(long maxSpinWaitNanos) {
    this(maxSpinWaitNanos, DEFAULT_MAX_BUFFER_SIZE, () -> {}, () -> {});
  }

  Buffer(long maxSpinWaitNanos, int maxBufferSize, Runnable beforeGenerationRead) {
    this(maxSpinWaitNanos, maxBufferSize, beforeGenerationRead, () -> {});
  }

  Buffer(
      long maxSpinWaitNanos,
      int maxBufferSize,
      Runnable beforeGenerationRead,
      Runnable afterGenerationRead) {
    if (maxBufferSize <= 0) {
      throw new IllegalArgumentException("maxBufferSize must be positive");
    }
    this.maxSpinWaitNanos = maxSpinWaitNanos;
    this.maxBufferSize = maxBufferSize;
    this.beforeGenerationRead = beforeGenerationRead;
    this.afterGenerationRead = afterGenerationRead;
    stripedObservationCounts = new AtomicLong[Runtime.getRuntime().availableProcessors()];
    generationStartCounts = new long[stripedObservationCounts.length];
    for (int i = 0; i < stripedObservationCounts.length; i++) {
      stripedObservationCounts[i] = new AtomicLong();
    }
  }

  boolean append(double value) {
    // Keep the uncontended hot path small enough for the JIT to inline into observations.
    int stripe = stripeIndex(Thread.currentThread().getId(), stripedObservationCounts.length);
    AtomicLong counter = stripedObservationCounts[stripe];
    long count = counter.incrementAndGet();
    // The active bit is the exact handoff decision. An observation either increments its stripe
    // before the collector's getAndAdd(BUFFER_ACTIVE_BIT) and takes the direct path, or sees the
    // active bit and may be buffered. The stripe ticket below also checks that it was not counted
    // by a later collection that started before this thread read activeGeneration.
    if ((count & BUFFER_ACTIVE_BIT) == 0) {
      return false;
    }
    return appendToActiveGeneration(value, 1L, stripe, count);
  }

  /**
   * Like {@link #append(double)}, for {@code weight} identical observations of {@code value}
   * recorded as one operation.
   *
   * <p>The batch claims its ticket range {@code (count - weight, count]} with a single atomic add,
   * so it cannot straddle a collector's activation: either all of its tickets predate the
   * activation and the batch is included in that collection's expected count (direct path), or none
   * do and the batch is buffered for replay after the snapshot.
   */
  boolean append(double value, long weight) {
    int stripe = stripeIndex(Thread.currentThread().getId(), stripedObservationCounts.length);
    AtomicLong counter = stripedObservationCounts[stripe];
    long count = counter.addAndGet(weight);
    if ((count & BUFFER_ACTIVE_BIT) == 0) {
      return false;
    }
    return appendToActiveGeneration(value, weight, stripe, count);
  }

  private boolean appendToActiveGeneration(double value, long weight, int stripe, long count) {
    // Allow tests to pause between allocating an observation ticket and reading the generation.
    beforeGenerationRead.run();
    Generation generation = activeGeneration;
    afterGenerationRead.run();
    if (generation == null) {
      return false;
    }
    appendLock.lock();
    try {
      Generation current = activeGeneration;
      if (current != generation || !generation.active) {
        return false;
      }
      if ((count & ~BUFFER_ACTIVE_BIT) - weight < generationStartCounts[stripe]) {
        // This observation claimed its tickets in an earlier generation (for weight 1 this is the
        // familiar count <= generationStartCounts[stripe]). The current collector already includes
        // it in expectedCount, so buffering it here would make the collector wait for an
        // observation that is only replayed after that same wait finishes.
        return false;
      }
      while (generation.size >= maxBufferSize && generation.active) {
        try {
          bufferSpaceAvailable.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          return false;
        }
      }
      if (!generation.active) {
        return false;
      }
      if (generation.size >= generation.values.length) {
        int doubled =
            generation.values.length > maxBufferSize / 2
                ? maxBufferSize
                : generation.values.length * 2;
        int newLength = Math.min(maxBufferSize, Math.max(INITIAL_BUFFER_SIZE, doubled));
        generation.values = Arrays.copyOf(generation.values, newLength);
        if (generation.weights != null) {
          generation.weights = Arrays.copyOf(generation.weights, newLength);
        }
      }
      if (weight != 1L && generation.weights == null) {
        generation.weights = new long[generation.values.length];
        Arrays.fill(generation.weights, 0, generation.size, 1L);
      }
      if (generation.weights != null) {
        generation.weights[generation.size] = weight;
      }
      generation.values[generation.size++] = value;
      return true;
    } finally {
      appendLock.unlock();
    }
  }

  static int stripeIndex(long threadId, int stripeCount) {
    return (int) Math.floorMod(threadId, stripeCount);
  }

  void reset() {
    reset = true;
  }

  <T> T observeDirect(Supplier<T> observeFunction) {
    // In steady state this is the lock-free path used before this buffer was introduced. Keep the
    // lock only while a generation is active, so direct observations cannot race collection/replay.
    if (activeGeneration == null) {
      return observeFunction.get();
    }
    observationLock.lock();
    try {
      return observeFunction.get();
    } finally {
      observationLock.unlock();
    }
  }

  @SuppressWarnings("ThreadPriorityCheck")
  <T extends DataPointSnapshot> T run(
      Function<Long, Boolean> complete,
      Supplier<T> createResult,
      WeightedObserver observeFunction) {
    return requireNonNull(run(complete, createResult, observeFunction, true));
  }

  @SuppressWarnings("ThreadPriorityCheck")
  @Nullable
  <T extends DataPointSnapshot> T run(
      Function<Long, Boolean> complete,
      Supplier<T> createResult,
      WeightedObserver observeFunction,
      boolean failOnTimeout) {
    Generation generation = new Generation();
    double[] buffer;
    long[] weights;
    int bufferSize;
    boolean timedOut = false;
    T result = null;
    runLock.lock();
    try {
      long expectedCount;
      appendLock.lock();
      try {
        activeGeneration = generation;
        long total = 0;
        for (int i = 0; i < stripedObservationCounts.length; i++) {
          long count = stripedObservationCounts[i].getAndAdd(BUFFER_ACTIVE_BIT);
          generationStartCounts[i] = count & ~BUFFER_ACTIVE_BIT;
          total += count;
        }
        expectedCount = total - observationCountOffset;
      } finally {
        appendLock.unlock();
      }
      long deadline = System.nanoTime() + maxSpinWaitNanos;
      while (!complete.apply(expectedCount)) {
        if (System.nanoTime() - deadline >= 0) {
          timedOut = true;
          break;
        }
        Thread.yield();
      }
      observationLock.lock();
      try {
        result = timedOut ? null : createResult.get();
      } finally {
        try {
          appendLock.lock();
          try {
            generation.active = false;
            for (AtomicLong counter : stripedObservationCounts) {
              counter.addAndGet(BUFFER_ACTIVE_BIT);
            }
            if (reset) {
              observationCountOffset += expectedCount;
              reset = false;
            }
            buffer = generation.values;
            weights = generation.weights;
            bufferSize = generation.size;
            generation.values = EMPTY_BUFFER;
            generation.weights = null;
            generation.size = 0;
            bufferSpaceAvailable.signalAll();
          } finally {
            appendLock.unlock();
          }
          for (int i = 0; i < bufferSize; i++) {
            observeFunction.observe(buffer[i], weights == null ? 1L : weights[i]);
          }
          // Keep the inactive generation visible until replay completes. An appender that loses the
          // generation race must take observationLock before observing directly.
          activeGeneration = null;
        } finally {
          observationLock.unlock();
        }
      }
      if (timedOut && failOnTimeout) {
        throw new IllegalStateException("Timed out while waiting for in-flight observations.");
      }
      return result;
    } finally {
      runLock.unlock();
    }
  }
}
