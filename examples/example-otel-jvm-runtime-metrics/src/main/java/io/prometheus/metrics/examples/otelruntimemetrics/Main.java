package io.prometheus.metrics.examples.otelruntimemetrics;

import io.opentelemetry.exporter.prometheus.PrometheusMetricReader;
import io.opentelemetry.instrumentation.runtimemetrics.java8.RuntimeMetrics;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;
import java.io.IOException;

/**
 * Example combining Prometheus metrics with OpenTelemetry JVM runtime metrics on a single endpoint.
 *
 * <p>This demonstrates:
 *
 * <ul>
 *   <li>Registering a Prometheus counter metric
 *   <li>Bridging OTel runtime metrics into the same PrometheusRegistry
 *   <li>Exposing everything via the built-in HTTPServer on /metrics
 * </ul>
 */
public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {

    PrometheusRegistry registry = new PrometheusRegistry();

    // 1. Register a Prometheus counter metric
    Counter counter =
        Counter.builder()
            .name("uptime_seconds_total")
            .help("total number of seconds since this application was started")
            .unit(Unit.SECONDS)
            .register(registry);

    // 2. Create a PrometheusMetricReader and register it with the same registry.
    //    This bridges OTel metrics into the Prometheus registry.
    PrometheusMetricReader reader = PrometheusMetricReader.create();
    registry.register(reader);

    // 3. Build the OTel SDK with the reader.
    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setMeterProvider(SdkMeterProvider.builder().registerMetricReader(reader).build())
            .build();

    // 4. Start OTel JVM runtime metrics collection.
    //    - captureGcCause() adds a jvm.gc.cause attribute to jvm.gc.duration
    //    - emitExperimentalTelemetry() enables buffer pools, extended CPU,
    //      extended memory pools, and file descriptor metrics
    RuntimeMetrics runtimeMetrics =
        RuntimeMetrics.builder(openTelemetry).captureGcCause().emitExperimentalTelemetry().build();

    // 5. Expose both Prometheus and OTel metrics on a single endpoint.
    HTTPServer server = HTTPServer.builder().port(9400).registry(registry).buildAndStart();

    // 6. Close RuntimeMetrics and server on shutdown to stop JMX metric collection.
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  runtimeMetrics.close();
                  server.close();
                }));

    System.out.println(
        "HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");

    while (true) {
      Thread.sleep(1000);
      counter.inc();
    }
  }
}
