package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JvmMetricsTest {

  @Test
  public void testRegisterIdempotent() {
    PrometheusRegistry registry = new PrometheusRegistry();
    assertThat(registry.scrape().size()).isZero();
    JvmMetrics.builder().register(registry);
    assertThat(registry.scrape().size()).isGreaterThan(0);
    JvmMetrics.builder().register(registry);
  }

  @Test
  void pool() {
    JvmMemoryPoolAllocationMetrics.builder(PrometheusProperties.get()).register();

    PrometheusRegistry.defaultRegistry.
  }
}
