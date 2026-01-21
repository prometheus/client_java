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
    Map<String, String> props1 = new HashMap<>();
    props1.put("io.prometheus.exporter.httpServer.port", "1");
    ExporterHttpServerProperties properties = load(props1);
    assertThat(properties.getPort()).isOne();
    assertThat(properties.isPreferUncompressedResponse()).isFalse();

    Map<String, String> props2 = new HashMap<>();
    props2.put("io.prometheus.exporter.httpServer.port", "0");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(props2))
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
