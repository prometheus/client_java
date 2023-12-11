package io.prometheus.metrics.core.datapoints;

/**
 * Represents a single StateSet data point.
 * <p>
 * See JavaDoc of {@link CounterDataPoint} on how using data points directly can improve performance.
 */
public interface StateSetDataPoint extends DataPoint {

    /**
     * {@code state} must be one of the states from when the {@code StateSet} was created with {@link io.prometheus.metrics.core.metrics.StateSet.Builder#states(String...)}.
     */
    void setTrue(String state);

    /**
     * {@code state} must be one of the states from when the {@code StateSet} was created with {@link io.prometheus.metrics.core.metrics.StateSet.Builder#states(String...)}.
     */
    void setFalse(String state);

    /**
     * {@code state} must be one of the states from when the {@code StateSet} was created with {@link io.prometheus.metrics.core.metrics.StateSet.Builder#states(Class)}.
     */
    default void setTrue(Enum<?> state) {
        setTrue(state.toString());
    }

    /**
     * {@code state} must be one of the states from when the {@code StateSet} was created with {@link io.prometheus.metrics.core.metrics.StateSet.Builder#states(Class)}.
     */
    default void setFalse(Enum<?> state) {
        setFalse(state.toString());
    }
}
