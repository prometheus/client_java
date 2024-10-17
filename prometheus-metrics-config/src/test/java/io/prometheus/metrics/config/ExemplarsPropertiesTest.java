package io.prometheus.metrics.config;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ExemplarsPropertiesTest {

  @Test
  void load() {
    ExemplarsProperties properties =
        load(
            ImmutableMap.of(
                "io.prometheus.exemplars.minRetentionPeriodSeconds", "1",
                "io.prometheus.exemplars.maxRetentionPeriodSeconds", "2",
                "io.prometheus.exemplars.sampleIntervalMilliseconds", "3"));
    assertThat(properties.getMinRetentionPeriodSeconds()).isOne();
    assertThat(properties.getMaxRetentionPeriodSeconds()).isEqualTo(2);
    assertThat(properties.getSampleIntervalMilliseconds()).isEqualTo(3);

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () -> load(ImmutableMap.of("io.prometheus.exemplars.minRetentionPeriodSeconds", "-1")))
        .withMessage(
            "io.prometheus.exemplars.minRetentionPeriodSeconds: Expecting value > 0. Found: -1");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () -> load(ImmutableMap.of("io.prometheus.exemplars.maxRetentionPeriodSeconds", "0")))
        .withMessage(
            "io.prometheus.exemplars.maxRetentionPeriodSeconds: Expecting value > 0. Found: 0");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () -> load(ImmutableMap.of("io.prometheus.exemplars.sampleIntervalMilliseconds", "-1")))
        .withMessage(
            "io.prometheus.exemplars.sampleIntervalMilliseconds: Expecting value > 0. Found: -1");
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
