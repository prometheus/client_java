package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.core.observer.Observer;

// package private
abstract class MetricData<T extends Observer> {

    abstract T toObserver();
}
