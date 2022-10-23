package io.prometheus.metrics.registry;

import io.prometheus.metrics.model.Collector;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.Metric;

public class PrometheusRegistry {
    public static final PrometheusRegistry defaultRegistry = new PrometheusRegistry();
    public void register(Metric<?> metric) {}
    public void register(Collector collector) {}

    public void unregister(Metric<?> metric) {}
    public void unregister(Collector collector) {}
    public void unregister(String name) {}
    public void unregister(String name, Labels labels) {}
}
