package io.prometheus.metrics.model;

import org.junit.Assert;
import org.junit.Test;

public class MetricSnapshotTest {

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateLabels() {
        CounterSnapshot.newBuilder()
                .withName("events")
                .addCounterData(CounterSnapshot.CounterData.newBuilder()
                        .withLabels(Labels.of("path", "/hello", "status", "200"))
                        .withValue(1.0)
                        .build())
                .addCounterData(CounterSnapshot.CounterData.newBuilder()
                        .withLabels(Labels.of("path", "/world", "status", "200"))
                        .withValue(2.0)
                        .build())
                .addCounterData(CounterSnapshot.CounterData.newBuilder()
                        .withLabels(Labels.of("status", "200", "path", "/hello"))
                        .withValue(3.0)
                        .build())
                .build();
    }

    @Test
    public void testNoData() {
        MetricSnapshot snapshot = CounterSnapshot.newBuilder().withName("test").build();
        Assert.assertEquals(0, snapshot.getData().size());
    }

    @Test(expected = NullPointerException.class)
    public void testNullData() {
        new CounterSnapshot(new MetricMetadata("test"), null);
    }
}
