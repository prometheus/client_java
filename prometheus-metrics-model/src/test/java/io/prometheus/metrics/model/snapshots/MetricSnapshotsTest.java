package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MetricSnapshotsTest {

    @Test
    void testEmpty() {
        MetricSnapshots snapshots = MetricSnapshots.builder().build();
        Assertions.assertFalse(snapshots.stream().findAny().isPresent());
    }

    @Test
    void testSort() {
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
        Assertions.assertEquals(3, snapshots.size());
        Assertions.assertEquals("counter1", snapshots.get(0).getMetadata().getName());
        Assertions.assertEquals("counter2", snapshots.get(1).getMetadata().getName());
        Assertions.assertEquals("counter3", snapshots.get(2).getMetadata().getName());
    }

    @Test
    void testDuplicateName() {
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
        assertThrows(IllegalArgumentException.class, () -> new MetricSnapshots(c, g));
    }

    @Test
    void testBuilder() {
        CounterSnapshot counter = CounterSnapshot.builder()
                .name("my_metric")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        MetricSnapshots.Builder builder = MetricSnapshots.builder();
        Assertions.assertFalse(builder.containsMetricName("my_metric"));
        builder.metricSnapshot(counter);
        Assertions.assertTrue(builder.containsMetricName("my_metric"));
    }

    @Test
    void testImmutable() {
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
        Assertions.assertThrows(UnsupportedOperationException.class, iterator::remove);
    }
}
