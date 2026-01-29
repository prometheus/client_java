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
    assertThat(result.getDefaultMetricProperties().getHistogramClassicUpperBounds()).hasSize(10);
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
}
