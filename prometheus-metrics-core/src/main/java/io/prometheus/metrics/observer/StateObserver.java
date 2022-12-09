package io.prometheus.metrics.observer;

public interface StateObserver extends Observer {
    void setTrue(String state);
    void setFalse(String state);

    default void setTrue(Enum<?> state) {
        setTrue(state.toString());
    }
    default void setFalse(Enum<?> state) {
        setFalse(state.toString());
    }
}
