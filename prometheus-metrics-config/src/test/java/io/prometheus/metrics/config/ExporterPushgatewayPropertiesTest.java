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
    ExporterPushgatewayProperties properties =
        load(
            Map.of(
                "io.prometheus.exporter.pushgateway.address", "http://localhost",
                "io.prometheus.exporter.pushgateway.job", "job",
                "io.prometheus.exporter.pushgateway.scheme", "http"));

    assertThat(properties.getAddress()).isEqualTo("http://localhost");
    assertThat(properties.getJob()).isEqualTo("job");
    assertThat(properties.getScheme()).isEqualTo("http");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> load(Map.of("io.prometheus.exporter.pushgateway.scheme", "foo")))
        .withMessage(
            "io.prometheus.exporter.pushgateway.scheme: Illegal value. Expecting 'http' or 'https'."
                + " Found: foo");
  }

  @Test
  void loadWithHttps() {
    ExporterPushgatewayProperties properties =
        load(Map.of("io.prometheus.exporter.pushgateway.scheme", "https"));
    assertThat(properties.getScheme()).isEqualTo("https");
  }

  @Test
  void loadWithEscapingSchemes() {
    ExporterPushgatewayProperties properties =
        load(Map.of("io.prometheus.exporter.pushgateway.escaping_scheme", "allow-utf-8"));
    assertThat(properties.getEscapingScheme()).isEqualTo(EscapingScheme.ALLOW_UTF8);

    properties = load(Map.of("io.prometheus.exporter.pushgateway.escaping_scheme", "values"));
    assertThat(properties.getEscapingScheme()).isEqualTo(EscapingScheme.VALUE_ENCODING_ESCAPING);

    properties = load(Map.of("io.prometheus.exporter.pushgateway.escaping_scheme", "underscores"));
    assertThat(properties.getEscapingScheme()).isEqualTo(EscapingScheme.UNDERSCORE_ESCAPING);

    properties = load(Map.of("io.prometheus.exporter.pushgateway.escaping_scheme", "dots"));
    assertThat(properties.getEscapingScheme()).isEqualTo(EscapingScheme.DOTS_ESCAPING);
  }

  @Test
  void loadWithInvalidEscapingScheme() {
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () -> load(Map.of("io.prometheus.exporter.pushgateway.escaping_scheme", "invalid")))
        .withMessage(
            "io.prometheus.exporter.pushgateway.escaping_scheme: Illegal value. Expecting"
                + " 'allow-utf-8', 'values', 'underscores', or 'dots'. Found: invalid");
  }

  @Test
  void loadWithTimeouts() {
    ExporterPushgatewayProperties properties =
        load(
            Map.of(
                "io.prometheus.exporter.pushgateway.connect_timeout_seconds", "5",
                "io.prometheus.exporter.pushgateway.read_timeout_seconds", "10"));
    assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(5));
    assertThat(properties.getReadTimeout()).isEqualTo(Duration.ofSeconds(10));
  }

  private static ExporterPushgatewayProperties load(Map<String, String> map) {
    Map<Object, Object> regularProperties = new HashMap<>(map);
    PropertySource propertySource = new PropertySource(regularProperties);
    return ExporterPushgatewayProperties.load(propertySource);
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
