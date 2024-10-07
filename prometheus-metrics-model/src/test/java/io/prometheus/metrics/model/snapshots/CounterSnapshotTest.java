package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

public class CounterSnapshotTest {

  @Test
  public void testCompleteGoodCase() {
    long createdTimestamp1 = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
    long createdTimestamp2 = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2);
    long exemplarTimestamp = System.currentTimeMillis();
    CounterSnapshot snapshot =
        CounterSnapshot.builder()
            .name("http_server_requests_seconds")
            .help("total time spent serving requests")
            .unit(Unit.SECONDS)
            .dataPoint(
                CounterDataPointSnapshot.builder()
                    .value(1.0)
                    .exemplar(
                        Exemplar.builder()
                            .value(3.0)
                            .traceId("abc123")
                            .spanId("123457")
                            .timestampMillis(exemplarTimestamp)
                            .build())
                    .labels(Labels.builder().label("path", "/world").build())
                    .createdTimestampMillis(createdTimestamp1)
                    .build())
            .dataPoint(
                CounterDataPointSnapshot.builder()
                    .value(2.0)
                    .exemplar(
                        Exemplar.builder()
                            .value(4.0)
                            .traceId("def456")
                            .spanId("234567")
                            .timestampMillis(exemplarTimestamp)
                            .build())
                    .labels(Labels.builder().label("path", "/hello").build())
                    .createdTimestampMillis(createdTimestamp2)
                    .build())
            .build();
    SnapshotTestUtil.assertMetadata(
        snapshot, "http_server_requests_seconds", "total time spent serving requests", "seconds");
    assertThat(snapshot.getDataPoints()).hasSize(2);
    CounterDataPointSnapshot data =
        snapshot
            .getDataPoints()
            .get(0); // data is sorted by labels, so the first one should be path="/hello"
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.of("path", "/hello"));
    assertThat(data.getValue()).isCloseTo(2.0, offset(0.0));
    assertThat(data.getExemplar().getValue()).isCloseTo(4.0, offset(0.0));
    assertThat(data.getCreatedTimestampMillis()).isEqualTo(createdTimestamp2);
    assertThat(data.hasScrapeTimestamp()).isFalse();
    data = snapshot.getDataPoints().get(1);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.of("path", "/world"));
    assertThat(data.getValue()).isCloseTo(1.0, offset(0.0));
    assertThat(data.getExemplar().getValue()).isCloseTo(3.0, offset(0.0));
    assertThat(data.getCreatedTimestampMillis()).isEqualTo(createdTimestamp1);
    assertThat(data.hasScrapeTimestamp()).isFalse();
  }

  @Test
  public void testMinimalGoodCase() {
    CounterSnapshot snapshot =
        CounterSnapshot.builder()
            .name("events")
            .dataPoint(CounterDataPointSnapshot.builder().value(1.0).build())
            .build();
    SnapshotTestUtil.assertMetadata(snapshot, "events", null, null);
    assertThat(snapshot.getDataPoints()).hasSize(1);
    CounterDataPointSnapshot data = snapshot.getDataPoints().get(0);
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.EMPTY);
    assertThat(data.getValue()).isCloseTo(1.0, offset(0.0));
    assertThat(data.getExemplar()).isNull();
    assertThat(data.hasCreatedTimestamp()).isFalse();
    assertThat(data.hasScrapeTimestamp()).isFalse();
  }

  @Test
  public void testEmptyCounter() {
    CounterSnapshot snapshot = CounterSnapshot.builder().name("events").build();
    assertThat(snapshot.getDataPoints()).isEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTotalSuffixPresent() {
    CounterSnapshot.builder().name("test_total").build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testValueMissing() {
    CounterDataPointSnapshot.builder().build();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testDataImmutable() {
    CounterSnapshot snapshot =
        CounterSnapshot.builder()
            .name("events")
            .dataPoint(
                CounterDataPointSnapshot.builder().labels(Labels.of("a", "a")).value(1.0).build())
            .dataPoint(
                CounterDataPointSnapshot.builder().labels(Labels.of("a", "b")).value(2.0).build())
            .build();
    Iterator<CounterDataPointSnapshot> iterator = snapshot.getDataPoints().iterator();
    iterator.next();
    iterator.remove();
  }
}
