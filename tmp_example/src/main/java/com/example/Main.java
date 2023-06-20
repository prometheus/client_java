package com.example;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.observer.DiscreteEventObserver;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Counter counter = Counter.newBuilder()
                .withLabelNames("path", "status")
                .withName("events_total")
                .register();
        DiscreteEventObserver goodCase = counter.withLabelValues("/get", "200");
        DiscreteEventObserver errorCase = counter.withLabelValues("/get", "500");
        goodCase.inc();
        errorCase.inc();
        HTTPServer.newBuilder().withPort(9000).build();
    }
}
