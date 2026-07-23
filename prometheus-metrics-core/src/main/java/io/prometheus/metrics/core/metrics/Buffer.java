package io.prometheus.metrics.core.metrics;

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

/** Metrics support concurrent write and scrape operations. */
class Buffer {
  private static final long bufferActiveBit = 1L << 63;
  private static final long DEFAULT_MAX_SPIN_WAIT_NANOS = TimeUnit.SECONDS.toNanos(1);
  private static final int DEFAULT_MAX_BUFFER_SIZE = 1_000_000;
  private static final int INITIAL_BUFFER_SIZE = 128;

  private static final class Generation {
    private double[] values = new double[0];
    private int size;
    private boolean active = true;
  }

  private final AtomicLong[] stripedObservationCounts;
  private final AtomicLong appendersInFlight = new AtomicLong();
  private final AtomicLong appendPhase = new AtomicLong();
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
    long phase = appendPhase.get();
    long count = counter.incrementAndGet();
    boolean phaseChanged = appendPhase.get() != phase;
    appendersInFlight.decrementAndGet();
    if (phaseChanged || (phase & 1L) != 0 || (count & bufferActiveBit) == 0) {
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
      if (current != null && current != generation) {
        generation = current;
      }
      if (!generation.active) {
        return false;
      }
      while (generation.size >= maxBufferSize && generation.active) {
        bufferSpaceAvailable.awaitUninterruptibly();
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
                generation.values,
                Math.min(maxBufferSize, Math.max(INITIAL_BUFFER_SIZE, Math.max(1, doubled))));
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

  @SuppressWarnings({"NullAway", "ThreadPriorityCheck"})
  <T extends DataPointSnapshot> T run(
      Function<Long, Boolean> complete,
      Supplier<T> createResult,
      Consumer<Double> observeFunction) {
    return run(complete, createResult, observeFunction, true, complete);
  }

  @SuppressWarnings({"NullAway", "ThreadPriorityCheck"})
  <T extends DataPointSnapshot> T run(
      Function<Long, Boolean> complete,
      Supplier<T> createResult,
      Consumer<Double> observeFunction,
      boolean failOnTimeout) {
    return run(complete, createResult, observeFunction, failOnTimeout, complete);
  }

  @SuppressWarnings({"NullAway", "ThreadPriorityCheck"})
  <T extends DataPointSnapshot> T run(
      Function<Long, Boolean> complete,
      Supplier<T> createResult,
      Consumer<Double> observeFunction,
      boolean failOnTimeout,
      Function<Long, Boolean> afterTimeout) {
    Generation generation = new Generation();
    double[] buffer;
    int bufferSize;
    boolean timedOut = false;
    T result;
    runLock.lock();
    try {
      phaseTransition();
      long expectedCount;
      appendLock.lock();
      try {
        activeGeneration = generation;
        long total = 0;
        for (AtomicLong counter : stripedObservationCounts) {
          total += counter.getAndAdd(bufferActiveBit);
        }
        expectedCount = total - observationCountOffset;
      } finally {
        appendLock.unlock();
      }
      appendPhase.incrementAndGet();
      long deadline = System.nanoTime() + maxSpinWaitNanos;
      while (!complete.apply(expectedCount)) {
        if (System.nanoTime() - deadline >= 0) {
          timedOut = true;
          break;
        }
        Thread.yield();
      }
      result = timedOut ? null : createResult.get();
      phaseTransition();
      appendLock.lock();
      try {
        generation.active = false;
        for (AtomicLong counter : stripedObservationCounts) {
          counter.addAndGet(bufferActiveBit);
        }
        if (reset) {
          observationCountOffset += expectedCount;
          reset = false;
        }
        activeGeneration = null;
        buffer = generation.values;
        bufferSize = generation.size;
        generation.values = new double[0];
        generation.size = 0;
        bufferSpaceAvailable.signalAll();
      } finally {
        appendLock.unlock();
      }
      appendPhase.incrementAndGet();
      for (int i = 0; i < bufferSize; i++) {
        observeFunction.accept(buffer[i]);
      }
      if (timedOut && failOnTimeout) {
        if (afterTimeout.apply(expectedCount)) {
          return createResult.get();
        }
        throw new IllegalStateException("Timed out while waiting for in-flight observations.");
      }
      return result;
    } finally {
      runLock.unlock();
    }
  }

  @SuppressWarnings("ThreadPriorityCheck")
  private void phaseTransition() {
    appendPhase.incrementAndGet();
    while (appendersInFlight.get() != 0) {
      Thread.yield();
    }
  }
}
