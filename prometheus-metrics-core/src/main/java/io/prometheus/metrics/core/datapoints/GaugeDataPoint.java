package io.prometheus.metrics.core.datapoints;

import io.prometheus.metrics.model.snapshots.Labels;

/**
 * Represents a single gauge data point, i.e. a single line for a gauge metric in Prometheus text format.
 * <p>
 * See JavaDoc of {@link CounterDataPoint} on how using data points directly can improve performance.
 */
public interface GaugeDataPoint extends DataPoint, TimerApi {

    /**
     * Add one.
     */
    default void inc() {
        inc(1.0);
    }

    /**
     * Add {@code amount}.
     */
    void inc(double amount);

    /**
     * Add one, and create a custom exemplar with the given labels.
     */
    default void incWithExemplar(Labels labels) {
        incWithExemplar(1.0, labels);
    }

    /**
     * Add {@code amount}, and create a custom exemplar with the given labels.
     */
    void incWithExemplar(double amount, Labels labels);

    /**
     * Subtract one.
     */
    default void dec() {
        inc(-1.0);
    }

    /**
     * Subtract {@code amount}.
     */
    default void dec(double amount) {
        inc(-amount);
    }

    /**
     * Subtract one, and create a custom exemplar with the given labels.
     */
    default void decWithExemplar(Labels labels) {
        incWithExemplar(-1.0, labels);
    }

    /**
     * Subtract {@code amount}, and create a custom exemplar with the given labels.
     */
    default void decWithExemplar(double amount, Labels labels) {
        incWithExemplar(-amount, labels);
    }

    /**
     * Set the gauge to {@code value}.
     */
    void set(double value);

    /**
     * Get the current value.
     */
    double get();

    /**
     * Set the gauge to {@code value}, and create a custom exemplar with the given labels.
     */
    void setWithExemplar(double value, Labels labels);

    /**
     * {@inheritDoc}
     */
    default Timer startTimer() {
        return new Timer(this::set);
    }
}
