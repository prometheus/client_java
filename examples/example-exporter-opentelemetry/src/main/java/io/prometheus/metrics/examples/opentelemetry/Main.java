package io.prometheus.metrics.examples.opentelemetry;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.opentelemetry.OpenTelemetryExporter;
import io.prometheus.metrics.model.snapshots.Unit;

/**
 * Simple example of an application exposing metrics pushing metrics via OTLP.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        Counter counter = Counter.newBuilder()
                .withName("uptime_seconds_total")
                .withHelp("total number of seconds since this application was started")
                .withUnit(Unit.SECONDS)
                .register();

        OpenTelemetryExporter.newBuilder()
                .withIntervalSeconds(5)
                .buildAndStart();

        while (true) {
            Thread.sleep(1000);
            counter.inc();
        }
    }
}
