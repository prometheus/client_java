package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExporterOpenTelemetryPropertiesTest {

  @Test
  void load() {
    Map<String, String> props = new HashMap<>();
    props.put("io.prometheus.exporter.opentelemetry.protocol", "grpc");
    props.put("io.prometheus.exporter.opentelemetry.endpoint", "http://localhost:8080");
    props.put("io.prometheus.exporter.opentelemetry.headers", "key1=value1,key2=value2");
    props.put("io.prometheus.exporter.opentelemetry.intervalSeconds", "10");
    props.put("io.prometheus.exporter.opentelemetry.timeoutSeconds", "5");
    props.put("io.prometheus.exporter.opentelemetry.serviceName", "serviceName");
    props.put("io.prometheus.exporter.opentelemetry.serviceNamespace", "serviceNamespace");
    props.put("io.prometheus.exporter.opentelemetry.serviceInstanceId", "serviceInstanceId");
    props.put("io.prometheus.exporter.opentelemetry.serviceVersion", "serviceVersion");
    props.put("io.prometheus.exporter.opentelemetry.resourceAttributes", "key1=value1,key2=value2");
    ExporterOpenTelemetryProperties properties = load(props);

    assertValues(properties);
  }

  private static void assertValues(ExporterOpenTelemetryProperties properties) {
    Map<String, String> expectedHeaders = new HashMap<>();
    expectedHeaders.put("key1", "value1");
    expectedHeaders.put("key2", "value2");
    assertThat(properties.getProtocol()).isEqualTo("grpc");
    assertThat(properties.getEndpoint()).isEqualTo("http://localhost:8080");
    assertThat(properties.getHeaders()).containsExactlyInAnyOrderEntriesOf(expectedHeaders);
    assertThat(properties.getInterval()).isEqualTo("10s");
    assertThat(properties.getTimeout()).isEqualTo("5s");
    assertThat(properties.getServiceName()).isEqualTo("serviceName");
    assertThat(properties.getServiceNamespace()).isEqualTo("serviceNamespace");
    assertThat(properties.getServiceInstanceId()).isEqualTo("serviceInstanceId");
    assertThat(properties.getServiceVersion()).isEqualTo("serviceVersion");
    assertThat(properties.getResourceAttributes())
        .containsExactlyInAnyOrderEntriesOf(expectedHeaders);
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
