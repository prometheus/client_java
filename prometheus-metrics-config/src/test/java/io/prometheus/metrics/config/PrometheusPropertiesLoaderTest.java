package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

/** Tests for {@link PrometheusPropertiesLoader}. */
class PrometheusPropertiesLoaderTest {

  @Test
  void propertiesShouldBeLoadedFromPropertiesFile() {
    PrometheusProperties prometheusProperties = PrometheusPropertiesLoader.load();
    assertThat(prometheusProperties.getDefaultMetricProperties().getHistogramClassicUpperBounds())
        .hasSize(11);
    assertThat(
            prometheusProperties
                .getMetricProperties("http_duration_seconds")
                .getHistogramClassicUpperBounds())
        .hasSize(4);
    assertThat(prometheusProperties.getExporterProperties().getExemplarsOnAllMetricTypes())
        .isTrue();
  }

  @Test
  @SetSystemProperty(key = "prometheus.config", value = "nonexistent.properties")
  void cantLoadPropertiesFile() {
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () -> {
              PrometheusPropertiesLoader.load(new Properties());
            })
        .withMessage(
            "Failed to read Prometheus properties from nonexistent.properties:"
                + " nonexistent.properties");
  }

  @Test
  void externalPropertiesShouldOverridePropertiesFile() {
    Properties properties = new Properties();
    properties.setProperty("io.prometheus.metrics.histogram_classic_upper_bounds", ".005, .01");
    properties.setProperty(
        "io.prometheus.metrics.http_duration_seconds.histogram_classic_upper_bounds",
        ".005, .01, .015");
    properties.setProperty("io.prometheus.exporter.exemplars_on_all_metric_types", "false");

    PrometheusProperties prometheusProperties = PrometheusPropertiesLoader.load(properties);
    assertThat(prometheusProperties.getDefaultMetricProperties().getHistogramClassicUpperBounds())
        .hasSize(2);
    assertThat(
            prometheusProperties
                .getMetricProperties("http_duration_seconds")
                .getHistogramClassicUpperBounds())
        .hasSize(3);
    assertThat(prometheusProperties.getExporterProperties().getExemplarsOnAllMetricTypes())
        .isFalse();
  }

  @Test
  void normalizeEnvironmentVariableKey_simpleMetricsProperty() {
    String result =
        PrometheusPropertiesLoader.normalizeEnvironmentVariableKey(
            "IO_PROMETHEUS_METRICS_EXEMPLARS_ENABLED");
    assertThat(result).isEqualTo("io.prometheus.metrics.exemplars_enabled");
  }

  @Test
  void normalizeEnvironmentVariableKey_exporterProperty() {
    String result =
        PrometheusPropertiesLoader.normalizeEnvironmentVariableKey(
            "IO_PROMETHEUS_EXPORTER_EXEMPLARS_ON_ALL_METRIC_TYPES");
    assertThat(result).isEqualTo("io.prometheus.exporter.exemplars_on_all_metric_types");
  }

  @Test
  void normalizeEnvironmentVariableKey_histogramBounds() {
    String result =
        PrometheusPropertiesLoader.normalizeEnvironmentVariableKey(
            "IO_PROMETHEUS_METRICS_HISTOGRAM_CLASSIC_UPPER_BOUNDS");
    assertThat(result).isEqualTo("io.prometheus.metrics.histogram_classic_upper_bounds");
  }

  @Test
  void normalizeEnvironmentVariableKey_openTelemetryProperty() {
    String result =
        PrometheusPropertiesLoader.normalizeEnvironmentVariableKey(
            "IO_PROMETHEUS_EXPORTER_OPENTELEMETRY_ENDPOINT");
    assertThat(result).isEqualTo("io.prometheus.exporter.opentelemetry.endpoint");
  }

  @Test
  void normalizeEnvironmentVariableKey_filterProperty() {
    String result =
        PrometheusPropertiesLoader.normalizeEnvironmentVariableKey(
            "IO_PROMETHEUS_EXPORTER_FILTER_ALLOWED_METRIC_NAMES");
    assertThat(result).isEqualTo("io.prometheus.exporter.filter.allowed_metric_names");
  }
}
