package io.prometheus.metrics.core.datapoints;

public interface StateSetDataPoint extends DataPoint {
    void setTrue(String state);
    void setFalse(String state);

    default void setTrue(Enum<?> state) {
        setTrue(state.toString());
    }
    default void setFalse(Enum<?> state) {
        setFalse(state.toString());
    }
}
