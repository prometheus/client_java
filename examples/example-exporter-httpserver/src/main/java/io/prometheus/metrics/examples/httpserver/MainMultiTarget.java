package io.prometheus.metrics.examples.httpserver;

import java.io.IOException;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

/**
 * Simple example of an application exposing metrics via Prometheus' built-in HTTPServer.
 */
public class MainMultiTarget {

    public static void main(String[] args) throws IOException, InterruptedException {

        SampleExtendedCollector xc = new SampleExtendedCollector();
        PrometheusRegistry.defaultRegistry.register(xc);
        SampleExtendedMultiCollector xmc = new SampleExtendedMultiCollector();
        PrometheusRegistry.defaultRegistry.register(xmc);
        HTTPServer server = HTTPServer.builder()
                .port(9400)
                .buildAndStart();

        System.out.println("HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");
    }
}
