package io.prometheus.metrics.instrumentation.jvm;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.Test;

public class JvmMetricsTest {

  @Test
  public void testRegisterIdempotent() {
    PrometheusRegistry registry = new PrometheusRegistry();
    assertThat(registry.scrape().size()).isZero();
    JvmMetrics.builder().register(registry);
    assertThat(registry.scrape().size()).isGreaterThan(0);
    JvmMetrics.builder().register(registry);
  }
}
