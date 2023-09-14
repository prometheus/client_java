package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class MetricSnapshotsTest {

    @Test
    public void testEmpty() {
        MetricSnapshots snapshots = MetricSnapshots.builder().build();
        Assert.assertFalse(snapshots.stream().findAny().isPresent());
    }

    @Test
    public void testSort() {
        CounterSnapshot c1 = CounterSnapshot.builder()
                .name("counter1")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        CounterSnapshot c2 = CounterSnapshot.builder()
                .name("counter2")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        CounterSnapshot c3 = CounterSnapshot.builder()
                .name("counter3")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        MetricSnapshots snapshots = new MetricSnapshots(c2, c3, c1);
        Assert.assertEquals(3, snapshots.size());
        Assert.assertEquals("counter1", snapshots.get(0).getMetadata().getName());
        Assert.assertEquals("counter2", snapshots.get(1).getMetadata().getName());
        Assert.assertEquals("counter3", snapshots.get(2).getMetadata().getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateName() {
        // Q: What if you have a counter named "foo" and a gauge named "foo"?
        // A: Great question. You might think this is a valid scenario, because the counter will produce
        //    the values "foo_total" and "foo_created" while the gauge will produce the value "foo".
        //    So from that perspective there is no conflict. However, the name for HELP, TYPE, UNIT is the same,
        //    and that is the conflict. Therefore, you cannot have a counter named "foo" and a gauge named "foo".
        CounterSnapshot c = CounterSnapshot.builder()
                .name("my_metric")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        GaugeSnapshot g = GaugeSnapshot.builder()
                .name("my_metric")
                .dataPoint(GaugeSnapshot.GaugeDataPointSnapshot.builder().value(1.0).build())
                .build();
        new MetricSnapshots(c, g);
    }

    @Test
    public void testBuilder() {
        CounterSnapshot counter = CounterSnapshot.builder()
                .name("my_metric")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        MetricSnapshots.Builder builder = MetricSnapshots.builder();
        Assert.assertFalse(builder.containsMetricName("my_metric"));
        builder.metricSnapshot(counter);
        Assert.assertTrue(builder.containsMetricName("my_metric"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutable() {
        CounterSnapshot c1 = CounterSnapshot.builder()
                .name("counter1")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        CounterSnapshot c2 = CounterSnapshot.builder()
                .name("counter2")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        CounterSnapshot c3 = CounterSnapshot.builder()
                .name("counter3")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        MetricSnapshots snapshots = new MetricSnapshots(c2, c3, c1);
        Iterator<MetricSnapshot> iterator = snapshots.iterator();
        iterator.next();
        iterator.remove();
    }
}
