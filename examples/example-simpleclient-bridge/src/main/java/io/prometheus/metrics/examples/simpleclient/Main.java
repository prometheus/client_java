package io.prometheus.metrics.examples.simpleclient;

import io.prometheus.client.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.simpleclient.bridge.SimpleclientCollector;

import java.io.IOException;

/**
 * Simple example of the simpleclient backwards compatibility module.
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        // The following call will register all metrics from the old CollectorRegistry.defaultRegistry
        // with the new PrometheusRegistry.defaultRegistry.

        SimpleclientCollector.builder().register();

        // Register a counter with the old CollectorRegistry.
        // It doesn't matter whether the counter is registered before or after bridging with PrometheusRegistry.

        Counter simpleclientCounter = Counter.build()
                .name("events_total")
                .help("total number of events")
                .register();

        simpleclientCounter.inc();

        // Expose metrics from the new PrometheusRegistry. This should contain the events_total metric.

        HTTPServer server = HTTPServer.builder()
                .port(9400)
                .buildAndStart();

        System.out.println("HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");

        Thread.currentThread().join();
    }
}
