package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Metrics support concurrent write and scrape operations.
 *
 * <p>This is implemented by switching to a Buffer when the scrape starts, and applying the values
 * from the buffer after the scrape ends.
 */
class Buffer {

  private static final long bufferActiveBit = 1L << 63;
  private final AtomicLong observationCount = new AtomicLong(0);
  private double[] observationBuffer = new double[0];
  private int bufferPos = 0;
  private boolean reset = false;

  ReentrantLock appendLock = new ReentrantLock();
  ReentrantLock runLock = new ReentrantLock();
  Condition bufferFilled = appendLock.newCondition();

  boolean append(double value) {
    long count = observationCount.incrementAndGet();
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

  <T extends DataPointSnapshot> T run(
      Supplier<Long> getCount, Supplier<T> createResult, Consumer<Double> observeFunction) {
    double[] buffer;
    int bufferSize;
    T result;

    runLock.lock();
    try {
      appendLock.lock();
      try {
        // Signal that the buffer is active.
        long expectedCount = observationCount.getAndAdd(bufferActiveBit);
        long count = getCount.get();

        while (count + bufferPos < expectedCount) {
          // Wait until all in-flight threads have added their observations to the buffer.
          bufferFilled.await();
        }
        result = createResult.get();

        if (reset) {
          observationCount.getAndSet(0);
          reset = false;
        } else {
          observationCount.addAndGet(bufferActiveBit);
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
