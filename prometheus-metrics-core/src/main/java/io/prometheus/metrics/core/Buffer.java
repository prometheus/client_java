package io.prometheus.metrics.core;

import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.Snapshot;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

class Buffer<T extends Snapshot> {

    private static final long signBit = 1L << 63;
    private final AtomicLong observationCount = new AtomicLong(0);
    private double[] observationBuffer = new double[0];
    private Labels[] exemplarLabelBuffer = new Labels[0];
    private int bufferPos = 0;
    private final Object writeLock = new Object();

    boolean append(double amount) {
        long count = observationCount.incrementAndGet();
        if ((count & signBit) == 0) {
            return false; // sign bit not set -> buffer not active.
        } else {
            doAppend(amount, null);
            return true;
        }
    }

    boolean append(double amount, Labels exemplarLabels) {
        long count = observationCount.incrementAndGet();
        if ((count & signBit) == 0) {
            return false; // sign bit not set -> buffer not active.
        } else {
            doAppend(amount, exemplarLabels);
            return true;
        }
    }

    private void doAppend(double amount, Labels labels) {
        synchronized (writeLock) {
            if (bufferPos >= observationBuffer.length) {
                observationBuffer = Arrays.copyOf(observationBuffer, observationBuffer.length + 128);
                exemplarLabelBuffer = Arrays.copyOf(exemplarLabelBuffer, exemplarLabelBuffer.length + 128);
            }
            observationBuffer[bufferPos] = amount;
            exemplarLabelBuffer[bufferPos] = labels;
            bufferPos++;
        }
    }

    synchronized T run(Function<Long, Boolean> complete, Supplier<T> runnable, BiConsumer<Double, Labels> observeFunction) {
        Long count = observationCount.getAndAdd(signBit);
        while (!complete.apply(count)) {
            Thread.yield();
        }
        T result = runnable.get();
        int expectedBufferSize = (int) (observationCount.addAndGet(signBit) - count);
        while (bufferPos != expectedBufferSize) {
            Thread.yield();
        }
        for (int i=0; i<bufferPos; i++) {
            observeFunction.accept(observationBuffer[i], exemplarLabelBuffer[i]);
        }
        observationBuffer = new double[0];
        exemplarLabelBuffer = new Labels[0];
        bufferPos = 0;
        return result;
    }
}
