package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExemplarsPropertiesTest {

  @Test
  void load() {
    ExemplarsProperties properties =
        load(
            Map.of(
                "io.prometheus.exemplars.min_retention_period_seconds", "1",
                "io.prometheus.exemplars.max_retention_period_seconds", "2",
                "io.prometheus.exemplars.sample_interval_milliseconds", "3"));
    assertThat(properties.getMinRetentionPeriodSeconds()).isOne();
    assertThat(properties.getMaxRetentionPeriodSeconds()).isEqualTo(2);
    assertThat(properties.getSampleIntervalMilliseconds()).isEqualTo(3);

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () -> load(Map.of("io.prometheus.exemplars.min_retention_period_seconds", "-1")))
        .withMessage(
            "io.prometheus.exemplars.min_retention_period_seconds: Expecting value > 0. Found: -1");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(Map.of("io.prometheus.exemplars.max_retention_period_seconds", "0")))
        .withMessage(
            "io.prometheus.exemplars.max_retention_period_seconds: Expecting value > 0. Found: 0");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () -> load(Map.of("io.prometheus.exemplars.sample_interval_milliseconds", "-1")))
        .withMessage(
            "io.prometheus.exemplars.sample_interval_milliseconds: Expecting value > 0. Found: -1");
  }

  private static ExemplarsProperties load(Map<String, String> map) {
    return ExemplarsProperties.load(new HashMap<>(map));
  }

  @Test
  void builder() {
    ExemplarsProperties properties =
        ExemplarsProperties.builder()
            .minRetentionPeriodSeconds(1)
            .maxRetentionPeriodSeconds(2)
            .sampleIntervalMilliseconds(3)
            .build();
    assertThat(properties.getMinRetentionPeriodSeconds()).isOne();
    assertThat(properties.getMaxRetentionPeriodSeconds()).isEqualTo(2);
    assertThat(properties.getSampleIntervalMilliseconds()).isEqualTo(3);
  }
}
