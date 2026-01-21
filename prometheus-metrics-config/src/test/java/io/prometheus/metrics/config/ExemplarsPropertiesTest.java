package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExemplarsPropertiesTest {

  @Test
  void load() {
    Map<String, String> props1 = new HashMap<>();
    props1.put("io.prometheus.exemplars.minRetentionPeriodSeconds", "1");
    props1.put("io.prometheus.exemplars.maxRetentionPeriodSeconds", "2");
    props1.put("io.prometheus.exemplars.sampleIntervalMilliseconds", "3");
    ExemplarsProperties properties = load(props1);
    assertThat(properties.getMinRetentionPeriodSeconds()).isOne();
    assertThat(properties.getMaxRetentionPeriodSeconds()).isEqualTo(2);
    assertThat(properties.getSampleIntervalMilliseconds()).isEqualTo(3);

    Map<String, String> props2 = new HashMap<>();
    props2.put("io.prometheus.exemplars.minRetentionPeriodSeconds", "-1");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(props2))
        .withMessage(
            "io.prometheus.exemplars.minRetentionPeriodSeconds: Expecting value > 0. Found: -1");

    Map<String, String> props3 = new HashMap<>();
    props3.put("io.prometheus.exemplars.maxRetentionPeriodSeconds", "0");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(props3))
        .withMessage(
            "io.prometheus.exemplars.maxRetentionPeriodSeconds: Expecting value > 0. Found: 0");

    Map<String, String> props4 = new HashMap<>();
    props4.put("io.prometheus.exemplars.sampleIntervalMilliseconds", "-1");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(props4))
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
