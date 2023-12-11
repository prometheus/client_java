package io.prometheus.metrics.examples.opentelemetry;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.opentelemetry.OpenTelemetryExporter;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import io.prometheus.metrics.model.snapshots.Unit;

/**
 * Simple example of an application exposing metrics pushing metrics via OTLP.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // Note: Some JVM metrics are also defined as OpenTelemetry's semantic conventions.
        // We have plans to implement a configuration option for JvmMetrics to use OpenTelemetry
        // naming conventions rather than the Prometheus names.
        JvmMetrics.builder().register();

        // Note: uptime_seconds_total is not a great example:
        // The built-in JvmMetrics have an out-of-the-box metric named process_start_time_seconds
        // with the start timestamp in seconds, so if you want to know the uptime you can simply
        // run the Prometheus query
        //     time() - process_start_time_seconds
        // rather than creating a custom uptime metric.
        Counter counter = Counter.builder()
                .name("uptime_seconds_total")
                .help("total number of seconds since this application was started")
                .unit(Unit.SECONDS)
                .register();

        OpenTelemetryExporter.builder()
                .intervalSeconds(5) // ridiculously short interval for demo purposes
                .buildAndStart();

        while (true) {
            Thread.sleep(1000);
            counter.inc();
        }
    }
}
