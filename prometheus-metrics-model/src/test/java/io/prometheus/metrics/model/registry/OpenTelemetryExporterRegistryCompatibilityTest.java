package io.prometheus.metrics.model.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * Tests that use the Prometheus registry in the same way as the OpenTelemetry Java SDK Prometheus
 * exporter ({@code io.opentelemetry.exporter.prometheus}). The SDK's {@code PrometheusMetricReader}
 * implements {@link MultiCollector} with default implementations for all optional methods: {@link
 * MultiCollector#getPrometheusNames()} returns an empty list, and {@link
 * MultiCollector#getMetricType(String)}, {@link MultiCollector#getLabelNames(String)}, and {@link
 * MultiCollector#getMetadata(String)} return null. This test suite ensures that registration,
 * scrape, and unregister continue to work for that usage pattern and that a shared registry with
 * both SDK-style and validated collectors behaves correctly.
 */
class OpenTelemetryExporterRegistryCompatibilityTest {

  /**
   * A MultiCollector that mimics the OpenTelemetry Java SDK's PrometheusMetricReader: it does not
   * override getPrometheusNames() (empty list), getMetricType(String), getLabelNames(String), or
   * getMetadata(String) (all null). Only collect() is implemented and returns MetricSnapshots.
   */
  private static final MultiCollector OTEL_STYLE_MULTI_COLLECTOR =
      new MultiCollector() {
        @Override
        public MetricSnapshots collect() {
          return new MetricSnapshots(
              CounterSnapshot.builder()
                  .name("otel_metric")
                  .help("A metric produced by an OTel-style converter")
                  .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(42.0).build())
                  .build());
        }
      };

  @Test
  void registerOtelStyleMultiCollector_succeeds() {
    PrometheusRegistry registry = new PrometheusRegistry();

    assertThatCode(() -> registry.register(OTEL_STYLE_MULTI_COLLECTOR)).doesNotThrowAnyException();
  }

  @Test
  void scrape_afterRegisteringOtelStyleMultiCollector_returnsSnapshotsFromCollector() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(OTEL_STYLE_MULTI_COLLECTOR);

    MetricSnapshots snapshots = registry.scrape();

    assertThat(snapshots).hasSize(1);
    MetricSnapshot snapshot = snapshots.get(0);
    assertThat(snapshot.getMetadata().getPrometheusName()).isEqualTo("otel_metric");
  }

  @Test
  void unregisterOtelStyleMultiCollector_succeedsAndScrapeNoLongerIncludesIt() {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(OTEL_STYLE_MULTI_COLLECTOR);

    assertThat(registry.scrape()).hasSize(1);

    assertThatCode(() -> registry.unregister(OTEL_STYLE_MULTI_COLLECTOR))
        .doesNotThrowAnyException();

    assertThat(registry.scrape()).isEmpty();
  }

  @Test
  void sharedRegistry_otelStyleMultiCollectorAndValidatedCollector_bothParticipateInScrape() {
    PrometheusRegistry registry = new PrometheusRegistry();

    Collector validatedCollector =
        new Collector() {
          @Override
          public MetricSnapshot collect() {
            return GaugeSnapshot.builder().name("app_gauge").help("App gauge").build();
          }

          @Override
          public String getPrometheusName() {
            return "app_gauge";
          }

          @Override
          public MetricType getMetricType() {
            return MetricType.GAUGE;
          }

          @Override
          public java.util.Set<String> getLabelNames() {
            return Collections.emptySet();
          }
        };

    registry.register(validatedCollector);
    registry.register(OTEL_STYLE_MULTI_COLLECTOR);

    MetricSnapshots snapshots = registry.scrape();

    assertThat(snapshots).hasSize(2);
    assertThat(snapshots)
        .extracting(s -> s.getMetadata().getPrometheusName())
        .containsExactlyInAnyOrder("app_gauge", "otel_metric");

    registry.unregister(OTEL_STYLE_MULTI_COLLECTOR);
    assertThat(registry.scrape()).hasSize(1);
    assertThat(registry.scrape().get(0).getMetadata().getPrometheusName()).isEqualTo("app_gauge");
  }
}
