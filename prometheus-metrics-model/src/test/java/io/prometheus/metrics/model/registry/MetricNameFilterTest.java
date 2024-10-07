package io.prometheus.metrics.model.registry;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Before;
import org.junit.Test;

public class MetricNameFilterTest {

  private PrometheusRegistry registry;

  @Before
  public void setUp() {
    registry = new PrometheusRegistry();
  }

  @Test
  public void testCounter() {
    registry.register(
        () ->
            CounterSnapshot.builder()
                .name("counter1")
                .help("test counter 1")
                .dataPoint(
                    CounterDataPointSnapshot.builder()
                        .labels(Labels.of("path", "/hello"))
                        .value(1.0)
                        .build())
                .dataPoint(
                    CounterDataPointSnapshot.builder()
                        .labels(Labels.of("path", "/goodbye"))
                        .value(2.0)
                        .build())
                .build());
    registry.register(
        () ->
            CounterSnapshot.builder()
                .name("counter2")
                .help("test counter 2")
                .dataPoint(CounterDataPointSnapshot.builder().value(1.0).build())
                .build());

    MetricNameFilter filter = MetricNameFilter.builder().build();
    assertThat(registry.scrape(filter).size()).isEqualTo(2);

    filter = MetricNameFilter.builder().nameMustStartWith("counter1").build();
    MetricSnapshots snapshots = registry.scrape(filter);
    assertThat(snapshots.size()).isEqualTo(1);
    assertThat(snapshots.get(0).getMetadata().getName()).isEqualTo("counter1");

    filter = MetricNameFilter.builder().nameMustNotStartWith("counter1").build();
    snapshots = registry.scrape(filter);
    assertThat(snapshots.size()).isEqualTo(1);
    assertThat(snapshots.get(0).getMetadata().getName()).isEqualTo("counter2");

    filter =
        MetricNameFilter.builder().nameMustBeEqualTo("counter2_total", "counter1_total").build();
    snapshots = registry.scrape(filter);
    assertThat(snapshots.size()).isEqualTo(2);

    filter =
        MetricNameFilter.builder()
            .nameMustBeEqualTo("counter1_total")
            .nameMustNotBeEqualTo("counter1_total")
            .build();
    assertThat(registry.scrape(filter).size()).isEqualTo(0);
  }
}
