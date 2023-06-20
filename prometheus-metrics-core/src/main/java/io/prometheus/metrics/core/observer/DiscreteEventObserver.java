package io.prometheus.metrics.core.observer;

import io.prometheus.metrics.model.snapshots.Labels;

public interface DiscreteEventObserver extends Observer {

    default void inc() {
        inc(1L);
    }
    default void incWithExemplar(Labels labels) {
        incWithExemplar(1.0, labels);
    }
    void inc(double amount);
    default void inc(long amount) {
        inc((double) amount);
    }
    void incWithExemplar(double amount, Labels labels);
    default void incWithExemplar(long amount, Labels labels) {
        inc((double) amount);
    }
}
