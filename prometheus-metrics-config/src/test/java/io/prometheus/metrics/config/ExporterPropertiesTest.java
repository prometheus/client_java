package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExporterPropertiesTest {

  @Test
  void load() {
    Map<String, String> props1 = new HashMap<>();
    props1.put("io.prometheus.exporter.includeCreatedTimestamps", "true");
    props1.put("io.prometheus.exporter.exemplarsOnAllMetricTypes", "true");
    ExporterProperties properties = load(props1);
    assertThat(properties.getIncludeCreatedTimestamps()).isTrue();
    assertThat(properties.getExemplarsOnAllMetricTypes()).isTrue();

    Map<String, String> props2 = new HashMap<>();
    props2.put("io.prometheus.exporter.includeCreatedTimestamps", "invalid");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(props2))
        .withMessage(
            "io.prometheus.exporter.includeCreatedTimestamps: Expecting 'true' or 'false'. Found:"
                + " invalid");
    Map<String, String> props3 = new HashMap<>();
    props3.put("io.prometheus.exporter.exemplarsOnAllMetricTypes", "invalid");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(props3))
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
