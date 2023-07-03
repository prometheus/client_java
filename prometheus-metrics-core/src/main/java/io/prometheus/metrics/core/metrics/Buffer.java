package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.model.snapshots.DataPointSnapshot;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Metrics support concurrent write and scrape operations.
 * <p>
 * This is implemented by switching to a Buffer when the scrape starts,
 * and applying the values from the buffer after the scrape ends.
 */
class Buffer {

    private static final long signBit = 1L << 63;
    private final AtomicLong observationCount = new AtomicLong(0);
    private double[] observationBuffer = new double[0];
    private int bufferPos = 0;
    private boolean reset = false;
    private final Object appendLock = new Object();
    private final Object runLock = new Object();

    boolean append(double value) {
        long count = observationCount.incrementAndGet();
        if ((count & signBit) == 0) {
            return false; // sign bit not set -> buffer not active.
        } else {
            doAppend(value);
            return true;
        }
    }

    private void doAppend(double amount) {
        synchronized (appendLock) {
            if (bufferPos >= observationBuffer.length) {
                observationBuffer = Arrays.copyOf(observationBuffer, observationBuffer.length + 128);
            }
            observationBuffer[bufferPos] = amount;
            bufferPos++;
        }
    }

    /**
     * Must be called by the runnable in the run() method.
     */
    void reset() {
        reset = true;
    }

    <T extends DataPointSnapshot> T run(Function<Long, Boolean> complete, Supplier<T> runnable, Consumer<Double> observeFunction) {
        double[] buffer;
        int bufferSize;
        T result;
        synchronized (runLock) {
            Long count = observationCount.getAndAdd(signBit);
            while (!complete.apply(count)) {
                Thread.yield();
            }
            result = runnable.get();
            int expectedBufferSize;
            if (reset) {
                expectedBufferSize = (int) ((observationCount.getAndSet(0) & ~signBit) - count);
                reset = false;
            } else {
                expectedBufferSize = (int) (observationCount.addAndGet(signBit) - count);
            }
            while (bufferPos != expectedBufferSize) {
                Thread.yield();
            }
            buffer = observationBuffer;
            bufferSize = bufferPos;
            observationBuffer = new double[0];
            bufferPos = 0;
        }
        for (int i = 0; i < bufferSize; i++) {
            observeFunction.accept(buffer[i]);
        }
        return result;
    }
}
