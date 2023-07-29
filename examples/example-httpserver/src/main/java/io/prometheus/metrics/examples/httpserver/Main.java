package io.prometheus.metrics.examples.httpserver;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.snapshots.Unit;

import java.io.IOException;

/**
 * Simple example of an application exposing metrics via Prometheus' built-in HTTPServer.
 */
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        Counter counter = Counter.newBuilder()
                .withName("uptime_seconds_total")
                .withHelp("total number of seconds since this application was started")
                .withUnit(Unit.SECONDS)
                .register();

        HTTPServer server = HTTPServer.newBuilder()
                .withPort(9400)
                .buildAndStart();

        System.out.println("HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");

        while (true) {
            Thread.sleep(1000);
            counter.inc();
        }
    }
}
