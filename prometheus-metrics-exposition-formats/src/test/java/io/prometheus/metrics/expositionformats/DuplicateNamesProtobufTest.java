package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_33_2.Metrics;
import io.prometheus.metrics.expositionformats.internal.PrometheusProtobufWriterImpl;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DuplicateNamesProtobufTest {

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
  void testDuplicateNames_differentLabels_producesSingleMetricFamily() throws IOException {
    PrometheusRegistry registry = getPrometheusRegistry();

    MetricSnapshots snapshots = registry.scrape();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrometheusProtobufWriterImpl writer = new PrometheusProtobufWriterImpl();
    writer.write(out, snapshots, EscapingScheme.UNDERSCORE_ESCAPING);

    List<Metrics.MetricFamily> metricFamilies = parseProtobufOutput(out);

    assertThat(metricFamilies).hasSize(1);
    Metrics.MetricFamily family = metricFamilies.get(0);
    assertThat(family.getName()).isEqualTo("api_responses_total");
    assertThat(family.getHelp()).isEqualTo("API responses");
    assertThat(family.getType()).isEqualTo(Metrics.MetricType.COUNTER);
    assertThat(family.getMetricCount()).isEqualTo(2);

    Metrics.Metric successMetric =
        family.getMetricList().stream()
            .filter(
                m ->
                    m.getLabelList().stream()
                        .anyMatch(
                            l -> l.getName().equals("outcome") && l.getValue().equals("SUCCESS")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("SUCCESS metric not found"));
    assertThat(successMetric.getCounter().getValue()).isEqualTo(100.0);

    Metrics.Metric failureMetric =
        family.getMetricList().stream()
            .filter(
                m ->
                    m.getLabelList().stream()
                            .anyMatch(
                                l ->
                                    l.getName().equals("outcome") && l.getValue().equals("FAILURE"))
                        && m.getLabelList().stream()
                            .anyMatch(
                                l -> l.getName().equals("error") && l.getValue().equals("TIMEOUT")))
            .findFirst()
            .orElseThrow(() -> new AssertionError("FAILURE metric not found"));
    assertThat(failureMetric.getCounter().getValue()).isEqualTo(10.0);
  }

  @Test
  void testDuplicateNames_multipleDataPoints_producesSingleMetricFamily() throws IOException {
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
    PrometheusProtobufWriterImpl writer = new PrometheusProtobufWriterImpl();
    writer.write(out, snapshots, EscapingScheme.UNDERSCORE_ESCAPING);

    List<Metrics.MetricFamily> metricFamilies = parseProtobufOutput(out);

    assertThat(metricFamilies).hasSize(1);
    Metrics.MetricFamily family = metricFamilies.get(0);
    assertThat(family.getName()).isEqualTo("api_responses_total");
    assertThat(family.getMetricCount()).isEqualTo(4);

    long successCount =
        family.getMetricList().stream()
            .filter(
                m ->
                    m.getLabelList().stream()
                        .anyMatch(
                            l -> l.getName().equals("outcome") && l.getValue().equals("SUCCESS")))
            .count();

    long failureCount =
        family.getMetricList().stream()
            .filter(
                m ->
                    m.getLabelList().stream()
                        .anyMatch(
                            l -> l.getName().equals("outcome") && l.getValue().equals("FAILURE")))
            .count();

    assertThat(successCount).isEqualTo(2);
    assertThat(failureCount).isEqualTo(2);
  }

  @Test
  void testDifferentMetrics_producesSeparateMetricFamilies() throws IOException {
    MetricSnapshots snapshots = getMetricSnapshots();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrometheusProtobufWriterImpl writer = new PrometheusProtobufWriterImpl();
    writer.write(out, snapshots, EscapingScheme.UNDERSCORE_ESCAPING);

    List<Metrics.MetricFamily> metricFamilies = parseProtobufOutput(out);

    assertThat(metricFamilies).hasSize(2);

    Metrics.MetricFamily counterFamily = null;
    Metrics.MetricFamily gaugeFamily = null;
    for (Metrics.MetricFamily family : metricFamilies) {
      if (family.getName().equals("http_requests_total")) {
        counterFamily = family;
      } else if (family.getName().equals("active_sessions")) {
        gaugeFamily = family;
      }
    }

    assertThat(counterFamily).isNotNull();
    assertThat(counterFamily.getType()).isEqualTo(Metrics.MetricType.COUNTER);
    assertThat(counterFamily.getMetricCount()).isEqualTo(1);
    assertThat(counterFamily.getMetric(0).getCounter().getValue()).isEqualTo(100.0);

    assertThat(gaugeFamily).isNotNull();
    assertThat(gaugeFamily.getType()).isEqualTo(Metrics.MetricType.GAUGE);
    assertThat(gaugeFamily.getMetricCount()).isEqualTo(1);
    assertThat(gaugeFamily.getMetric(0).getGauge().getValue()).isEqualTo(50.0);
  }

  private static MetricSnapshots getMetricSnapshots() {
    PrometheusRegistry registry = new PrometheusRegistry();

    registry.register(
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return CounterSnapshot.builder()
                .name("http_requests")
                .help("HTTP Request counter")
                .dataPoint(
                    CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("method", "GET"))
                        .value(100)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "http_requests_total";
          }
        });

    registry.register(
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder()
                .name("active_sessions")
                .help("Active sessions gauge")
                .dataPoint(
                    GaugeSnapshot.GaugeDataPointSnapshot.builder()
                        .labels(Labels.of("method", "POST"))
                        .value(50)
                        .build())
                .build();
          }

          @Override
          public String getPrometheusName() {
            return "active_sessions";
          }
        });

    return registry.scrape();
  }

  private List<Metrics.MetricFamily> parseProtobufOutput(ByteArrayOutputStream out)
      throws IOException {
    List<Metrics.MetricFamily> metricFamilies = new ArrayList<>();
    ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    while (in.available() > 0) {
      metricFamilies.add(Metrics.MetricFamily.parseDelimitedFrom(in));
    }
    return metricFamilies;
  }
}
