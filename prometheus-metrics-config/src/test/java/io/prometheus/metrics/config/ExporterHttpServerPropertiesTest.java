package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExporterHttpServerPropertiesTest {
  @Test
  void load() {
    ExporterHttpServerProperties properties =
        load(ImmutableMap.of("io.prometheus.exporter.httpServer.port", "1"));
    assertThat(properties.getPort()).isOne();

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(ImmutableMap.of("io.prometheus.exporter.httpServer.port", "0")))
        .withMessage("io.prometheus.exporter.httpServer.port: Expecting value > 0. Found: 0");
  }

  @Test
  void builder() {
    assertThat(ExporterHttpServerProperties.builder().port(1).build().getPort()).isOne();
  }

  private static ExporterHttpServerProperties load(Map<String, String> map) {
    return ExporterHttpServerProperties.load(new HashMap<>(map));
  }
}
