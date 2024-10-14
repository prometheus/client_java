package io.prometheus.metrics.exporter.opentelemetry;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.internal.AutoConfigureUtil;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.prometheus.metrics.config.ExporterOpenTelemetryProperties;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OtelAutoConfigTest {

  static class TestCase {
    Map<String, String> systemProperties = Collections.emptyMap();
    Map<String, Optional<String>> expected;

    public TestCase(Map<String, Optional<String>> expected) {
      this.expected = expected;
    }

    public TestCase setSystemProperties(Map<String, String> systemProperties) {
      this.systemProperties = systemProperties;
      return this;
    }
  }

  public static Stream<Arguments> testCases() {
    return Stream.of(
        Arguments.of(
            "endpoint from system property",
            new TestCase(
                    ImmutableMap.of(
                        "otel.exporter.otlp.endpoint", Optional.of("http://lgtm:4317"),
                        "otel.exporter.otlp.metrics.endpoint", Optional.empty(),
                        "otel.exporter.otlp.metrics.protocol", Optional.empty()))
                .setSystemProperties(
                    Collections.singletonMap("otel.exporter.otlp.endpoint", "http://lgtm:4317"))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testCases")
  void properties(String name, TestCase testCase) {
    testCase.systemProperties.forEach(System::setProperty);

    try {
      AutoConfiguredOpenTelemetrySdk sdk =
          OtelAutoConfig.createAutoConfiguredOpenTelemetrySdk(
              OpenTelemetryExporter.builder(),
              new AtomicReference<>(),
              ExporterOpenTelemetryProperties.builder().build(),
              PrometheusInstrumentationScope.loadInstrumentationScopeInfo());

      ConfigProperties config = AutoConfigureUtil.getConfig(sdk);
      testCase.expected.forEach(
          (key, value) -> {
            if (value.isPresent()) {
              assertThat(config.getString(key)).isEqualTo(value.get());
            } else {
              assertThat(config.getString(key)).isNull();
            }
          });
    } finally {
      testCase.systemProperties.keySet().forEach(System::clearProperty);
    }
  }
}
