package io.prometheus.metrics.core.metrics;

import static java.util.Objects.requireNonNull;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
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
 * observations (about eight MiB of double storage) to keep a stalled collection from growing
 * without bound; the cap applies backpressure rather than dropping observations.
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
    private int size;
    private boolean active = true;
  }

  // Tracking observation counts requires an AtomicLong for coordination between recording and
  // collecting. AtomicLong does much worse under contention than the LongAdder instances used
  // elsewhere to hold aggregated state. To reduce contention, the count is striped across the
  // available processors. This is simpler than the striping used by LongAdder, so hot spots remain
  // possible when several recording threads resolve to the same stripe.
  private final AtomicLong[] stripedObservationCounts;
  // phaseTransition() waits for appenders that are between entering append() and updating their
  // stripe. This closes the handoff window around the collector's getAndAdd(BUFFER_ACTIVE_BIT).
  private final AtomicLong appendersInFlight = new AtomicLong();
  private final ReentrantLock observationLock = new ReentrantLock();
  private boolean reset;
  private long observationCountOffset;
  @Nullable private volatile Generation activeGeneration;
  ReentrantLock appendLock = new ReentrantLock();
  ReentrantLock runLock = new ReentrantLock();
  private final Condition bufferSpaceAvailable = appendLock.newCondition();
  private final long maxSpinWaitNanos;
  private final int maxBufferSize;
  private final Runnable beforeAppendLock;

  Buffer() {
    this(DEFAULT_MAX_SPIN_WAIT_NANOS, DEFAULT_MAX_BUFFER_SIZE, () -> {});
  }

  Buffer(long maxSpinWaitNanos) {
    this(maxSpinWaitNanos, DEFAULT_MAX_BUFFER_SIZE, () -> {});
  }

  Buffer(long maxSpinWaitNanos, int maxBufferSize, Runnable beforeAppendLock) {
    if (maxBufferSize <= 0) {
      throw new IllegalArgumentException("maxBufferSize must be positive");
    }
    this.maxSpinWaitNanos = maxSpinWaitNanos;
    this.maxBufferSize = maxBufferSize;
    this.beforeAppendLock = beforeAppendLock;
    stripedObservationCounts = new AtomicLong[Runtime.getRuntime().availableProcessors()];
    for (int i = 0; i < stripedObservationCounts.length; i++) {
      stripedObservationCounts[i] = new AtomicLong();
    }
  }

  boolean append(double value) {
    AtomicLong counter =
        stripedObservationCounts[
            stripeIndex(Thread.currentThread().getId(), stripedObservationCounts.length)];
    appendersInFlight.incrementAndGet();
    long count;
    try {
      count = counter.incrementAndGet();
    } finally {
      appendersInFlight.decrementAndGet();
    }
    // The active bit is the exact handoff decision. In particular, do not use the phase transition
    // as an additional gate: an observation that increments its stripe in that window must either
    // be included in expectedCount or be buffered in the current generation.
    if ((count & BUFFER_ACTIVE_BIT) == 0) {
      return false;
    }
    Generation generation = activeGeneration;
    if (generation == null) {
      return false;
    }
    beforeAppendLock.run();
    appendLock.lock();
    try {
      Generation current = activeGeneration;
      if (current != generation || !generation.active) {
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
        generation.values =
            Arrays.copyOf(
                generation.values, Math.min(maxBufferSize, Math.max(INITIAL_BUFFER_SIZE, doubled)));
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
      Consumer<Double> observeFunction) {
    return requireNonNull(run(complete, createResult, observeFunction, true));
  }

  @SuppressWarnings("ThreadPriorityCheck")
  @Nullable
  <T extends DataPointSnapshot> T run(
      Function<Long, Boolean> complete,
      Supplier<T> createResult,
      Consumer<Double> observeFunction,
      boolean failOnTimeout) {
    Generation generation = new Generation();
    double[] buffer;
    int bufferSize;
    boolean timedOut = false;
    T result = null;
    runLock.lock();
    try {
      phaseTransition();
      long expectedCount;
      appendLock.lock();
      try {
        activeGeneration = generation;
        long total = 0;
        for (AtomicLong counter : stripedObservationCounts) {
          total += counter.getAndAdd(BUFFER_ACTIVE_BIT);
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
          phaseTransition();
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
            bufferSize = generation.size;
            generation.values = EMPTY_BUFFER;
            generation.size = 0;
            bufferSpaceAvailable.signalAll();
          } finally {
            appendLock.unlock();
          }
          for (int i = 0; i < bufferSize; i++) {
            observeFunction.accept(buffer[i]);
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

  @SuppressWarnings("ThreadPriorityCheck")
  private void phaseTransition() {
    long deadline = System.nanoTime() + maxSpinWaitNanos;
    while (appendersInFlight.get() != 0) {
      if (System.nanoTime() - deadline >= 0) {
        // A late appender uses the active-bit decision and generation identity, so proceeding is
        // safe even if a suspended appender did not reach its stripe in time.
        return;
      }
      Thread.yield();
    }
  }
}
