package io.prometheus.metrics.core.datapoints;

import java.io.Closeable;
import java.util.function.DoubleConsumer;

/**
 * Helper class for observing durations.
 */
public class Timer implements Closeable {

    private static final double NANOSECONDS_PER_SECOND = 1E9;
    private final DoubleConsumer observeFunction;
    private final long startTimeNanos = System.nanoTime();

    /**
     * Constructor is package private. Use the {@link TimerApi} provided by the implementation of the {@link DataPoint}.
     */
    Timer(DoubleConsumer observeFunction) {
        this.observeFunction = observeFunction;
    }

    /**
     * Records the observed duration in seconds since this {@code Timer} instance was created.
     * @return the observed duration.
     */
    public double observeDuration() {
        double elapsed = (System.nanoTime() - startTimeNanos) / NANOSECONDS_PER_SECOND;
        observeFunction.accept(elapsed);
        return elapsed;
    }

    /**
     * Same as {@link #observeDuration()}.
     */
    @Override
    public void close() {
        observeDuration();
    }
}
