package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExporterPushgatewayPropertiesTest {

  @Test
  void load() {
    Map<String, String> props1 = new HashMap<>();
    props1.put("io.prometheus.exporter.pushgateway.address", "http://localhost");
    props1.put("io.prometheus.exporter.pushgateway.job", "job");
    props1.put("io.prometheus.exporter.pushgateway.scheme", "http");
    ExporterPushgatewayProperties properties = load(props1);

    assertThat(properties.getAddress()).isEqualTo("http://localhost");
    assertThat(properties.getJob()).isEqualTo("job");
    assertThat(properties.getScheme()).isEqualTo("http");

    Map<String, String> props2 = new HashMap<>();
    props2.put("io.prometheus.exporter.pushgateway.scheme", "foo");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(props2))
        .withMessage(
            "io.prometheus.exporter.pushgateway.scheme: Illegal value. Expecting 'http' or 'https'."
                + " Found: foo");
  }

  private static ExporterPushgatewayProperties load(Map<String, String> map) {
    return ExporterPushgatewayProperties.load(new HashMap<>(map));
  }

  @Test
  void builder() {
    ExporterPushgatewayProperties properties =
        ExporterPushgatewayProperties.builder()
            .address("http://localhost")
            .job("job")
            .scheme("http")
            .escapingScheme(EscapingScheme.DOTS_ESCAPING)
            .connectTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(2))
            .build();

    assertThat(properties.getAddress()).isEqualTo("http://localhost");
    assertThat(properties.getJob()).isEqualTo("job");
    assertThat(properties.getScheme()).isEqualTo("http");
    assertThat(properties.getEscapingScheme()).isEqualTo(EscapingScheme.DOTS_ESCAPING);
    assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(1));
    assertThat(properties.getReadTimeout()).isEqualTo(Duration.ofSeconds(2));
  }
}
