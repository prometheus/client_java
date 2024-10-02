package io.prometheus.metrics.instrumentation.jvm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.Test;

public class JvmMetricsTest {

  @Test
  public void testRegisterIdempotent() {
    PrometheusRegistry registry = new PrometheusRegistry();
    assertEquals(0, registry.scrape().size());
    JvmMetrics.builder().register(registry);
    assertTrue(registry.scrape().size() > 0);
    JvmMetrics.builder().register(registry);
  }
}
