package io.prometheus.metrics.instrumentation.jvm;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.lang.management.ManagementFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JvmMetricsTest {

  @BeforeEach
  void setUp() {
    PrometheusRegistry.defaultRegistry.clear();
  }

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
    // for coverage
    JvmMemoryPoolAllocationMetrics.builder(PrometheusProperties.get())
        .withGarbageCollectorBeans(ManagementFactory.getGarbageCollectorMXBeans())
        .register();
  }

  @Test
  void testConstLabelsApplied() {
    PrometheusRegistry registry = new PrometheusRegistry();
    Labels labels = Labels.of("env", "dev");
    JvmMetrics.builder().constLabels(labels).register(registry);
    MetricSnapshots snapshots = registry.scrape();
    boolean found = false;
    for (MetricSnapshot snapshot : snapshots) {
      for (DataPointSnapshot dp : snapshot.getDataPoints()) {
        if ("dev".equals(dp.getLabels().get("env"))) {
          found = true;
          break;
        }
      }
      if (found) break;
    }
    assertThat(found).isTrue();
  }

  @Test
  void testJvmMetrics() {
    JvmMetrics.builder(PrometheusProperties.get()).register();
    JvmMetrics.builder().register();
  }
}
