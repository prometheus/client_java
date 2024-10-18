package io.prometheus.metrics.exporter.httpserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.jupiter.api.Test;

class MetricsHandlerTest {

  @Test
  void ctor() {
    assertThat(new MetricsHandler())
        .extracting("prometheusScrapeHandler")
        .extracting("registry")
        .isEqualTo(PrometheusRegistry.defaultRegistry);

    PrometheusRegistry registry = new PrometheusRegistry();
    assertThat(new MetricsHandler(registry))
        .extracting("prometheusScrapeHandler")
        .extracting("registry")
        .isEqualTo(registry);

    PrometheusProperties properties = mock(PrometheusProperties.class, RETURNS_MOCKS);

    assertThat(new MetricsHandler(properties))
        .extracting("prometheusScrapeHandler")
        .extracting("registry")
        .isEqualTo(PrometheusRegistry.defaultRegistry);

    verify(properties).getExporterProperties();
  }
}
