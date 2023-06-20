package com.example;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.observer.DiscreteEventObserver;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Counter counter = Counter.newBuilder()
                .withLabelNames("path")
                .withName("events_total")
                .build();
        DiscreteEventObserver goodCase = counter.withLabelValues("/get");
        PrometheusRegistry.defaultRegistry.register(counter);
        counter.inc(1.0);
        HTTPServer server = HTTPServer.newBuilder().withPort(9000).build();
    }
}
