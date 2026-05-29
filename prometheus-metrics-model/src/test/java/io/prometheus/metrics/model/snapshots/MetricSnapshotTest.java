package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

class MetricSnapshotTest {

  @Test
  void testDuplicateLabels() {
    assertThatExceptionOfType(DuplicateLabelsException.class)
        .isThrownBy(
            () ->
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
                    .build())
        .satisfies(
            e -> {
              assertThat(e.getMetadata().getName()).isEqualTo("events");
              assertThat((Iterable<? extends Label>) e.getLabels())
                  .isEqualTo(Labels.of("path", "/hello", "status", "200"));
            });
  }

  @Test
  void testNoData() {
    MetricSnapshot snapshot = CounterSnapshot.builder().name("test").build();
    assertThat(snapshot.getDataPoints().size()).isEqualTo(0);
  }

  @Test
  void testNullData() {
    assertThatExceptionOfType(NullPointerException.class)
        .isThrownBy(() -> new CounterSnapshot(new MetricMetadata("test"), null));
  }
}
