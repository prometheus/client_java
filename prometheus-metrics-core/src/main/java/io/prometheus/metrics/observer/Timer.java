package io.prometheus.metrics.observer;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.DoubleConsumer;

public class Timer implements Closeable {

    private static final double NANOSECONDS_PER_SECOND = 1E9;
    private final DoubleConsumer observeFunction;
    private final long startTimeNanos = System.nanoTime();

    // package private
    Timer(DoubleConsumer observeFunction) {
        this.observeFunction = observeFunction;
    }

    public double observeDuration() {
        double elapsed = (System.nanoTime() - startTimeNanos) / NANOSECONDS_PER_SECOND;
        observeFunction.accept(elapsed);
        return elapsed;
    }

    @Override
    public void close() {
        observeDuration();
    }
}
