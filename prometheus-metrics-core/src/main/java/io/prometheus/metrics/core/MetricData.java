package io.prometheus.metrics.core;

import io.prometheus.metrics.observer.Observer;

// package private
abstract class MetricData<T extends Observer> {

    abstract T toObserver();
}
