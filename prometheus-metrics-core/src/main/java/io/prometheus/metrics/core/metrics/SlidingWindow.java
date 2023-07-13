package io.prometheus.metrics.core.metrics;

import java.lang.reflect.Array;
import java.util.concurrent.TimeUnit;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;

/**
 * Wrapper around {@link CKMSQuantiles}.
 * <p>
 * Maintains a ring buffer of CKMSQuantiles to provide quantiles over a sliding windows of time.
 */
class SlidingWindow<T> {

    private final Supplier<T> constructor;
    private final ObjDoubleConsumer<T> observeFunction;
    private final T[] ringBuffer;
    private int currentBucket;
    private long lastRotateTimestampMillis;
    private final long durationBetweenRotatesMillis;

    public SlidingWindow(Class<T> clazz, Supplier<T> constructor, ObjDoubleConsumer<T> observeFunction, long maxAgeSeconds, int ageBuckets) {
        this.constructor = constructor;
        this.observeFunction = observeFunction;
        this.ringBuffer = (T[]) Array.newInstance(clazz, ageBuckets);
        for (int i = 0; i < ringBuffer.length; i++) {
            this.ringBuffer[i] = constructor.get();
        }
        this.currentBucket = 0;
        this.lastRotateTimestampMillis = System.currentTimeMillis();
        this.durationBetweenRotatesMillis = TimeUnit.SECONDS.toMillis(maxAgeSeconds) / ageBuckets;
    }

    public synchronized T current() {
        return rotate();
    }

    public synchronized void observe(double value) {
        observeFunction.accept(rotate(), value);
    }

    private T rotate() {
        long timeSinceLastRotateMillis = System.currentTimeMillis() - lastRotateTimestampMillis;
        while (timeSinceLastRotateMillis > durationBetweenRotatesMillis) {
            ringBuffer[currentBucket] = constructor.get();
            if (++currentBucket >= ringBuffer.length) {
                currentBucket = 0;
            }
            timeSinceLastRotateMillis -= durationBetweenRotatesMillis;
            lastRotateTimestampMillis += durationBetweenRotatesMillis;
        }
        return ringBuffer[currentBucket];
    }
}
