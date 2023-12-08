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
        MetricSnapshots snapshots = MetricSnapshots.of(c2, c3, c1);
        Assert.assertEquals(3, snapshots.size());
        Assert.assertEquals("counter1", snapshots.get(0).getMetadata().getName());
        Assert.assertEquals("counter2", snapshots.get(1).getMetadata().getName());
        Assert.assertEquals("counter3", snapshots.get(2).getMetadata().getName());
    }

    /** It is legal to have duplicate names as long as labels are different. */
    @Test
    public void testDuplicateName() {
        CounterSnapshot c1 = CounterSnapshot.builder()
                .name("my_metric")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).labels(Labels.of("name", "c1")).build())
                .build();
        CounterSnapshot c2 = CounterSnapshot.builder()
                .name("my_metric")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).labels(Labels.of("name", "c2")).build())
                .build();
        MetricSnapshots.of(c1, c2);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateNameSameLabels() {
        CounterSnapshot c1 = CounterSnapshot.builder()
                .name("my_metric")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        CounterSnapshot c2 = CounterSnapshot.builder()
                .name("my_metric")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        MetricSnapshots.of(c1, c2);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateNameDifferentTypes() {
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
        MetricSnapshots.of(c, g);
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
