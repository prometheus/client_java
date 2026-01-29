package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;
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
        .isThrownBy(() -> PrometheusPropertiesLoader.load(new Properties()))
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
  void environmentVariablesShouldConfigureMetrics() {
    // Simulate environment variables as they would be loaded
    Map<Object, Object> envVarProperties = new HashMap<>();
    envVarProperties.put(
        "io_prometheus_metrics_http_duration_seconds_histogram_classic_upper_bounds",
        ".001, .005, .01");
    envVarProperties.put("io_prometheus_metrics_exemplars_enabled", "false");

    PropertySource propertySource =
        new PropertySource(new HashMap<>(), envVarProperties, new HashMap<>());
    Map<String, MetricsProperties> metricsConfigs =
        PrometheusPropertiesLoader.loadMetricsConfigs(propertySource);

    assertThat(metricsConfigs.get("http_duration_seconds").getHistogramClassicUpperBounds())
        .hasSize(3)
        .containsExactly(0.001, 0.005, 0.01);

    MetricsProperties defaultMetrics =
        MetricsProperties.load("io.prometheus.metrics", propertySource);
    assertThat(defaultMetrics.getExemplarsEnabled()).isFalse();
  }

  @Test
  void environmentVariablesShouldHandleSnakeCaseMetricNames() {
    // Simulate environment variable for metric with snake_case name
    Map<Object, Object> envVarProperties = new HashMap<>();
    envVarProperties.put("io_prometheus_metrics_http_server_histogram_native_only", "true");

    PropertySource propertySource =
        new PropertySource(new HashMap<>(), envVarProperties, new HashMap<>());
    Map<String, MetricsProperties> metricsConfigs =
        PrometheusPropertiesLoader.loadMetricsConfigs(propertySource);

    assertThat(metricsConfigs.get("http_server").getHistogramNativeOnly()).isTrue();
  }

  @Test
  void environmentVariablesShouldHandleMultipleSnakeCaseSegments() {
    // Simulate environment variable for metric with multiple snake_case segments
    Map<Object, Object> envVarProperties = new HashMap<>();
    envVarProperties.put("io_prometheus_metrics_my_custom_metric_histogram_native_only", "true");

    PropertySource propertySource =
        new PropertySource(new HashMap<>(), envVarProperties, new HashMap<>());
    Map<String, MetricsProperties> metricsConfigs =
        PrometheusPropertiesLoader.loadMetricsConfigs(propertySource);

    assertThat(metricsConfigs.get("my_custom_metric").getHistogramNativeOnly()).isTrue();
  }

  @Test
  void environmentVariablesShouldHandleMetricNamesContainingPropertyKeywords() {
    // Metric names can contain words like "summary" or "histogram"
    // This should not confuse the parser
    Map<Object, Object> envVarProperties = new HashMap<>();
    envVarProperties.put("io_prometheus_metrics_my_summary_metric_histogram_native_only", "true");
    envVarProperties.put(
        "io_prometheus_metrics_histogram_processor_summary_quantiles", "0.5, 0.95");

    PropertySource propertySource =
        new PropertySource(new HashMap<>(), envVarProperties, new HashMap<>());
    Map<String, MetricsProperties> metricsConfigs =
        PrometheusPropertiesLoader.loadMetricsConfigs(propertySource);

    assertThat(metricsConfigs.get("my_summary_metric").getHistogramNativeOnly()).isTrue();
    assertThat(metricsConfigs.get("histogram_processor").getSummaryQuantiles())
        .containsExactly(0.5, 0.95);
  }

  @Test
  void regularPropertiesShouldHandleComplexMetricNames() {
    // Test that suffix-based matching works correctly for regular properties
    // Metric names already use underscores (exposition format)
    Properties properties = new Properties();
    properties.setProperty(
        "io.prometheus.metrics.http_server_requests_total.histogram_native_only", "true");
    properties.setProperty(
        "io.prometheus.metrics.my_app_custom_metric.summary_quantiles", "0.5, 0.99");

    PropertySource propertySource = new PropertySource(properties);
    Map<String, MetricsProperties> metricsConfigs =
        PrometheusPropertiesLoader.loadMetricsConfigs(propertySource);

    assertThat(metricsConfigs.get("http_server_requests_total").getHistogramNativeOnly()).isTrue();
    assertThat(metricsConfigs.get("my_app_custom_metric").getSummaryQuantiles())
        .containsExactly(0.5, 0.99);
  }
}
