package io.prometheus.metrics.examples.prometheus_properties;

import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import io.prometheus.metrics.model.snapshots.Unit;

import java.io.IOException;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {

        JvmMetrics.builder().register();

        Histogram requestDuration = Histogram.builder()
                .name("request_duration_seconds")
                .help("request duration in seconds")
                .unit(Unit.SECONDS)
                .register();

        Histogram requestSize = Histogram.builder()
                .name("request_size_bytes")
                .help("request size in bytes")
                .unit(Unit.BYTES)
                .register();

        HTTPServer server = HTTPServer.builder()
                .port(9400)
                .buildAndStart();

        System.out.println("HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");

        Random random = new Random(0);

        while (true) {
            double duration = Math.abs(random.nextGaussian() / 10.0 + 0.2);
            double size = random.nextInt(1000) + 256;
            requestDuration.observe(duration);
            requestSize.observe(size);
            Thread.sleep(1000);
        }
    }
}
