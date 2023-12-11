package io.prometheus.metrics.examples.opentelemetry;

/*
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.core.metrics.Info;
import io.prometheus.metrics.core.metrics.StateSet;
import io.prometheus.metrics.core.metrics.Summary;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.exporter.opentelemetry.OpenTelemetryExporter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Unit;
import io.prometheus.metrics.model.snapshots.UnknownSnapshot;

import java.util.Random;
*/

public class ManualCompleteMetricsTest {

    // This contains a complete set of all metric types, and target_info and otel_scope_info.
    // I used this to expose in Prometheus format and OTLP format at the same time and compare the results.
    // I'm keeping this as a backup for now, but this should be converted to an integration test.
    //
    // To run it, add prometheus-metrics-exporter-httpserver as a dependency and configure Prometheus
    // to scrape from port 9400 in addition to receiving metrics via remote write.

    /*
    public static void main(String[] args) throws Exception {

        Counter counter = Counter.newBuilder()
                .withName("uptime_seconds_total")
                .withHelp("total number of seconds since this application was started")
                .withUnit(Unit.SECONDS)
                .register();

        Gauge gauge = Gauge.newBuilder()
                .withName("temperature_celsius")
                .withHelp("temperature in celsius")
                .withUnit(Unit.CELSIUS)
                .withLabelNames("location")
                .register();

        gauge.labelValues("inside").set(23.4);
        gauge.labelValues("outside").set(9.3);

        // By default, the histogram will be exported as an exponential histogram in OpenTelemetry.
        Histogram histogram = Histogram.newBuilder()
                .withName("request_latency_seconds")
                .withHelp("Request duration in seconds")
                .withUnit(Unit.SECONDS)
                .withLabelNames("http_status")
                .register();

        Random random = new Random(0);
        for (int i = 0; i < 1000; i++) {
            histogram.labelValues("200").observe(random.nextGaussian());
        }

        // Explicitly use a classic-only histogram to have an example of a classic histogram in OpenTelemetry
        Histogram classicHistogram = Histogram.newBuilder()
                .withName("request_size_bytes")
                .withHelp("Request size in Bytes")
                .withUnit(Unit.BYTES)
                .withLabelNames("path")
                .classicOnly()
                .withClassicBuckets(128, 256, 512, 1024, 2048)
                .register();

        for (int i = 0; i < 15; i++) {
            classicHistogram.labelValues("200").observe(random.nextInt(3000));
        }

        Summary summary = Summary.newBuilder()
                .withName("response_latency_seconds")
                .withHelp("Response latency seconds")
                .withUnit(Unit.BYTES)
                .withQuantile(0.95)
                .withQuantile(0.99)
                .register();

        for (int i = 0; i < 1000; i++) {
            summary.observe(random.nextGaussian());
        }

        Info targetInfo = Info.newBuilder()
                .withName("target_info")
                .withHelp("OTel resource")
                .withLabelNames("service.version")
                .register();
        targetInfo.setLabelValues("1.0.0");

        Info scopeInfo = Info.newBuilder()
                .withName("otel_scope_info")
                .withLabelNames("otel.scope.name", "otel.scope.version", "library_mascot")
                .register();

        scopeInfo.setLabelValues("my.instrumentation.lib", "100.3", "bear");

        Info info = Info.newBuilder()
                .withName("java_runtime_info")
                .withHelp("Java runtime info")
                .withLabelNames("version", "vendor", "runtime")
                .register();

        String version = System.getProperty("java.runtime.version", "unknown");
        String vendor = System.getProperty("java.vm.vendor", "unknown");
        String runtime = System.getProperty("java.runtime.name", "unknown");

        info.setLabelValues(version, vendor, runtime);

        StateSet stateSet = StateSet.newBuilder()
                .withName("feature_flags")
                .withLabelNames("env")
                .withStates("feature1", "feature2")
                .register();

        stateSet.labelValues("dev").setFalse("feature1");
        stateSet.labelValues("dev").setTrue("feature2");

        PrometheusRegistry.defaultRegistry.register(() -> UnknownSnapshot.newBuilder()
                .withName("my_unknown_metric")
                .addDataPoint(UnknownSnapshot.UnknownDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("a", "1", "b", "2"))
                        .withValue(3.0)
                        .build())
                .build());

        HTTPServer server = HTTPServer.newBuilder()
                .withPort(9400)
                .buildAndStart();
        System.out.println("HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");

        OpenTelemetryExporter.newBuilder()
                .withIntervalSeconds(5)
                .buildAndStart();

        while (true) {
            Thread.sleep(1000);
            counter.inc();
        }
    }
     */
}
