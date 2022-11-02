package io.prometheus.metrics.observer;

import io.prometheus.metrics.model.Labels;

public interface DiscreteEventObserver extends Observer {

    default void inc() {
        inc(1.0);
    }
    default void incWithExemplar(Labels labels) {
        incWithExemplar(1.0, labels);
    }
    void inc(double amount);
    void incWithExemplar(double amount, Labels labels);
}
