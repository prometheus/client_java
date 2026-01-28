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
                    "io.prometheus.exporter.include_created_timestamps", "true",
                    "io.prometheus.exporter.exemplars_on_all_metric_types", "true")));
    assertThat(properties.getIncludeCreatedTimestamps()).isTrue();
    assertThat(properties.getExemplarsOnAllMetricTypes()).isTrue();

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.exporter.include_created_timestamps", "invalid"))))
        .withMessage(
            "io.prometheus.exporter.include_created_timestamps: Expecting 'true' or 'false'. Found:"
                + " invalid");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.exporter.exemplars_on_all_metric_types", "invalid"))))
        .withMessage(
            "io.prometheus.exporter.exemplars_on_all_metric_types: Expecting 'true' or 'false'. Found:"
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
