package io.prometheus.metrics.core.metrics;

import java.lang.reflect.Array;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;

/**
 * Maintains a ring buffer of T to implement a sliding time window.
 * <p>
 * This is used to maintain a sliding window of {@link CKMSQuantiles} for {@link Summary} metrics.
 * <p>
 * It is implemented in a generic way so that 3rd party libraries can use it for implementing sliding windows.
 * <p>
 * TODO: The current implementation is {@code synchronized}. There is likely room for optimization.
 */
public class SlidingWindow<T> {

    private final Supplier<T> constructor;
    private final ObjDoubleConsumer<T> observeFunction;
    private final T[] ringBuffer;
    private int currentBucket;
    private long lastRotateTimestampMillis;
    private final long durationBetweenRotatesMillis;
    LongSupplier currentTimeMillis = System::currentTimeMillis; // to be replaced in unit tests

    /**
     * Example: If the {@code maxAgeSeconds} is 60 and {@code ageBuckets} is 3, then 3 instances of {@code T}
     * are maintained and the sliding window moves to the next instance of T every 20 seconds.
     *
     * @param clazz type of T
     * @param constructor for creating a new instance of T as the old one gets evicted
     * @param observeFunction for observing a value (e.g. calling {@code t.observe(value)}
     * @param maxAgeSeconds after this amount of time an instance of T gets evicted.
     * @param ageBuckets number of age buckets.
     */
    public SlidingWindow(Class<T> clazz, Supplier<T> constructor, ObjDoubleConsumer<T> observeFunction, long maxAgeSeconds, int ageBuckets) {
        this.constructor = constructor;
        this.observeFunction = observeFunction;
        this.ringBuffer = (T[]) Array.newInstance(clazz, ageBuckets);
        for (int i = 0; i < ringBuffer.length; i++) {
            this.ringBuffer[i] = constructor.get();
        }
        this.currentBucket = 0;
        this.lastRotateTimestampMillis = currentTimeMillis.getAsLong();
        this.durationBetweenRotatesMillis = TimeUnit.SECONDS.toMillis(maxAgeSeconds) / ageBuckets;
    }

    /**
     * Get the currently active instance of {@code T}.
     */
    public synchronized T current() {
        return rotate();
    }

    /**
     * Observe a value.
     */
    public synchronized void observe(double value) {
        rotate();
        for (T t : ringBuffer) {
            observeFunction.accept(t, value);
        }
    }

    private T rotate() {
        long timeSinceLastRotateMillis = currentTimeMillis.getAsLong() - lastRotateTimestampMillis;
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
