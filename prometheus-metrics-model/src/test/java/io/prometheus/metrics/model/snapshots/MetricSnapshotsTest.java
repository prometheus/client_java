package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Iterator;
import org.junit.jupiter.api.Test;

class MetricSnapshotsTest {

  @Test
  void testEmpty() {
    MetricSnapshots snapshots = MetricSnapshots.builder().build();
    assertThat(snapshots.stream().findAny()).isNotPresent();
  }

  @Test
  void testSort() {
    CounterSnapshot c1 =
        CounterSnapshot.builder()
            .name("counter1")
            .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
            .build();
    CounterSnapshot c2 =
        CounterSnapshot.builder()
            .name("counter2")
            .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
            .build();
    CounterSnapshot c3 =
        CounterSnapshot.builder()
            .name("counter3")
            .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
            .build();
    MetricSnapshots snapshots = new MetricSnapshots(c2, c3, c1);
    assertThat(snapshots.size()).isEqualTo(3);
    assertThat(snapshots.get(0).getMetadata().getName()).isEqualTo("counter1");
    assertThat(snapshots.get(1).getMetadata().getName()).isEqualTo("counter2");
    assertThat(snapshots.get(2).getMetadata().getName()).isEqualTo("counter3");
  }

  @Test
  void testDuplicateName() {
    // Q: What if you have a counter named "foo" and a gauge named "foo"?
    // A: Great question. You might think this is a valid scenario, because the counter will produce
    //    the values "foo_total" and "foo_created" while the gauge will produce the value "foo".
    //    So from that perspective there is no conflict. However, the name for HELP, TYPE, UNIT is
    // the same,
    //    and that is the conflict. Therefore, you cannot have a counter named "foo" and a gauge
    // named "foo".
    CounterSnapshot c =
        CounterSnapshot.builder()
            .name("my_metric")
            .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
            .build();
    GaugeSnapshot g =
        GaugeSnapshot.builder()
            .name("my_metric")
            .dataPoint(GaugeSnapshot.GaugeDataPointSnapshot.builder().value(1.0).build())
            .build();
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new MetricSnapshots(c, g));
  }

  @Test
  void testDuplicateName_histogramGaugeVsClassic_throws() {
    HistogramSnapshot classic =
        HistogramSnapshot.builder()
            .name("my_histogram")
            .dataPoint(
                HistogramSnapshot.HistogramDataPointSnapshot.builder()
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.of(
                            new double[] {Double.POSITIVE_INFINITY}, new long[] {0}))
                    .build())
            .build();
    HistogramSnapshot gauge =
        HistogramSnapshot.builder()
            .name("my_histogram")
            .gaugeHistogram(true)
            .dataPoint(
                HistogramSnapshot.HistogramDataPointSnapshot.builder()
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.of(
                            new double[] {Double.POSITIVE_INFINITY}, new long[] {0}))
                    .build())
            .build();
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new MetricSnapshots(classic, gauge))
        .withMessageContaining("conflicting histogram types");
  }

  @Test
  void testBuilder() {
    CounterSnapshot counter =
        CounterSnapshot.builder()
            .name("my_metric")
            .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
            .build();
    MetricSnapshots.Builder builder = MetricSnapshots.builder();
    assertThat(builder.containsMetricName("my_metric")).isFalse();
    builder.metricSnapshot(counter);
    assertThat(builder.containsMetricName("my_metric")).isTrue();
  }

  @Test
  void testImmutable() {
    CounterSnapshot c1 =
        CounterSnapshot.builder()
            .name("counter1")
            .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
            .build();
    CounterSnapshot c2 =
        CounterSnapshot.builder()
            .name("counter2")
            .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
            .build();
    CounterSnapshot c3 =
        CounterSnapshot.builder()
            .name("counter3")
            .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(1.0).build())
            .build();
    MetricSnapshots snapshots = new MetricSnapshots(c2, c3, c1);
    Iterator<MetricSnapshot> iterator = snapshots.iterator();
    iterator.next();
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(iterator::remove);
  }
}
