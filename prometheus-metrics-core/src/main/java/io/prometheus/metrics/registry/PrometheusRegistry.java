package io.prometheus.metrics.registry;

import io.prometheus.metrics.model.Collector;
import io.prometheus.metrics.model.Labels;

public class PrometheusRegistry {
    public static final PrometheusRegistry defaultRegistry = new PrometheusRegistry();
    public void register(io.prometheus.metrics.core.Metric metric) {}
    public void register(Collector collector) {}

    public void unregister(io.prometheus.metrics.core.Metric metric) {}
    public void unregister(Collector collector) {}
    public void unregister(String name) {}
    public void unregister(String name, Labels labels) {}
}
