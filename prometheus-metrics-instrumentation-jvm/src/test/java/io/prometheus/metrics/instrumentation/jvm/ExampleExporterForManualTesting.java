package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;

import java.io.IOException;


public class ExampleExporterForManualTesting {

    public static void main(String[] args) throws IOException, InterruptedException {

        JvmMetrics.newBuilder().register();

        HTTPServer server = HTTPServer.newBuilder()
                .withPort(9400)
                .buildAndStart();

        System.out.println("HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");

        while (true) {
            Thread.sleep(100);
            Runtime.getRuntime().gc(); // Memory allocation metrics only start after GC run.
        }
    }
}
