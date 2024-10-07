package io.prometheus.metrics.model.registry;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

public class MultiCollectorNameFilterTest {
  private final AtomicBoolean collectCalled = new AtomicBoolean();
  private Predicate<String> includedNames = null;
  private List<String> prometheusNames = new ArrayList<>();
  private final PrometheusRegistry registry = newRegistry(prometheusNames);

  private PrometheusRegistry newRegistry(List<String> prometheusNames) {
    PrometheusRegistry registry = new PrometheusRegistry();
    registry.register(
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
    return registry;
  }

  @Test
  public void testPartialFilter() {
    includedNames = name -> name.equals("counter_1");

    MetricSnapshots snapshots = registry.scrape(includedNames);
    assertThat(collectCalled).isTrue();
    assertThat(snapshots.size()).isOne();
    assertThat(snapshots.get(0).getMetadata().getName()).isEqualTo("counter_1");
  }

  @Test
  public void testPartialFilterWithPrometheusNames() {

    includedNames = name -> name.equals("counter_1");
    prometheusNames = Arrays.asList("counter_1", "gauge_2");

    MetricSnapshots snapshots = registry.scrape(includedNames);
    assertThat(collectCalled).isTrue();
    assertThat(snapshots.size()).isOne();
    assertThat(snapshots.get(0).getMetadata().getName()).isEqualTo("counter_1");
  }

  @Test
  public void testCompleteFilter_CollectCalled() {
    includedNames = name -> !name.equals("counter_1") && !name.equals("gauge_2");

    MetricSnapshots snapshots = registry.scrape(includedNames);
    assertThat(collectCalled).isTrue();
    assertThat(snapshots.size()).isZero();
  }

  @Test
  public void testCompleteFilter_CollectNotCalled() {
    includedNames = name -> !name.equals("counter_1") && !name.equals("gauge_2");
    prometheusNames = Arrays.asList("counter_1", "gauge_2");

    MetricSnapshots snapshots = registry.scrape(includedNames);
    assertThat(collectCalled).isFalse();
    assertThat(snapshots.size()).isZero();
  }
}
