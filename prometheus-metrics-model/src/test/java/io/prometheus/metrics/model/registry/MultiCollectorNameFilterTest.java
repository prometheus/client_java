package io.prometheus.metrics.model.registry;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class MultiCollectorNameFilterTest {

  private static class Registry extends PrometheusRegistry {
    private final AtomicBoolean collectCalled = new AtomicBoolean();

    public Registry(List<String> prometheusNames) {
      register(
          new MultiCollector() {
            @Override
            public MetricSnapshots collect() {
              collectCalled.set(true);
              return MetricSnapshots.builder()
                  .metricSnapshot(
                      CounterSnapshot.builder()
                          .name("counter_1")
                          .dataPoint(CounterDataPointSnapshot.builder().value(1.0).build())
                          .build())
                  .metricSnapshot(
                      GaugeSnapshot.builder()
                          .name("gauge_2")
                          .dataPoint(GaugeDataPointSnapshot.builder().value(1.0).build())
                          .build())
                  .build();
            }

            @Override
            public List<String> getPrometheusNames() {
              return prometheusNames;
            }
          });
    }

    public boolean collectCalled() {
      return collectCalled.get();
    }
  }

  @Test
  public void testPartialFilter() {
    Registry registry = new Registry(Collections.emptyList());
    MetricSnapshots snapshots = registry.scrape(name -> name.equals("counter_1"));
    assertThat(registry.collectCalled()).isTrue();
    assertThat(snapshots.size()).isOne();
    assertThat(snapshots.get(0).getMetadata().getName()).isEqualTo("counter_1");
  }

  @Test
  public void testPartialFilterWithPrometheusNames() {
    Registry registry = new Registry(Arrays.asList("counter_1", "gauge_2"));

    MetricSnapshots snapshots = registry.scrape(name -> name.equals("counter_1"));
    assertThat(registry.collectCalled()).isTrue();
    assertThat(snapshots.size()).isOne();
    assertThat(snapshots.get(0).getMetadata().getName()).isEqualTo("counter_1");
  }

  @Test
  public void testCompleteFilter_CollectCalled() {
    Registry registry = new Registry(Collections.emptyList());
    MetricSnapshots snapshots =
        registry.scrape(name -> !name.equals("counter_1") && !name.equals("gauge_2"));
    assertThat(registry.collectCalled()).isTrue();
    assertThat(snapshots.size()).isZero();
  }

  @Test
  public void testCompleteFilter_CollectNotCalled() {
    Registry registry = new Registry(Arrays.asList("counter_1", "gauge_2"));
    MetricSnapshots snapshots =
        registry.scrape(name -> !name.equals("counter_1") && !name.equals("gauge_2"));
    assertThat(registry.collectCalled()).isFalse();
    assertThat(snapshots.size()).isZero();
  }
}
