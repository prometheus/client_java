package io.prometheus.metrics.observer;

import io.prometheus.metrics.model.Labels;

public interface GaugingObserver extends Observer {

    default void inc() {
        inc(1.0);
    }

    // TODO: Do we need this method?
    default void incWithExemplar() {
        incWithExemplar(1.0, Labels.EMPTY);
    }

    default void incWithExemplar(Labels labels) {
        incWithExemplar(1.0, labels);
    }

    void inc(double amount);

    default void incWithExemplar(double amount) {
        incWithExemplar(amount, Labels.EMPTY);
    }

    void incWithExemplar(double amount, Labels labels);

    default void dec() {
        inc(-1.0);
    }

    default void decWithExemplar() {
        incWithExemplar(-1.0, Labels.EMPTY);
    }

    default void decWithExemplar(Labels labels) {
        incWithExemplar(-1.0, labels);
    }

    default void dec(double amount) {
        inc(-amount);
    }

    default void decWithExemplar(double amount) {
        incWithExemplar(-amount, Labels.EMPTY);
    }
    default void decWithExemplar(double amount, Labels labels) {
        incWithExemplar(-amount, labels);
    }

    void set(double value);
    default void setWithExemplar(double value) {
        setWithExemplar(value, Labels.EMPTY);
    }
    void setWithExemplar(double value, Labels labels);

    default Timer startTimer() {
        return new Timer(this::set);
    }
}
