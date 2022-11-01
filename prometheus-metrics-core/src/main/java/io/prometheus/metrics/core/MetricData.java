package io.prometheus.metrics.core;

import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.observer.Observer;

// package private
interface MetricData<T extends Observer> {
    T toObserver();
}
