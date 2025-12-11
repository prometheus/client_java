package io.prometheus.metrics.it.exporter.duplicatemetrics;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.model.snapshots.Unit;
import java.io.IOException;

/** Integration test sample demonstrating metrics with duplicate names but different label sets. */
public class DuplicateMetricsSample {

  public static void main(String[] args) throws IOException, InterruptedException {
    if (args.length < 1 || args.length > 2) {
      System.err.println("Usage: java -jar duplicate-metrics-sample.jar <port>");
      System.exit(1);
    }

    int port = parsePortOrExit(args[0]);
    run(port);
  }

  private static void run(int port) throws IOException, InterruptedException {
    // Register multiple counters with the same Prometheus name "http_requests_total"
    // but different label sets
    Counter requestsSuccess =
        Counter.builder()
            .name("http_requests_total")
            .help("Total HTTP requests by status")
            .labelNames("status", "method")
            .register();
    requestsSuccess.labelValues("success", "GET").inc(150);
    requestsSuccess.labelValues("success", "POST").inc(45);

    Counter requestsError =
        Counter.builder()
            .name("http_requests_total")
            .help("Total HTTP requests by status")
            .labelNames("status", "endpoint")
            .register();
    requestsError.labelValues("error", "/api").inc(5);
    requestsError.labelValues("error", "/health").inc(2);

    // Register multiple gauges with the same Prometheus name "active_connections"
    // but different label sets
    Gauge connectionsByRegion =
        Gauge.builder()
            .name("active_connections")
            .help("Active connections")
            .labelNames("region", "protocol")
            .register();
    connectionsByRegion.labelValues("us-east", "http").set(42);
    connectionsByRegion.labelValues("us-west", "http").set(38);
    connectionsByRegion.labelValues("eu-west", "https").set(55);

    Gauge connectionsByPool =
        Gauge.builder()
            .name("active_connections")
            .help("Active connections")
            .labelNames("pool", "type")
            .register();
    connectionsByPool.labelValues("primary", "read").set(30);
    connectionsByPool.labelValues("replica", "write").set(10);

    // Also add a regular metric without duplicates for reference
    Counter uniqueMetric =
        Counter.builder()
            .name("unique_metric_total")
            .help("A unique metric for reference")
            .unit(Unit.BYTES)
            .register();
    uniqueMetric.inc(1024);

    HTTPServer server = HTTPServer.builder().port(port).buildAndStart();

    System.out.println(
        "DuplicateMetricsSample listening on http://localhost:" + server.getPort() + "/metrics");
    Thread.currentThread().join(); // wait forever
  }

  private static int parsePortOrExit(String port) {
    try {
      return Integer.parseInt(port);
    } catch (NumberFormatException e) {
      System.err.println("\"" + port + "\": Invalid port number.");
      System.exit(1);
    }
    return 0; // this won't happen
  }
}
