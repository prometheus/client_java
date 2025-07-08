package io.prometheus.metrics.exporter.opentelemetry;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PrometheusInstrumentationScopeTest {

  @Test
  void loadInstrumentationScopeInfo() {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                PrometheusInstrumentationScope.loadInstrumentationScopeInfo(
                    "path", "name", "version"))
        .withMessage(
            "Prometheus metrics library initialization error: Failed to read path from classpath.");

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                PrometheusInstrumentationScope.loadInstrumentationScopeInfo(
                    "instrumentationScope.properties", "name", "version"))
        .havingRootCause()
        .withMessage(
            "Prometheus metrics library initialization error: name not found in"
                + " instrumentationScope.properties in classpath.");

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                PrometheusInstrumentationScope.loadInstrumentationScopeInfo(
                    "instrumentationScope.properties", "instrumentationScope.name", "version"))
        .havingRootCause()
        .withMessage(
            "Prometheus metrics library initialization error: version not found in"
                + " instrumentationScope.properties in classpath.");
  }
}
