package io.prometheus.metrics.observer;

public interface GaugingObserver extends Observer {

    default void inc() {
        inc(1.0);
    }

    void inc(double amount);

    default void dec() {
        inc(-1.0);
    }

    default void dec(double amount) {
        inc(-amount);
    }

    void set(double value);

    default Timer startTimer() {
        return new Timer(this::set);
    }
}
