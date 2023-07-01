package com.example;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        Counter counter = Counter.newBuilder()
                .withLabelNames("path", "status")
                .withName("events_total")
                .register();
        CounterDataPoint goodCase = counter.withLabelValues("/get", "200");
        CounterDataPoint errorCase = counter.withLabelValues("/get", "500");
        goodCase.inc();
        errorCase.inc();
        HTTPServer.newBuilder().withPort(9000).build();
    }
}
