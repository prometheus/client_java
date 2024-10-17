package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExporterPushgatewayPropertiesTest {

  @Test
  void load() {
    ExporterPushgatewayProperties properties =
        load(
            ImmutableMap.of(
                "io.prometheus.exporter.pushgateway.address", "http://localhost",
                "io.prometheus.exporter.pushgateway.job", "job",
                "io.prometheus.exporter.pushgateway.scheme", "http"));

    assertThat(properties.getAddress()).isEqualTo("http://localhost");
    assertThat(properties.getJob()).isEqualTo("job");
    assertThat(properties.getScheme()).isEqualTo("http");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(ImmutableMap.of("io.prometheus.exporter.pushgateway.scheme", "foo")))
        .withMessage(
            "io.prometheus.exporter.pushgateway.scheme: Illegal value. Expecting 'http' or 'https'. Found: foo");
  }

  private static ExporterPushgatewayProperties load(Map<String, String> map) {
    return ExporterPushgatewayProperties.load(new HashMap<>(map));
  }
}
