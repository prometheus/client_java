package io.prometheus.metrics.examples.multitarget;

import java.io.IOException;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

/**
 * Simple example of an application exposing metrics via Prometheus' built-in HTTPServer.
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        SampleMultiCollector xmc = new SampleMultiCollector();
        PrometheusRegistry.defaultRegistry.register(xmc);
        HTTPServer server = HTTPServer.builder()
                .port(9401)
                .buildAndStart();

        System.out.println("HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");
    }
}
