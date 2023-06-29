package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.core.observer.DataPoint;

// package private
abstract class MetricData<T extends DataPoint> {

    abstract T toObserver();
}
