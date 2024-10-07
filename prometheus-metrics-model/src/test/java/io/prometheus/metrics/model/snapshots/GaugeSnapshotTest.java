package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class GaugeSnapshotTest {

  @Test
  public void testCompleteGoodCase() {
    long exemplarTimestamp = System.currentTimeMillis();
    GaugeSnapshot snapshot =
        GaugeSnapshot.builder()
            .name("cache_size_bytes")
            .help("cache size in Bytes")
            .unit(Unit.BYTES)
            .dataPoint(
                GaugeDataPointSnapshot.builder()
                    .value(1024.0)
                    .exemplar(
                        Exemplar.builder()
                            .value(1024.0)
                            .traceId("abc123")
                            .spanId("123457")
                            .timestampMillis(exemplarTimestamp)
                            .build())
                    .labels(Labels.builder().label("env", "prod").build())
                    .build())
            .dataPoint(
                GaugeDataPointSnapshot.builder()
                    .value(128.0)
                    .exemplar(
                        Exemplar.builder()
                            .value(128.0)
                            .traceId("def456")
                            .spanId("234567")
                            .timestampMillis(exemplarTimestamp)
                            .build())
                    .labels(Labels.builder().label("env", "dev").build())
                    .build())
            .build();
    SnapshotTestUtil.assertMetadata(snapshot, "cache_size_bytes", "cache size in Bytes", "bytes");
    assertThat(snapshot.getDataPoints()).hasSize(2);
    GaugeDataPointSnapshot data =
        snapshot
            .getDataPoints()
            .get(0); // data is sorted by labels, so the first one should be path="/hello"
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.of("env", "dev"));
    assertThat(data.getValue()).isEqualTo(128.0);
    assertThat(data.getExemplar().getValue()).isEqualTo(128.0);
    assertThat(data.hasCreatedTimestamp()).isFalse();
    assertThat(data.hasScrapeTimestamp()).isFalse();
    data = snapshot.getDataPoints().get(1);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.of("env", "prod"));
    assertThat(data.getValue()).isEqualTo(1024.0);
    assertThat(data.getExemplar().getValue()).isEqualTo(1024.0);
    assertThat(data.hasCreatedTimestamp()).isFalse();
    assertThat(data.hasScrapeTimestamp()).isFalse();
  }

  @Test
  public void testMinimalGoodCase() {
    GaugeSnapshot snapshot =
        GaugeSnapshot.builder()
            .name("temperature")
            .dataPoint(GaugeDataPointSnapshot.builder().value(23.0).build())
            .build();
    SnapshotTestUtil.assertMetadata(snapshot, "temperature", null, null);
    assertThat(snapshot.getDataPoints().size()).isOne();
    GaugeDataPointSnapshot data = snapshot.getDataPoints().get(0);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEmpty();
    assertThat(data.getValue()).isEqualTo(23.0);
    assertThat(data.getExemplar()).isNull();
    assertThat(data.hasCreatedTimestamp()).isFalse();
    assertThat(data.hasScrapeTimestamp()).isFalse();
  }

  @Test
  public void testEmptyGauge() {
    GaugeSnapshot snapshot = GaugeSnapshot.builder().name("temperature").build();
    assertThat(snapshot.getDataPoints().size()).isZero();
  }

  @Test
  public void testTotalSuffixPresent() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> CounterSnapshot.builder().name("test_total").build());
  }

  @Test
  public void testTotalSuffixPresentDot() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> CounterSnapshot.builder().name("test.total").build());
  }

  @Test
  public void testValueMissing() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> CounterDataPointSnapshot.builder().build());
  }

  @Test
  public void testDataImmutable() {
    GaugeSnapshot snapshot =
        GaugeSnapshot.builder()
            .name("gauge")
            .dataPoint(
                GaugeDataPointSnapshot.builder().labels(Labels.of("a", "a")).value(23.0).build())
            .dataPoint(
                GaugeDataPointSnapshot.builder().labels(Labels.of("a", "b")).value(23.0).build())
            .build();
    Iterator<GaugeDataPointSnapshot> iterator = snapshot.getDataPoints().iterator();
    iterator.next();
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(iterator::remove);
  }
}
