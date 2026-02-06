package io.prometheus.metrics.instrumentation.jvm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.instrumentation.jvm.JvmMemoryPoolAllocationMetrics.AllocationCountingNotificationListener;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.jupiter.api.Test;

class JvmMemoryPoolAllocationMetricsTest {

  @Test
  void testListenerLogic() {
    PrometheusRegistry registry = new PrometheusRegistry();
    Counter counter = Counter.builder().name("test").labelNames("pool").register(registry);
    AllocationCountingNotificationListener listener =
        new AllocationCountingNotificationListener(counter);

    // Increase by 123
    listener.handleMemoryPool("TestPool", 0, 123);
    assertThat(getCountByPool("test", "TestPool", registry.scrape())).isEqualTo(123);

    // No increase
    listener.handleMemoryPool("TestPool", 123, 123);
    assertThat(getCountByPool("test", "TestPool", registry.scrape())).isEqualTo(123);

    // No increase, then decrease to 0
    listener.handleMemoryPool("TestPool", 123, 0);
    assertThat(getCountByPool("test", "TestPool", registry.scrape())).isEqualTo(123);

    // No increase, then increase by 7
    listener.handleMemoryPool("TestPool", 0, 7);
    assertThat(getCountByPool("test", "TestPool", registry.scrape())).isEqualTo(130);

    // Increase by 10, then decrease to 10
    listener.handleMemoryPool("TestPool", 17, 10);
    assertThat(getCountByPool("test", "TestPool", registry.scrape())).isEqualTo(140);

    // Increase by 7, then increase by 3
    listener.handleMemoryPool("TestPool", 17, 20);
    assertThat(getCountByPool("test", "TestPool", registry.scrape())).isEqualTo(150);

    // Decrease to 17, then increase by 3
    listener.handleMemoryPool("TestPool", 17, 20);
    assertThat(getCountByPool("test", "TestPool", registry.scrape())).isEqualTo(153);

    // Edge case: before < last (tests diff1 < 0 branch)
    listener.handleMemoryPool("TestPool", 10, 15);
    assertThat(getCountByPool("test", "TestPool", registry.scrape())).isEqualTo(158);
  }

  private double getCountByPool(String metricName, String poolName, MetricSnapshots snapshots) {
    for (MetricSnapshot snapshot : snapshots) {
      if (snapshot.getMetadata().getPrometheusName().equals(metricName)) {
        for (CounterSnapshot.CounterDataPointSnapshot data :
            ((CounterSnapshot) snapshot).getDataPoints()) {
          if (data.getLabels().get("pool").equals(poolName)) {
            return data.getValue();
          }
        }
      }
    }
    fail("pool " + poolName + " not found.");
    return 0.0;
  }
}
