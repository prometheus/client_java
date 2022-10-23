package io.prometheus.metrics.observer;

import io.prometheus.metrics.model.Exemplar;
import io.prometheus.metrics.model.Labels;

import java.util.concurrent.Callable;
import java.util.function.Function;

public interface DistributionObserver extends Observer {
    void observe(double amount);
    void observeWithExemplar(double amount, Labels labels);
    default void time(Callable<?> callable) {

    }
}
