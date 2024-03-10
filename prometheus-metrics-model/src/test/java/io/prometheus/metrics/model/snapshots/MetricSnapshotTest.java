package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MetricSnapshotTest {

    @Test
    void testDuplicateLabels() {
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                CounterSnapshot.builder()
                .name("events")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("path", "/hello", "status", "200"))
                        .value(1.0)
                        .build())
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("path", "/world", "status", "200"))
                        .value(2.0)
                        .build())
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("status", "200", "path", "/hello"))
                        .value(3.0)
                        .build())
                .build());
    }

    @Test
    void testNoData() {
        MetricSnapshot snapshot = CounterSnapshot.builder().name("test").build();
        Assertions.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test
    void testNullData() {
        Assertions.assertThrows(NullPointerException.class, () -> new CounterSnapshot(new MetricMetadata("test"), null));
    }
}
