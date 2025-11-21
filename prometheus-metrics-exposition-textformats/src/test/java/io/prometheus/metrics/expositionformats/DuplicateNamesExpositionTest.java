package io.prometheus.metrics.expositionformats;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    String expected = """
      # HELP api_responses_total API responses
      # TYPE api_responses_total counter
      api_responses_total{error="TIMEOUT",outcome="FAILURE",uri="/hello"} 10.0
      api_responses_total{outcome="SUCCESS",uri="/hello"} 100.0
      """;

    assertThat(output).isEqualTo(expected);
  }

  @Test
  void testDuplicateNames_multipleDataPoints_producesValidOutput() throws IOException {
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

    MetricSnapshots snapshots = registry.scrape();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrometheusTextFormatWriter writer = PrometheusTextFormatWriter.create();
    writer.write(out, snapshots);
    String output = out.toString(UTF_8);

    String expected = """
      # HELP api_responses_total API responses
      # TYPE api_responses_total counter
      api_responses_total{error="NOT_FOUND",outcome="FAILURE",uri="/world"} 5.0
      api_responses_total{error="TIMEOUT",outcome="FAILURE",uri="/hello"} 10.0
      api_responses_total{outcome="SUCCESS",uri="/hello"} 100.0
      api_responses_total{outcome="SUCCESS",uri="/world"} 200.0
      """;
    assertThat(output).isEqualTo(expected);

  }

  @Test
  void testOpenMetricsFormat_withDuplicateNames() throws IOException {
    PrometheusRegistry registry = getPrometheusRegistry();

    MetricSnapshots snapshots = registry.scrape();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OpenMetricsTextFormatWriter writer = new OpenMetricsTextFormatWriter(false, false);
    writer.write(out, snapshots);
    String output = out.toString(UTF_8);

    String expected = """
      # TYPE api_responses counter
      # HELP api_responses API responses
      api_responses_total{error="TIMEOUT",outcome="FAILURE",uri="/hello"} 10.0
      api_responses_total{outcome="SUCCESS",uri="/hello"} 100.0
      # EOF
      """;
    assertThat(output).isEqualTo(expected);
  }

  @Test
  void testDuplicateNames_sameLabels_throwsException() {
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

    // Scraping should throw exception due to duplicate time series (same name + same labels)
    assertThatThrownBy(registry::scrape)
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Duplicate labels detected")
      .hasMessageContaining("api_responses");
  }
}
