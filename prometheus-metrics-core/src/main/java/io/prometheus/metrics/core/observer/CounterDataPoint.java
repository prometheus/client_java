package io.prometheus.metrics.core.observer;

import io.prometheus.metrics.model.snapshots.Labels;

/**
 * Represents a single counter data point, i.e. a single line for a counter metric in Prometheus text format.
 * <p>
 * Example usage:
 * <pre>{@code
 * Counter counter = Counter.newBuilder()
 *     .withName("tasks_total")
 *     .withLabelNames("status")
 *     .register();
 * CounterDataPoint newTasks = counter.withLabelValues("new");
 * CounterDataPoint pendingTasks = counter.withLabelValues("pending");
 * CounterDataPoint completedTasks = counter.withLabelValues("completed");
 * }</pre>
 * <p>
 * Using {@code DataPoint} directly improves performance. If you increment a counter like this:
 * <pre>{@code
 * counter.withLabelValues("pending").inc();
 * }</pre>
 * the label value {@code "pending"} needs to be looked up every single time.
 * Using the {@code CounterDataPoint} like this:
 * <pre>{@code
 * CounterDataPoint pendingTasks = counter.withLabelValues("pending");
 * pendingTasks.inc();
 * }</pre>
 * allows you to look up the label value only once, and then use the {@code CounterDataPoint} directly.
 * This is a worthwhile performance improvement when instrumenting a performance-critical code path.
 * <p>
 * If you have a counter without labels like this:
 * <pre>{@code
 * Counter counterWithoutLabels = Counter.newBuilder()
 *     .withName("events_total")
 *     .register();
 * }</pre>
 * You can use it as a {@code CounterDataPoint} directly. So the following:
 * <pre>{@code
 * CounterDataPoint counterData = counterWithoutLabels.withLabels();
 * }</pre>
 * is equivalent to
 * <pre>{@code
 * CounterDataPoint counterData = counterWithoutLabels;
 * }</pre>
 */
public interface CounterDataPoint extends DataPoint {

    /**
     * Add one.
     */
    default void inc() {
        inc(1L);
    }

    /**
     * Add {@code amount}. Throws an {@link IllegalArgumentException} if {@code amount} is negative.
     */
    default void inc(long amount) {
        inc((double) amount);
    }

    /**
     * Add one, but also create a custom exemplar with the given labels.
     */
    default void incWithExemplar(Labels labels) {
        incWithExemplar(1.0, labels);
    }

    /**
     * Add {@code amount}. Throws an {@link IllegalArgumentException} if {@code amount} is negative.
     */
    void inc(double amount);

    /**
     * Add {@code amount}, but also create a custom exemplar with the given labels.
     * Throws an {@link IllegalArgumentException} if {@code amount} is negative.
     */
    void incWithExemplar(double amount, Labels labels);

    /**
     * Add {@code amount}, but also create a custom exemplar with the given labels.
     * Throws an {@link IllegalArgumentException} if {@code amount} is negative.
     */
    default void incWithExemplar(long amount, Labels labels) {
        inc((double) amount);
    }
}
