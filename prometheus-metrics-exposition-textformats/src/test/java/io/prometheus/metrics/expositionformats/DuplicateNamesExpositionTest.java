package io.prometheus.metrics.expositionformats;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class DuplicateNamesExpositionTest {

  private static PrometheusRegistry getPrometheusRegistry() {
    PrometheusRegistry registry = new PrometheusRegistry();

    registry.register(
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(100)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        });

    registry.register(
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(
                            Labels.of("uri", "/hello", "outcome", "FAILURE", "error", "TIMEOUT"))
                        .value(10)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        });
    return registry;
  }

  @Test
  void testDuplicateNames_differentLabels_producesValidOutput() throws IOException {
    PrometheusRegistry registry = getPrometheusRegistry();

    MetricSnapshots snapshots = registry.scrape();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrometheusTextFormatWriter writer = PrometheusTextFormatWriter.create();
    writer.write(out, snapshots);
    String output = out.toString(UTF_8);

    System.out.println("=== Duplicate Names (Different Labels) Output ===");
    System.out.println(output);
    System.out.println("=== End Output ===\n");

    // Verify output contains both metrics
    assertThat(output).contains("api_responses_total{");
    assertThat(output).contains("outcome=\"SUCCESS\"");
    assertThat(output).contains("outcome=\"FAILURE\"");
    assertThat(output).contains("error=\"TIMEOUT\"");
    assertThat(output).contains(" 100");
    assertThat(output).contains(" 10");

    // Verify TYPE declaration appears (may appear multiple times)
    assertThat(output).contains("# TYPE api_responses_total counter");

    // Count how many times the metric name appears in data lines
    long metricLines =
        output.lines().filter(line -> line.startsWith("api_responses_total{")).count();
    assertThat(metricLines).isEqualTo(2);
  }

  @Test
  void testDuplicateNames_sameLabels_producesOutput() throws IOException {
    PrometheusRegistry registry = new PrometheusRegistry();

    // Counter 1
    registry.register(
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(100)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        });

    // Counter 2: SAME labels, different value
    registry.register(
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(50)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        });

    // Scrape and write to text format
    MetricSnapshots snapshots = registry.scrape();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrometheusTextFormatWriter writer = PrometheusTextFormatWriter.create();
    writer.write(out, snapshots);
    String output = out.toString(UTF_8);

    System.out.println("=== Duplicate Names (Same Labels) Output ===");
    System.out.println(output);
    System.out.println("=== End Output ===\n");
    System.out.println("⚠️  WARNING: This produces duplicate time series with identical labels!");
    System.out.println("    Prometheus may only keep one value, which could be confusing.\n");

    // Verify both values appear in output
    assertThat(output).contains("api_responses_total{");
    assertThat(output).contains(" 100");
    assertThat(output).contains(" 50");

    // Both have identical label sets
    long matchingLines =
        output
            .lines()
            .filter(
                line ->
                    line.contains("api_responses_total{")
                        && line.contains("outcome=\"SUCCESS\"")
                        && line.contains("uri=\"/hello\""))
            .count();
    assertThat(matchingLines).isEqualTo(2);
  }

  @Test
  void testDuplicateNames_multipleDataPoints_producesValidOutput() throws IOException {
    PrometheusRegistry registry = new PrometheusRegistry();

    // Counter 1: Multiple data points
    registry.register(
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/hello", "outcome", "SUCCESS"))
                        .value(100)
                        .build())
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("uri", "/world", "outcome", "SUCCESS"))
                        .value(200)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        });

    // Counter 2: Multiple data points with additional error label
    registry.register(
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("api_responses")
                .help("API responses")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(
                            Labels.of("uri", "/hello", "outcome", "FAILURE", "error", "TIMEOUT"))
                        .value(10)
                        .build())
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(
                            Labels.of("uri", "/world", "outcome", "FAILURE", "error", "NOT_FOUND"))
                        .value(5)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "api_responses_total";
          }
        });

    // Scrape and write to text format
    MetricSnapshots snapshots = registry.scrape();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrometheusTextFormatWriter writer = PrometheusTextFormatWriter.create();
    writer.write(out, snapshots);
    String output = out.toString(UTF_8);

    System.out.println("=== Duplicate Names (Multiple Data Points) Output ===");
    System.out.println(output);
    System.out.println("=== End Output ===\n");

    long metricLines =
        output.lines().filter(line -> line.startsWith("api_responses_total{")).count();
    assertThat(metricLines).isEqualTo(4);

    assertThat(output).contains(" 100");
    assertThat(output).contains(" 200");
    assertThat(output).contains(" 10");
    assertThat(output).contains(" 5");
  }

  @Test
  void testBackwardCompatibility_strictModeWorksAsExpected() throws IOException {
    PrometheusRegistry registry = new PrometheusRegistry(); // Strict mode

    registry.register(
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("requests")
                .help("Request counter")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("method", "GET"))
                        .value(100)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "requests_total";
          }
        });

    registry.register(
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder()
                .name("active_requests")
                .help("Active requests gauge")
                .dataPoint(
                    GaugeSnapshot.GaugeDataPointSnapshot.builder()
                        .labels(Labels.of("method", "POST"))
                        .value(50)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "active_requests";
          }
        });

    MetricSnapshots snapshots = registry.scrape();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrometheusTextFormatWriter writer = PrometheusTextFormatWriter.create();
    writer.write(out, snapshots);
    String output = out.toString(UTF_8);

    System.out.println("=== Backward Compatibility (Strict Mode) Output ===");
    System.out.println(output);
    System.out.println("=== End Output ===\n");

    // Verify both metrics appear with unique names
    assertThat(output).contains("# TYPE requests_total counter");
    assertThat(output).contains("# TYPE active_requests gauge");
    assertThat(output).contains("requests_total{");
    assertThat(output).contains("active_requests{");
  }

  @Test
  void testOpenMetricsFormat_withDuplicateNames() throws IOException {
    PrometheusRegistry registry = getPrometheusRegistry();

    MetricSnapshots snapshots = registry.scrape();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(false, false);
    writer.write(out, snapshots);
    String output = out.toString(UTF_8);

    System.out.println("=== OpenMetrics Format with Duplicate Names ===");
    System.out.println(output);
    System.out.println("=== End Output ===\n");

    assertThat(output).contains("# TYPE api_responses counter");
    assertThat(output).contains("api_responses_total{");
    assertThat(output).contains("outcome=\"SUCCESS\"");
    assertThat(output).contains("outcome=\"FAILURE\"");
    assertThat(output).contains("# EOF");

    long metricLines =
        output.lines().filter(line -> line.startsWith("api_responses_total{")).count();
    assertThat(metricLines).isEqualTo(2);
  }
}
