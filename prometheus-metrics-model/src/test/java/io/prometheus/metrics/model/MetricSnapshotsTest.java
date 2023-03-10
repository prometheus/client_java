package io.prometheus.metrics.model;

import org.junit.Assert;
import org.junit.Test;

public class MetricSnapshotsTest {

    @Test
    public void testEmpty() {
        MetricSnapshots snapshots = MetricSnapshots.newBuilder().build();
        Assert.assertFalse(snapshots.stream().findAny().isPresent());
    }

    @Test
    public void testSort() {
        CounterSnapshot c1 = CounterSnapshot.newBuilder()
                .withName("counter1")
                .addCounterData(CounterSnapshot.CounterData.newBuilder().withValue(1.0).build())
                .build();
        CounterSnapshot c2 = CounterSnapshot.newBuilder()
                .withName("counter2")
                .addCounterData(CounterSnapshot.CounterData.newBuilder().withValue(1.0).build())
                .build();
        CounterSnapshot c3 = CounterSnapshot.newBuilder()
                .withName("counter3")
                .addCounterData(CounterSnapshot.CounterData.newBuilder().withValue(1.0).build())
                .build();
        MetricSnapshots snapshots = new MetricSnapshots(c2, c3, c1);
        Assert.assertEquals(3, snapshots.size());
        Assert.assertEquals("counter1", snapshots.get(0).getMetadata().getName());
        Assert.assertEquals("counter2", snapshots.get(1).getMetadata().getName());
        Assert.assertEquals("counter3", snapshots.get(2).getMetadata().getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateName() {
        // You might think this is a valid scenario, because the counter will produce
        // my_metric_total and my_metric_created, both not conflicting with the gauge.
        // However, the name for HELP, TYPE, UNIT is the same, and that is invalid.
        CounterSnapshot c = CounterSnapshot.newBuilder()
                .withName("my_metric")
                .addCounterData(CounterSnapshot.CounterData.newBuilder().withValue(1.0).build())
                .build();
        GaugeSnapshot g = GaugeSnapshot.newBuilder()
                .withName("my_metric")
                .addGaugeData(GaugeSnapshot.GaugeData.newBuilder().withValue(1.0).build())
                .build();
        new MetricSnapshots(c, g);
    }
}
