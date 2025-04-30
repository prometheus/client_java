package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExporterPropertiesTest {

  @Test
  void load() {
    ExporterProperties properties =
        load(
            new HashMap<>(
                Map.of(
                    "io.prometheus.exporter.includeCreatedTimestamps", "true",
                    "io.prometheus.exporter.exemplarsOnAllMetricTypes", "true")));
    assertThat(properties.getIncludeCreatedTimestamps()).isTrue();
    assertThat(properties.getExemplarsOnAllMetricTypes()).isTrue();

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.exporter.includeCreatedTimestamps", "invalid"))))
        .withMessage(
            "io.prometheus.exporter.includeCreatedTimestamps: Expecting 'true' or 'false'. Found:"
                + " invalid");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.exporter.exemplarsOnAllMetricTypes", "invalid"))))
        .withMessage(
            "io.prometheus.exporter.exemplarsOnAllMetricTypes: Expecting 'true' or 'false'. Found:"
                + " invalid");
  }

  private static ExporterProperties load(Map<String, String> map) {
    return ExporterProperties.load(new HashMap<>(map));
  }

  @Test
  void builder() {
    ExporterProperties properties =
        ExporterProperties.builder()
            .includeCreatedTimestamps(true)
            .exemplarsOnAllMetricTypes(true)
            .prometheusTimestampsInMs(false)
            .build();
    assertThat(properties.getIncludeCreatedTimestamps()).isTrue();
    assertThat(properties.getExemplarsOnAllMetricTypes()).isTrue();
  }
}
