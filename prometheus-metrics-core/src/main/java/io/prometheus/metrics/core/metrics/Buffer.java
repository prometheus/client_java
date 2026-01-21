package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Metrics support concurrent write and scrape operations.
 *
 * <p>This is implemented by switching to a Buffer when the scrape starts, and applying the values
 * from the buffer after the scrape ends.
 */
class Buffer {

  private static final long bufferActiveBit = 1L << 63;
  private final AtomicLong[] stripedObservationCounts;
  private double[] observationBuffer = new double[0];
  private int bufferPos = 0;
  private boolean reset = false;

  ReentrantLock appendLock = new ReentrantLock();
  ReentrantLock runLock = new ReentrantLock();
  Condition bufferFilled = appendLock.newCondition();

  Buffer() {
    stripedObservationCounts = new AtomicLong[Runtime.getRuntime().availableProcessors()];
    for (int i = 0; i < stripedObservationCounts.length; i++) {
      stripedObservationCounts[i] = new AtomicLong(0);
    }
  }

  boolean append(double value) {
    AtomicLong observationCountForThread =
        stripedObservationCounts[
            ((int) Thread.currentThread().getId()) % stripedObservationCounts.length];
    long count = observationCountForThread.incrementAndGet();
    if ((count & bufferActiveBit) == 0) {
      return false; // sign bit not set -> buffer not active.
    } else {
      doAppend(value);
      return true;
    }
  }

  private void doAppend(double amount) {
    appendLock.lock();
    try {
      if (bufferPos >= observationBuffer.length) {
        observationBuffer = Arrays.copyOf(observationBuffer, observationBuffer.length + 128);
      }
      observationBuffer[bufferPos] = amount;
      bufferPos++;

      bufferFilled.signalAll();
    } finally {
      appendLock.unlock();
    }
  }

  /** Must be called by the runnable in the run() method. */
  void reset() {
    reset = true;
  }

  @SuppressWarnings("ThreadPriorityCheck")
  <T extends DataPointSnapshot> T run(
      Function<Long, Boolean> complete,
      Supplier<T> createResult,
      Consumer<Double> observeFunction) {
    double[] buffer;
    int bufferSize;
    T result;

    runLock.lock();
    try {
      // Signal that the buffer is active.
      Long expectedCount = 0L;
      for (AtomicLong observationCount : stripedObservationCounts) {
        expectedCount += observationCount.getAndAdd(bufferActiveBit);
      }

      while (!complete.apply(expectedCount)) {
        // Wait until all in-flight threads have added their observations to the histogram /
        // summary.
        // we can't use a condition here, because the other thread doesn't have a lock as it's on
        // the fast path.
        Thread.yield();
      }
      result = createResult.get();

      // Signal that the buffer is inactive.
      long expectedBufferSize = 0;
      if (reset) {
        for (AtomicLong observationCount : stripedObservationCounts) {
          expectedBufferSize += (int) (observationCount.getAndSet(0) & ~bufferActiveBit);
        }
        reset = false;
      } else {
        for (AtomicLong observationCount : stripedObservationCounts) {
          expectedBufferSize += (int) observationCount.addAndGet(bufferActiveBit);
        }
      }
      expectedBufferSize -= expectedCount;

      appendLock.lock();
      try {
        while (bufferPos < expectedBufferSize) {
          // Wait until all in-flight threads have added their observations to the buffer.
          bufferFilled.await();
        }
      } finally {
        appendLock.unlock();
      }

      buffer = observationBuffer;
      bufferSize = bufferPos;
      observationBuffer = new double[0];
      bufferPos = 0;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      runLock.unlock();
    }

    for (int i = 0; i < bufferSize; i++) {
      observeFunction.accept(buffer[i]);
    }
    return result;
  }
}
