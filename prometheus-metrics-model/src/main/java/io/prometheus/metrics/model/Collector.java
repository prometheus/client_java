package io.prometheus.metrics.model;

import java.util.Collection;

@FunctionalInterface
public interface Collector {
    MetricSnapshots collect();
}
