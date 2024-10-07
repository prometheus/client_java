package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MetricSnapshotTest {

  @Test(expected = IllegalArgumentException.class)
  public void testDuplicateLabels() {
    CounterSnapshot.builder()
        .name("events")
        .dataPoint(
            CounterSnapshot.CounterDataPointSnapshot.builder()
                .labels(Labels.of("path", "/hello", "status", "200"))
                .value(1.0)
                .build())
        .dataPoint(
            CounterSnapshot.CounterDataPointSnapshot.builder()
                .labels(Labels.of("path", "/world", "status", "200"))
                .value(2.0)
                .build())
        .dataPoint(
            CounterSnapshot.CounterDataPointSnapshot.builder()
                .labels(Labels.of("status", "200", "path", "/hello"))
                .value(3.0)
                .build())
        .build();
  }

  @Test
  public void testNoData() {
    MetricSnapshot snapshot = CounterSnapshot.builder().name("test").build();
    assertThat(snapshot.getDataPoints().size()).isEqualTo(0);
  }

  @Test(expected = NullPointerException.class)
  public void testNullData() {
    new CounterSnapshot(new MetricMetadata("test"), null);
  }
}
