package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class PrometheusPropertiesTest {

  @Test
  void testPrometheusConfig() {
    PrometheusProperties result = PrometheusProperties.get();
    assertThat(result.getDefaultMetricProperties().getHistogramClassicUpperBounds()).hasSize(11);
    assertThat(result.getMetricProperties("http_duration_seconds").getHistogramClassicUpperBounds())
        .hasSize(4);
  }

  @Test
  void testEmptyUpperBounds() throws IOException {
    Properties properties = new Properties();
    try (InputStream stream =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("emptyUpperBounds.properties")) {
      properties.load(stream);
    }
    assertThat(properties).hasSize(1);
    Map<Object, Object> regularProperties = new HashMap<>(properties);
    PropertySource propertySource = new PropertySource(regularProperties);
    MetricsProperties.load("io.prometheus.metrics", propertySource);
    assertThat(regularProperties).isEmpty();
  }

  @Test
  void testBuilder() {
    PrometheusProperties defaults = PrometheusPropertiesLoader.load(new HashMap<>());
    PrometheusProperties.Builder builder = PrometheusProperties.builder();
    builder.defaultMetricsProperties(defaults.getDefaultMetricProperties());
    builder.metricProperties(
        Collections.singletonMap(
            "http_duration_seconds",
            MetricsProperties.builder().histogramClassicUpperBounds(0.1, 0.2, 0.5, 1.0).build()));
    builder.exemplarProperties(defaults.getExemplarProperties());
    builder.defaultMetricsProperties(defaults.getDefaultMetricProperties());
    builder.exporterFilterProperties(defaults.getExporterFilterProperties());
    builder.exporterHttpServerProperties(defaults.getExporterHttpServerProperties());
    builder.exporterOpenTelemetryProperties(defaults.getExporterOpenTelemetryProperties());
    builder.pushgatewayProperties(defaults.getExporterPushgatewayProperties());
    builder.exporterProperties(defaults.getExporterProperties());
    PrometheusProperties result = builder.build();
    assertThat(result.getDefaultMetricProperties()).isSameAs(defaults.getDefaultMetricProperties());
    assertThat(result.getExemplarProperties()).isSameAs(defaults.getExemplarProperties());
    assertThat(result.getExporterFilterProperties())
        .isSameAs(defaults.getExporterFilterProperties());
    assertThat(result.getExporterHttpServerProperties())
        .isSameAs(defaults.getExporterHttpServerProperties());
    assertThat(result.getExporterOpenTelemetryProperties())
        .isSameAs(defaults.getExporterOpenTelemetryProperties());
    assertThat(result.getExporterPushgatewayProperties())
        .isSameAs(defaults.getExporterPushgatewayProperties());
    assertThat(result.getMetricProperties("http_duration_seconds"))
        .usingRecursiveComparison()
        .isEqualTo(
            MetricsProperties.builder().histogramClassicUpperBounds(0.1, 0.2, 0.5, 1.0).build());
    assertThat(result.getMetricProperties("unknown_metric")).isNull();
    assertThat(result.getExporterProperties()).isSameAs(defaults.getExporterProperties());
  }

  @Test
  void testMetricNameNormalization() {
    PrometheusProperties.Builder builder = PrometheusProperties.builder();
    MetricsProperties customProps =
        MetricsProperties.builder().histogramClassicUpperBounds(0.1, 0.5).build();

    // Test that metric names with dots are normalized to underscores
    builder.putMetricProperty("my.metric.name", customProps);
    PrometheusProperties result = builder.build();

    // Should be able to retrieve with dots
    assertThat(result.getMetricProperties("my.metric.name")).isSameAs(customProps);
    // Should also be able to retrieve with underscores
    assertThat(result.getMetricProperties("my_metric_name")).isSameAs(customProps);
  }

  @Test
  void testMetricNameWithInvalidCharacters() {
    PrometheusProperties.Builder builder = PrometheusProperties.builder();
    MetricsProperties customProps =
        MetricsProperties.builder().histogramClassicUpperBounds(0.1, 0.5).build();

    // Test that invalid characters are converted to underscores
    builder.putMetricProperty("metric-name@with#invalid$chars", customProps);
    PrometheusProperties result = builder.build();

    // Should normalize invalid characters to underscores
    assertThat(result.getMetricProperties("metric-name@with#invalid$chars")).isSameAs(customProps);
    assertThat(result.getMetricProperties("metric_name_with_invalid_chars")).isSameAs(customProps);
  }

  @Test
  void testMetricNameWithValidCharacters() {
    PrometheusProperties.Builder builder = PrometheusProperties.builder();
    MetricsProperties customProps =
        MetricsProperties.builder().histogramClassicUpperBounds(0.1, 0.5).build();

    // Test valid characters: letters, numbers (not at start), underscore, colon
    builder.putMetricProperty("my_metric:name123", customProps);
    PrometheusProperties result = builder.build();

    assertThat(result.getMetricProperties("my_metric:name123")).isSameAs(customProps);
  }

  @Test
  void testMetricNameStartingWithNumber() {
    PrometheusProperties.Builder builder = PrometheusProperties.builder();
    MetricsProperties customProps =
        MetricsProperties.builder().histogramClassicUpperBounds(0.1, 0.5).build();

    // First digit is invalid (i=0), but subsequent digits are valid (i>0)
    builder.putMetricProperty("123metric", customProps);
    PrometheusProperties result = builder.build();

    assertThat(result.getMetricProperties("123metric")).isSameAs(customProps);
    assertThat(result.getMetricProperties("_23metric")).isSameAs(customProps);
  }

  @Test
  void testOpenMetrics2ConsumerPattern() {
    PrometheusProperties config =
        PrometheusProperties.builder()
            .enableOpenMetrics2(om2 -> om2.contentNegotiation(true).compositeValues(false))
            .build();
    assertThat(config.getOpenMetrics2Properties().getContentNegotiation()).isTrue();
    assertThat(config.getOpenMetrics2Properties().getCompositeValues()).isFalse();
    assertThat(config.getOpenMetrics2Properties().getExemplarCompliance()).isFalse();
    assertThat(config.getOpenMetrics2Properties().getNativeHistograms()).isFalse();
  }

  @Test
  void testOpenMetrics2EnableAll() {
    PrometheusProperties config =
        PrometheusProperties.builder()
            .enableOpenMetrics2(OpenMetrics2Properties.Builder::enableAll)
            .build();
    assertThat(config.getOpenMetrics2Properties().getContentNegotiation()).isTrue();
    assertThat(config.getOpenMetrics2Properties().getCompositeValues()).isTrue();
    assertThat(config.getOpenMetrics2Properties().getExemplarCompliance()).isTrue();
    assertThat(config.getOpenMetrics2Properties().getNativeHistograms()).isTrue();
  }

  @Test
  void testOpenMetrics2DirectAssignment() {
    OpenMetrics2Properties om2Props =
        OpenMetrics2Properties.builder().contentNegotiation(true).nativeHistograms(true).build();
    PrometheusProperties config =
        PrometheusProperties.builder().openMetrics2Properties(om2Props).build();
    assertThat(config.getOpenMetrics2Properties().getContentNegotiation()).isTrue();
    assertThat(config.getOpenMetrics2Properties().getCompositeValues()).isFalse();
    assertThat(config.getOpenMetrics2Properties().getExemplarCompliance()).isFalse();
    assertThat(config.getOpenMetrics2Properties().getNativeHistograms()).isTrue();
  }

  @Test
  void testOpenMetrics2Defaults() {
    PrometheusProperties config = PrometheusProperties.builder().build();
    assertThat(config.getOpenMetrics2Properties().getContentNegotiation()).isFalse();
    assertThat(config.getOpenMetrics2Properties().getCompositeValues()).isFalse();
    assertThat(config.getOpenMetrics2Properties().getExemplarCompliance()).isFalse();
    assertThat(config.getOpenMetrics2Properties().getNativeHistograms()).isFalse();
  }

  @Test
  void testOpenMetrics2PropertiesLoading() {
    Map<Object, Object> properties = new HashMap<>();
    properties.put("io.prometheus.openmetrics2.content_negotiation", "true");
    properties.put("io.prometheus.openmetrics2.composite_values", "false");
    properties.put("io.prometheus.openmetrics2.exemplar_compliance", "true");
    properties.put("io.prometheus.openmetrics2.native_histograms", "false");
    PrometheusProperties config = PrometheusPropertiesLoader.load(properties);
    assertThat(config.getOpenMetrics2Properties().getContentNegotiation()).isTrue();
    assertThat(config.getOpenMetrics2Properties().getCompositeValues()).isFalse();
    assertThat(config.getOpenMetrics2Properties().getExemplarCompliance()).isTrue();
    assertThat(config.getOpenMetrics2Properties().getNativeHistograms()).isFalse();
  }
}
