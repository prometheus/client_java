package io.prometheus.metrics.tmp;


import io.prometheus.metrics.core.Counter;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.registry.PrometheusRegistry;

public class Examples {

    public static void main(String[] args) {
        Counter counter = Counter.builder()
                .withConstLabels(Labels.of("env", "prod"))
                .withLabelNames("path")
                .build();
        //
        /*
        PrometheusRegistry.counter(name).inc();
        PrometheusRegistry.counter(name, labels).inc();
        PrometheusRegistry.counter(name, help, unit, labels).inc();
         */
    }
}
