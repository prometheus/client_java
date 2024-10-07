package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

import io.prometheus.metrics.model.snapshots.HistogramSnapshot.HistogramDataPointSnapshot;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class HistogramSnapshotTest {

  @Test
  public void testGoodCaseComplete() {
    long createdTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
    long scrapeTimestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2);
    long exemplarTimestamp = System.currentTimeMillis();
    Exemplar exemplar1 =
        Exemplar.builder()
            .value(129.0)
            .traceId("abcabc")
            .spanId("defdef")
            .labels(Labels.of("status", "200"))
            .timestampMillis(exemplarTimestamp)
            .build();
    HistogramSnapshot snapshot =
        HistogramSnapshot.builder()
            .name("request_size_bytes")
            .help("request sizes in bytes")
            .unit(Unit.BYTES)
            .dataPoint(
                HistogramDataPointSnapshot.builder()
                    .sum(27000.0)
                    .nativeSchema(5)
                    .nativeZeroCount(2)
                    .nativeZeroThreshold(0.0000001)
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.builder()
                            .bucket(Double.POSITIVE_INFINITY, 0)
                            .bucket(128.0, 7)
                            .bucket(1024.0, 15)
                            .build())
                    // The total number of observations in the native and classic histogram
                    // is consistent (22 observations), but the individual bucket counts don't fit.
                    // It doesn't matter for this test, but it would be good to use a more
                    // consistent
                    // example in the test.
                    .nativeBucketsForPositiveValues(
                        NativeHistogramBuckets.builder()
                            .bucket(1, 12)
                            .bucket(2, 3)
                            .bucket(4, 2)
                            .build())
                    .nativeBucketsForNegativeValues(
                        NativeHistogramBuckets.builder().bucket(-1, 1).bucket(0, 2).build())
                    .labels(Labels.of("path", "/"))
                    .exemplars(Exemplars.of(exemplar1))
                    .createdTimestampMillis(createdTimestamp)
                    .scrapeTimestampMillis(scrapeTimestamp)
                    .build())
            .dataPoint(
                HistogramDataPointSnapshot.builder()
                    .count(3)
                    .sum(400.2)
                    .nativeSchema(5)
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.builder()
                            .bucket(128.0, 0)
                            .bucket(1024.0, 4)
                            .bucket(Double.POSITIVE_INFINITY, 2)
                            .build())
                    .nativeBucketsForPositiveValues(
                        NativeHistogramBuckets.builder()
                            .bucket(-1, 1)
                            .bucket(3, 3)
                            .bucket(4, 2)
                            .build())
                    .labels(Labels.of("path", "/api/v1"))
                    .exemplars(Exemplars.of(exemplar1))
                    .createdTimestampMillis(createdTimestamp)
                    .scrapeTimestampMillis(scrapeTimestamp)
                    .build())
            .build();
    SnapshotTestUtil.assertMetadata(
        snapshot, "request_size_bytes", "request sizes in bytes", "bytes");

    assertThat(snapshot.getDataPoints()).hasSize(2);
    HistogramDataPointSnapshot data =
        snapshot
            .getDataPoints()
            .get(0); // data is sorted by labels, so the first one should be path="/"
    assertThat(data.hasSum()).isTrue();
    assertThat(data.hasCount()).isTrue();
    assertThat(data.hasCreatedTimestamp()).isTrue();
    assertThat(data.hasScrapeTimestamp()).isTrue();
    assertThat(data.getCount()).isEqualTo(22);
    assertThat(data.getSum()).isCloseTo(27000.0, offset(0.0));
    assertThat((Iterable<? extends Label>) data.getLabels()).isEqualTo(Labels.of("path", "/"));
    assertThat(data.getExemplars().get(128.0, 1024.0).getValue())
        .isCloseTo(exemplar1.getValue(), offset(0.0));
    assertThat(data.getCreatedTimestampMillis()).isEqualTo(createdTimestamp);
    assertThat(data.getScrapeTimestampMillis()).isEqualTo(scrapeTimestamp);
    // classic histogram 1
    int i = 0;
    for (ClassicHistogramBucket bucket : data.getClassicBuckets()) {
      switch (i++) {
        case 0:
          assertThat(bucket.getUpperBound()).isCloseTo(128.0, offset(0.0));
          assertThat(bucket.getCount()).isEqualTo(7);
          break;
        case 1:
          assertThat(bucket.getUpperBound()).isCloseTo(1024.0, offset(0.0));
          assertThat(bucket.getCount()).isEqualTo(15);
          break;
        case 2:
          assertThat(bucket.getUpperBound()).isCloseTo(Double.POSITIVE_INFINITY, offset(0.0));
          assertThat(bucket.getCount()).isZero();
          break;
      }
    }
    assertThat(i).as("expecting 3 classic histogram buckets").isEqualTo(3);
    // native histogram 1
    assertThat(data.getNativeSchema()).isEqualTo(5);
    assertThat(data.getNativeZeroCount()).isEqualTo(2);
    assertThat(data.getNativeZeroThreshold()).isCloseTo(0.0000001, offset(0.0000001));
    assertThat(data.getNativeBucketsForPositiveValues().size()).isEqualTo(3);
    i = 0;
    for (NativeHistogramBucket bucket : data.getNativeBucketsForPositiveValues()) {
      switch (i++) {
        case 0:
          assertThat(bucket.getBucketIndex()).isOne();
          assertThat(bucket.getCount()).isEqualTo(12);
          break;
        case 1:
          assertThat(bucket.getBucketIndex()).isEqualTo(2);
          assertThat(bucket.getCount()).isEqualTo(3);
          break;
        case 2:
          assertThat(bucket.getBucketIndex()).isEqualTo(4);
          assertThat(bucket.getCount()).isEqualTo(2);
          break;
      }
    }
    assertThat(i).as("expecting 3 native buckets for positive values").isEqualTo(3);
    i = 0;
    assertThat(data.getNativeBucketsForNegativeValues().size()).isEqualTo(2);
    for (NativeHistogramBucket bucket : data.getNativeBucketsForNegativeValues()) {
      switch (i++) {
        case 0:
          assertThat(bucket.getBucketIndex()).isEqualTo(-1);
          assertThat(bucket.getCount()).isOne();
          break;
        case 1:
          assertThat(bucket.getBucketIndex()).isZero();
          assertThat(bucket.getCount()).isEqualTo(2);
          break;
      }
    }
    assertThat(i).as("expecting 2 native buckets for positive values").isEqualTo(2);
    // classic histogram 2 (it's ok that this is incomplete, because we covered it with the other
    // tests)
    data = snapshot.getDataPoints().get(1);
    assertThat(data.getCount()).isEqualTo(6);
    // native histogram 2 (it's ok that this is incomplete, because we covered it with the other
    // tests)
    assertThat(data.getNativeSchema()).isEqualTo(5);
    assertThat(data.getNativeZeroCount()).isZero();
    assertThat(data.getNativeZeroThreshold()).isZero();
  }

  @Test
  public void testEmptyHistogram() {
    HistogramSnapshot snapshot = HistogramSnapshot.builder().name("empty_histogram").build();
    assertThat(snapshot.getDataPoints()).isEmpty();
  }

  @Test
  public void testMinimalClassicHistogram() {
    HistogramSnapshot snapshot =
        HistogramSnapshot.builder()
            .name("minimal_histogram")
            .dataPoint(
                HistogramDataPointSnapshot.builder()
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.of(
                            new double[] {Double.POSITIVE_INFINITY}, new long[] {0}))
                    .build())
            .build();
    HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);
    assertThat(data.hasSum()).isFalse();
    assertThat(snapshot.getDataPoints().get(0).getClassicBuckets().size()).isOne();
  }

  @Test
  public void testMinimalNativeHistogram() {
    HistogramSnapshot snapshot =
        HistogramSnapshot.builder()
            .name("hist")
            .dataPoint(HistogramDataPointSnapshot.builder().nativeSchema(5).build())
            .build();
    assertThat(snapshot.getMetadata().getName()).isEqualTo("hist");
    assertThat(snapshot.getMetadata().hasUnit()).isFalse();
    assertThat(snapshot.getDataPoints().size()).isOne();
    HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);
    assertThat(data.hasCreatedTimestamp()).isFalse();
    assertThat(data.hasScrapeTimestamp()).isFalse();
    assertThat(data.hasCount()).isTrue();
    assertThat(data.getCount()).isZero();
    assertThat(data.hasSum()).isFalse();
    assertThat(data.getNativeBucketsForNegativeValues().size()).isZero();
    assertThat(data.getNativeBucketsForPositiveValues().size()).isZero();
  }

  @Test
  public void testClassicCount() {
    HistogramSnapshot snapshot =
        HistogramSnapshot.builder()
            .name("test_histogram")
            .dataPoint(
                HistogramDataPointSnapshot.builder()
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.builder()
                            .bucket(1.0, 3)
                            .bucket(2.0, 2)
                            .bucket(Double.POSITIVE_INFINITY, 0)
                            .build())
                    .build())
            .build();
    HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);
    assertThat(data.hasSum()).isFalse();
    assertThat(data.hasCount()).isTrue();
    assertThat(data.getCount()).isEqualTo(5);
  }

  @Test
  public void testEmptyData() {
    // This will fail because one of nativeSchema and classicHistogramBuckets is required
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> HistogramDataPointSnapshot.builder().build());
  }

  @Test
  public void testEmptyNativeData() {
    HistogramDataPointSnapshot data = HistogramDataPointSnapshot.builder().nativeSchema(5).build();
    assertThat(data.getNativeBucketsForNegativeValues().size()).isZero();
    assertThat(data.getNativeBucketsForPositiveValues().size()).isZero();
  }

  @Test
  public void testDataImmutable() {
    HistogramSnapshot snapshot =
        HistogramSnapshot.builder()
            .name("test_histogram")
            .dataPoint(
                HistogramDataPointSnapshot.builder()
                    .labels(Labels.of("a", "a"))
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.of(
                            new double[] {Double.POSITIVE_INFINITY}, new long[] {0}))
                    .build())
            .dataPoint(
                HistogramDataPointSnapshot.builder()
                    .labels(Labels.of("a", "b"))
                    .classicHistogramBuckets(
                        ClassicHistogramBuckets.of(
                            new double[] {Double.POSITIVE_INFINITY}, new long[] {2}))
                    .build())
            .build();
    Iterator<HistogramDataPointSnapshot> iterator = snapshot.getDataPoints().iterator();
    iterator.next();
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(iterator::remove);
  }

  @Test
  public void testEmptyClassicBuckets() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                new HistogramDataPointSnapshot(
                    ClassicHistogramBuckets.EMPTY, Double.NaN, Labels.EMPTY, Exemplars.EMPTY, 0L));
  }

  @Test
  public void testMinimalNativeData() {
    new HistogramDataPointSnapshot(
        ClassicHistogramBuckets.EMPTY,
        0,
        0,
        0.0,
        NativeHistogramBuckets.EMPTY,
        NativeHistogramBuckets.EMPTY,
        Double.NaN,
        Labels.EMPTY,
        Exemplars.EMPTY,
        0L);
  }

  @Test
  public void testMinimalClassicData() {
    ClassicHistogramBuckets buckets =
        ClassicHistogramBuckets.builder().bucket(Double.POSITIVE_INFINITY, 0).build();
    new HistogramDataPointSnapshot(
        buckets,
        HistogramSnapshot.CLASSIC_HISTOGRAM,
        0,
        0.0,
        NativeHistogramBuckets.EMPTY,
        NativeHistogramBuckets.EMPTY,
        Double.NaN,
        Labels.EMPTY,
        Exemplars.EMPTY,
        0L);
  }
}
