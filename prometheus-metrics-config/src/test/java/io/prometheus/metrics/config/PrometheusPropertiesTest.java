package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class PrometheusPropertiesTest {

  @Test
  public void testPrometheusConfig() {
    PrometheusProperties result = PrometheusProperties.get();
    assertThat(result.getDefaultMetricProperties().getHistogramClassicUpperBounds()).hasSize(11);
    assertThat(result.getMetricProperties("http_duration_seconds").getHistogramClassicUpperBounds())
        .hasSize(4);
  }

  @Test
  public void testEmptyUpperBounds() throws IOException {
    Properties properties = new Properties();
    try (InputStream stream =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("emptyUpperBounds.properties")) {
      properties.load(stream);
    }
    assertThat(properties).hasSize(1);
    MetricsProperties.load("io.prometheus.metrics", properties);
    assertThat(properties).isEmpty();
  }

  @Test
  public void testBuilder() {
    PrometheusProperties defaults = PrometheusPropertiesLoader.load(new HashMap<>());
    PrometheusProperties.Builder builder = PrometheusProperties.builder();
    builder.defaultMetricsProperties(defaults.getDefaultMetricProperties());
    builder.exemplarProperties(defaults.getExemplarProperties());
    builder.defaultMetricsProperties(defaults.getDefaultMetricProperties());
    builder.exporterFilterProperties(defaults.getExporterFilterProperties());
    builder.exporterHttpServerProperties(defaults.getExporterHttpServerProperties());
    builder.exporterOpenTelemetryProperties(defaults.getExporterOpenTelemetryProperties());
    builder.pushgatewayProperties(defaults.getExporterPushgatewayProperties());
    PrometheusProperties result = builder.build();
    assertThat(result.getDefaultMetricProperties()).isSameAs(defaults.getDefaultMetricProperties());
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
  }
}
