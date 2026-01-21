package io.prometheus.metrics.exporter.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.AutoConfigureUtil;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.prometheus.metrics.config.ExporterOpenTelemetryProperties;
import io.prometheus.metrics.config.PrometheusPropertiesLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OtelAutoConfigTest {

  static class TestCase {
    Map<String, String> systemProperties = new HashMap<>();
    Map<String, Optional<String>> expectedProperties = Collections.emptyMap();
    Map<String, String> expectedResourceAttributes = Collections.emptyMap();
    Consumer<OpenTelemetryExporter.Builder> exporterBuilder;
    Consumer<ExporterOpenTelemetryProperties.Builder> propertiesBuilder;

    public TestCase() {}

    public TestCase expectedProperties(Map<String, Optional<String>> expectedProperties) {
      this.expectedProperties = expectedProperties;
      return this;
    }

    public TestCase expectedResourceAttributes(Map<String, String> expectedResourceAttributes) {
      this.expectedResourceAttributes = expectedResourceAttributes;
      return this;
    }

    public TestCase systemProperties(Map<String, String> systemProperties) {
      this.systemProperties.putAll(systemProperties);
      return this;
    }

    public TestCase exporterBuilder(Consumer<OpenTelemetryExporter.Builder> exporterBuilder) {
      this.exporterBuilder = exporterBuilder;
      return this;
    }

    public TestCase propertiesBuilder(
        Consumer<ExporterOpenTelemetryProperties.Builder> propertiesBuilder) {
      this.propertiesBuilder = propertiesBuilder;
      return this;
    }
  }

  public static Stream<Arguments> testCases() {
    return Stream.of(
        Arguments.of(
            "values from builder",
            new TestCase()
                .expectedProperties(
                    ImmutableMap.of(
                        "otel.exporter.otlp.protocol",
                        Optional.of("http/protobuf"),
                        "otel.exporter.otlp.endpoint",
                        Optional.of("http://builder:4318"),
                        "otel.exporter.otlp.headers",
                        Optional.of("h=builder-v"),
                        "otel.metric.export.interval",
                        Optional.of("2s"),
                        "otel.exporter.otlp.timeout",
                        Optional.of("3s"),
                        "otel.service.name",
                        Optional.of("builder-service")))
                .expectedResourceAttributes(
                    ImmutableMap.of(
                        "key",
                        "builder-value",
                        "service.name",
                        "builder-service",
                        "service.namespace",
                        "builder-namespace",
                        "service.instance.id",
                        "builder-instance",
                        "service.version",
                        "builder-version"))
                .exporterBuilder(OtelAutoConfigTest::setBuilderValues)),
        Arguments.of(
            "builder endpoint with path",
            new TestCase()
                .expectedProperties(
                    ImmutableMap.of(
                        "otel.exporter.otlp.endpoint", Optional.of("http://builder:4318/")))
                .exporterBuilder(builder -> builder.endpoint("http://builder:4318/v1/metrics"))),
        Arguments.of(
            "values from otel have precedence over builder",
            new TestCase()
                .expectedProperties(
                    ImmutableMap.<String, Optional<String>>builder()
                        .put("otel.exporter.otlp.protocol", Optional.of("grpc"))
                        .put("otel.exporter.otlp.metrics.protocol", Optional.empty())
                        .put("otel.exporter.otlp.endpoint", Optional.of("http://otel:4317"))
                        .put("otel.exporter.otlp.metrics.endpoint", Optional.empty())
                        .put("otel.exporter.otlp.headers", Optional.of("h=otel-v"))
                        .put("otel.exporter.otlp.metrics.headers", Optional.empty())
                        .put("otel.metric.export.interval", Optional.of("12s"))
                        .put("otel.exporter.otlp.timeout", Optional.of("13s"))
                        .put("otel.exporter.otlp.metrics.timeout", Optional.empty())
                        .put("otel.service.name", Optional.of("otel-service"))
                        .build())
                .expectedResourceAttributes(
                    ImmutableMap.of(
                        "key",
                        "otel-value",
                        "service.name",
                        "otel-service",
                        "service.namespace",
                        "otel-namespace",
                        "service.instance.id",
                        "otel-instance",
                        "service.version",
                        "otel-version"))
                .exporterBuilder(OtelAutoConfigTest::setBuilderValues)
                .systemProperties(otelOverrides())),
        Arguments.of(
            "values from prom properties have precedence over builder and otel",
            new TestCase()
                .expectedProperties(
                    ImmutableMap.<String, Optional<String>>builder()
                        .put("otel.exporter.otlp.metrics.protocol", Optional.of("http/protobuf"))
                        .put("otel.exporter.otlp.protocol", Optional.of("grpc"))
                        .put("otel.exporter.otlp.metrics.endpoint", Optional.of("http://prom:4317"))
                        .put("otel.exporter.otlp.endpoint", Optional.of("http://otel:4317"))
                        .put("otel.exporter.otlp.metrics.headers", Optional.of("h=prom-v"))
                        .put("otel.exporter.otlp.headers", Optional.of("h=otel-v"))
                        .put("otel.metric.export.interval", Optional.of("22s"))
                        .put("otel.exporter.otlp.metrics.timeout", Optional.of("23s"))
                        .put("otel.exporter.otlp.timeout", Optional.of("13s"))
                        .put("otel.service.name", Optional.of("prom-service"))
                        .build())
                .expectedResourceAttributes(
                    ImmutableMap.of(
                        "key",
                        "prom-value",
                        "service.name",
                        "prom-service",
                        "service.namespace",
                        "prom-namespace",
                        "service.instance.id",
                        "prom-instance",
                        "service.version",
                        "prom-version"))
                .exporterBuilder(OtelAutoConfigTest::setBuilderValues)
                .systemProperties(otelOverrides())
                .systemProperties(
                    ImmutableMap.<String, String>builder()
                        .put("io.prometheus.exporter.opentelemetry.protocol", "http/protobuf")
                        .put("io.prometheus.exporter.opentelemetry.endpoint", "http://prom:4317")
                        .put("io.prometheus.exporter.opentelemetry.headers", "h=prom-v")
                        .put("io.prometheus.exporter.opentelemetry.intervalSeconds", "22")
                        .put("io.prometheus.exporter.opentelemetry.timeoutSeconds", "23")
                        .put("io.prometheus.exporter.opentelemetry.serviceName", "prom-service")
                        .put(
                            "io.prometheus.exporter.opentelemetry.serviceNamespace",
                            "prom-namespace")
                        .put(
                            "io.prometheus.exporter.opentelemetry.serviceInstanceId",
                            "prom-instance")
                        .put("io.prometheus.exporter.opentelemetry.serviceVersion", "prom-version")
                        .put(
                            "io.prometheus.exporter.opentelemetry.resourceAttributes",
                            "key=prom-value")
                        .build())),
        Arguments.of(
            "values from prom properties builder have precedence over builder and otel",
            new TestCase()
                .expectedProperties(
                    ImmutableMap.<String, Optional<String>>builder()
                        .put("otel.exporter.otlp.metrics.protocol", Optional.of("http/protobuf"))
                        .put("otel.exporter.otlp.protocol", Optional.of("grpc"))
                        .put("otel.exporter.otlp.metrics.endpoint", Optional.of("http://prom:4317"))
                        .put("otel.exporter.otlp.endpoint", Optional.of("http://otel:4317"))
                        .put("otel.exporter.otlp.metrics.headers", Optional.of("h=prom-v"))
                        .put("otel.exporter.otlp.headers", Optional.of("h=otel-v"))
                        .put("otel.metric.export.interval", Optional.of("22s"))
                        .put("otel.exporter.otlp.metrics.timeout", Optional.of("23s"))
                        .put("otel.exporter.otlp.timeout", Optional.of("13s"))
                        .put("otel.service.name", Optional.of("prom-service"))
                        .build())
                .expectedResourceAttributes(
                    ImmutableMap.of(
                        "key",
                        "prom-value",
                        "service.name",
                        "prom-service",
                        "service.namespace",
                        "prom-namespace",
                        "service.instance.id",
                        "prom-instance",
                        "service.version",
                        "prom-version"))
                .exporterBuilder(OtelAutoConfigTest::setBuilderValues)
                .systemProperties(otelOverrides())
                .propertiesBuilder(
                    builder ->
                        builder
                            .protocol("http/protobuf")
                            .endpoint("http://prom:4317")
                            .header("h", "prom-v")
                            .intervalSeconds(22)
                            .timeoutSeconds(23)
                            .serviceName("prom-service")
                            .serviceNamespace("prom-namespace")
                            .serviceInstanceId("prom-instance")
                            .serviceVersion("prom-version")
                            .resourceAttribute("key", "prom-value"))));
  }

  private static ImmutableMap<String, String> otelOverrides() {
    return ImmutableMap.<String, String>builder()
        .put("otel.exporter.otlp.protocol", "grpc")
        .put("otel.exporter.otlp.endpoint", "http://otel:4317")
        .put("otel.exporter.otlp.headers", "h=otel-v")
        .put("otel.metric.export.interval", "12s")
        .put("otel.exporter.otlp.timeout", "13s")
        .put("otel.service.name", "otel-service")
        .put(
            "otel.resource.attributes",
            "key=otel-value,service.namespace=otel-namespace,service.instance.id=otel-instance,service.version=otel-version")
        .build();
  }

  private static void setBuilderValues(OpenTelemetryExporter.Builder builder) {
    builder
        .protocol("http/protobuf")
        .endpoint("http://builder:4318")
        .header("h", "builder-v")
        .intervalSeconds(2)
        .timeoutSeconds(3)
        .serviceName("builder-service")
        .serviceNamespace("builder-namespace")
        .serviceInstanceId("builder-instance")
        .serviceVersion("builder-version")
        .resourceAttribute("key", "builder-value");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void properties(String name, TestCase testCase) {
    testCase.systemProperties.forEach(System::setProperty);

    try {
      OpenTelemetryExporter.Builder builder = OpenTelemetryExporter.builder();
      if (testCase.exporterBuilder != null) {
        testCase.exporterBuilder.accept(builder);
      }
      AutoConfiguredOpenTelemetrySdk sdk =
          OtelAutoConfig.createAutoConfiguredOpenTelemetrySdk(
              builder,
              new AtomicReference<>(),
              getExporterOpenTelemetryProperties(testCase),
              PrometheusInstrumentationScope.loadInstrumentationScopeInfo());

      ConfigProperties config = AutoConfigureUtil.getConfig(sdk);
      Map<AttributeKey<?>, Object> map =
          OtelAutoConfig.getResourceField(sdk).getAttributes().asMap();
      testCase.expectedProperties.forEach(
          (key, value) -> {
            AbstractStringAssert<?> o = assertThat(config.getString(key)).describedAs("key=" + key);
            if (value.isPresent()) {
              o.isEqualTo(value.get());
            } else {
              o.isNull();
            }
          });
      testCase.expectedResourceAttributes.forEach(
          (key, value) ->
              assertThat(map.get(AttributeKey.stringKey(key)))
                  .describedAs("key=" + key)
                  .hasToString(value));
    } finally {
      testCase.systemProperties.keySet().forEach(System::clearProperty);
    }
  }

  private static ExporterOpenTelemetryProperties getExporterOpenTelemetryProperties(
      TestCase testCase) {
    if (testCase.propertiesBuilder == null) {
      return PrometheusPropertiesLoader.load().getExporterOpenTelemetryProperties();
    }
    ExporterOpenTelemetryProperties.Builder builder = ExporterOpenTelemetryProperties.builder();
    testCase.propertiesBuilder.accept(builder);
    return builder.build();
  }
}
