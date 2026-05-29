package io.prometheus.metrics.examples.custombuckets;

import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import io.prometheus.metrics.model.snapshots.Unit;
import java.io.IOException;
import java.util.Random;

/**
 * Example demonstrating native histograms with custom buckets (NHCB).
 *
 * <p>This example shows three different types of custom bucket configurations:
 *
 * <ul>
 *   <li>API latency with arbitrary custom boundaries optimized for typical response times
 *   <li>Queue size with linear boundaries for equal-width buckets
 *   <li>Response size with exponential boundaries for data spanning multiple orders of magnitude
 * </ul>
 *
 * <p>These histograms maintain both classic (with custom buckets) and native representations. When
 * Prometheus is configured with {@code convert_classic_histograms_to_nhcb: true}, the custom bucket
 * boundaries are preserved in the native histogram format (schema -53).
 */
public class Main {

  public static void main(String[] args) throws IOException, InterruptedException {

    JvmMetrics.builder().register();

    // Example 1: API latency with arbitrary custom boundaries
    // Optimized for typical API response times in seconds
    Histogram apiLatency =
        Histogram.builder()
            .name("api_request_duration_seconds")
            .help("API request duration with custom buckets")
            .unit(Unit.SECONDS)
            .classicUpperBounds(0.01, 0.05, 0.1, 0.5, 1.0, 5.0, 10.0)
            .labelNames("endpoint", "status")
            .register();

    // Example 2: Queue size with linear boundaries
    // Equal-width buckets for monitoring queue depth
    Histogram queueSize =
        Histogram.builder()
            .name("message_queue_size")
            .help("Number of messages in queue with linear buckets")
            .classicLinearUpperBounds(10, 10, 10) // 10, 20, 30, ..., 100
            .labelNames("queue_name")
            .register();

    // Example 3: Response size with exponential boundaries
    // Exponential growth for data spanning multiple orders of magnitude
    Histogram responseSize =
        Histogram.builder()
            .name("http_response_size_bytes")
            .help("HTTP response size in bytes with exponential buckets")
            .classicExponentialUpperBounds(100, 10, 6) // 100, 1k, 10k, 100k, 1M, 10M
            .labelNames("endpoint")
            .register();

    HTTPServer server = HTTPServer.builder().port(9400).buildAndStart();

    System.out.println(
        "HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");
    System.out.println("\nGenerating metrics with custom bucket configurations:");
    System.out.println("1. API latency: custom boundaries optimized for response times");
    System.out.println("2. Queue size: linear boundaries (10, 20, 30, ..., 100)");
    System.out.println("3. Response size: exponential boundaries (100, 1k, 10k, ..., 10M)");
    System.out.println("\nPrometheus will convert these to NHCB (schema -53) when configured.\n");

    Random random = new Random(0);

    while (true) {
      // Simulate API latency observations
      // Fast endpoint: mostly < 100ms, occasionally slow
      double fastLatency = Math.abs(random.nextGaussian() * 0.03 + 0.05);
      String status = random.nextInt(100) < 95 ? "200" : "500";
      apiLatency.labelValues("/api/fast", status).observe(fastLatency);

      // Slow endpoint: typically 1-3 seconds
      double slowLatency = Math.abs(random.nextGaussian() * 0.5 + 2.0);
      apiLatency.labelValues("/api/slow", status).observe(slowLatency);

      // Simulate queue size observations
      // Queue oscillates between 20-80 items
      int queueDepth = 50 + (int) (random.nextGaussian() * 15);
      queueDepth = Math.max(0, Math.min(100, queueDepth));
      queueSize.labelValues("default").observe(queueDepth);

      // Priority queue: usually smaller
      int priorityQueueDepth = 10 + (int) (random.nextGaussian() * 5);
      priorityQueueDepth = Math.max(0, Math.min(50, priorityQueueDepth));
      queueSize.labelValues("priority").observe(priorityQueueDepth);

      // Simulate response size observations
      // Small responses: mostly < 10KB
      double smallResponse = Math.abs(random.nextGaussian() * 2000 + 5000);
      responseSize.labelValues("/api/summary").observe(smallResponse);

      // Large responses: can be up to several MB
      double largeResponse = Math.abs(random.nextGaussian() * 200000 + 500000);
      responseSize.labelValues("/api/download").observe(largeResponse);

      Thread.sleep(1000);
    }
  }
}
