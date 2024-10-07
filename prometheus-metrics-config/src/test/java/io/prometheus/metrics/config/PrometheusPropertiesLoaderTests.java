package io.prometheus.metrics.config;

import java.util.Properties;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests for {@link PrometheusPropertiesLoader}. */
public class PrometheusPropertiesLoaderTests {

  @Test
  public void propertiesShouldBeLoadedFromPropertiesFile() {
    PrometheusProperties prometheusProperties = PrometheusPropertiesLoader.load();
    assertThat(prometheusProperties.getDefaultMetricProperties().getHistogramClassicUpperBounds()).hasSize(11);
    assertThat(prometheusProperties
            .getMetricProperties("http_duration_seconds")
            .getHistogramClassicUpperBounds()).hasSize(4);
    assertThat(prometheusProperties.getExporterProperties().getExemplarsOnAllMetricTypes()).isTrue();
  }

  @Test
  public void externalPropertiesShouldOverridePropertiesFile() {
    Properties properties = new Properties();
    properties.setProperty("io.prometheus.metrics.histogramClassicUpperBounds", ".005, .01");
    properties.setProperty(
        "io.prometheus.metrics.http_duration_seconds.histogramClassicUpperBounds",
        ".005, .01, .015");
    properties.setProperty("io.prometheus.exporter.exemplarsOnAllMetricTypes", "false");

    PrometheusProperties prometheusProperties = PrometheusPropertiesLoader.load(properties);
    assertThat(prometheusProperties.getDefaultMetricProperties().getHistogramClassicUpperBounds()).hasSize(2);
    assertThat(prometheusProperties
            .getMetricProperties("http_duration_seconds")
            .getHistogramClassicUpperBounds()).hasSize(3);
    assertThat(prometheusProperties.getExporterProperties().getExemplarsOnAllMetricTypes()).isFalse();
  }
}
