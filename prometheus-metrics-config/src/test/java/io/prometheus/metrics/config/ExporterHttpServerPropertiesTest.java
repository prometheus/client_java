package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExporterHttpServerPropertiesTest {
  @Test
  void load() {
    ExporterHttpServerProperties properties =
        load(Map.of("io.prometheus.exporter.httpServer.port", "1"));
    assertThat(properties.getPort()).isOne();
    assertThat(properties.isPreferUncompressedResponse()).isFalse();

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(Map.of("io.prometheus.exporter.httpServer.port", "0")))
        .withMessage("io.prometheus.exporter.httpServer.port: Expecting value > 0. Found: 0");
  }

  @Test
  void builder() {
    ExporterHttpServerProperties properties =
        ExporterHttpServerProperties.builder().port(1).build();

    assertSoftly(
        softly -> {
          softly.assertThat(properties.getPort()).isOne();
          softly.assertThat(properties.isPreferUncompressedResponse()).isFalse();
        });
  }

  private static ExporterHttpServerProperties load(Map<String, String> map) {
    return ExporterHttpServerProperties.load(new HashMap<>(map));
  }
}
