package io.prometheus.metrics.core.datapoints;

import io.prometheus.metrics.model.snapshots.Unit;

import java.io.Closeable;
import java.util.function.DoubleConsumer;

/**
 * Helper class for observing durations.
 */
public class Timer implements Closeable {

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
     * @return the observed duration in seconds.
     */
    public double observeDuration() {
        double elapsed = Unit.nanosToSeconds(System.nanoTime() - startTimeNanos);
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
