package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExporterOpenTelemetryPropertiesTest {

  @Test
  void load() {
    ExporterOpenTelemetryProperties properties =
        load(
            Map.of(
                "io.prometheus.exporter.opentelemetry.protocol", "grpc",
                "io.prometheus.exporter.opentelemetry.endpoint", "http://localhost:8080",
                "io.prometheus.exporter.opentelemetry.headers", "key1=value1,key2=value2",
                "io.prometheus.exporter.opentelemetry.interval_seconds", "10",
                "io.prometheus.exporter.opentelemetry.timeout_seconds", "5",
                "io.prometheus.exporter.opentelemetry.service_name", "serviceName",
                "io.prometheus.exporter.opentelemetry.service_namespace", "serviceNamespace",
                "io.prometheus.exporter.opentelemetry.service_instance_id", "serviceInstanceId",
                "io.prometheus.exporter.opentelemetry.service_version", "serviceVersion",
                "io.prometheus.exporter.opentelemetry.resource_attributes",
                    "key1=value1,key2=value2"));

    assertValues(properties);
  }

  private static void assertValues(ExporterOpenTelemetryProperties properties) {
    assertThat(properties.getProtocol()).isEqualTo("grpc");
    assertThat(properties.getEndpoint()).isEqualTo("http://localhost:8080");
    assertThat(properties.getHeaders())
        .containsExactlyInAnyOrderEntriesOf(Map.of("key1", "value1", "key2", "value2"));
    assertThat(properties.getInterval()).isEqualTo("10s");
    assertThat(properties.getTimeout()).isEqualTo("5s");
    assertThat(properties.getServiceName()).isEqualTo("serviceName");
    assertThat(properties.getServiceNamespace()).isEqualTo("serviceNamespace");
    assertThat(properties.getServiceInstanceId()).isEqualTo("serviceInstanceId");
    assertThat(properties.getServiceVersion()).isEqualTo("serviceVersion");
    assertThat(properties.getResourceAttributes())
        .containsExactlyInAnyOrderEntriesOf(Map.of("key1", "value1", "key2", "value2"));
  }

  private static ExporterOpenTelemetryProperties load(Map<String, String> map) {
    Map<Object, Object> regularProperties = new HashMap<>(map);
    PropertySource propertySource = new PropertySource(regularProperties);
    return ExporterOpenTelemetryProperties.load(propertySource);
  }

  @Test
  void builder() {
    ExporterOpenTelemetryProperties properties =
        ExporterOpenTelemetryProperties.builder()
            .protocol("grpc")
            .endpoint("http://localhost:8080")
            .header("key1", "value1")
            .header("key2", "value2")
            .intervalSeconds(10)
            .timeoutSeconds(5)
            .serviceName("serviceName")
            .serviceNamespace("serviceNamespace")
            .serviceInstanceId("serviceInstanceId")
            .serviceVersion("serviceVersion")
            .resourceAttribute("key1", "value1")
            .resourceAttribute("key2", "value2")
            .build();
    assertValues(properties);
  }

  @Test
  void builderWithHttpProtobuf() {
    ExporterOpenTelemetryProperties properties =
        ExporterOpenTelemetryProperties.builder().protocol("http/protobuf").build();
    assertThat(properties.getProtocol()).isEqualTo("http/protobuf");
  }

  @Test
  void builderWithInvalidProtocol() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> ExporterOpenTelemetryProperties.builder().protocol("invalid"))
        .withMessage("invalid: Unsupported protocol. Expecting grpc or http/protobuf");
  }

  @Test
  void builderWithInvalidIntervalSeconds() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> ExporterOpenTelemetryProperties.builder().intervalSeconds(0))
        .withMessage("0: Expecting intervalSeconds > 0");

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> ExporterOpenTelemetryProperties.builder().intervalSeconds(-1))
        .withMessage("-1: Expecting intervalSeconds > 0");
  }

  @Test
  void builderWithInvalidTimeoutSeconds() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> ExporterOpenTelemetryProperties.builder().timeoutSeconds(0))
        .withMessage("0: Expecting timeoutSeconds > 0");

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> ExporterOpenTelemetryProperties.builder().timeoutSeconds(-1))
        .withMessage("-1: Expecting timeoutSeconds > 0");
  }
}
