package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;

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
    return ExporterOpenTelemetryProperties.load(new HashMap<>(map));
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
}
