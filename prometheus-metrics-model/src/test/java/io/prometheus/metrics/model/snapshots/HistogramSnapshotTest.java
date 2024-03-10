package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.HistogramSnapshot.HistogramDataPointSnapshot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

class HistogramSnapshotTest {

    @Test
    void testGoodCaseComplete() {
        long createdTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
        long scrapeTimestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2);
        long exemplarTimestamp = System.currentTimeMillis();
        Exemplar exemplar1 = Exemplar.builder()
                .value(129.0)
                .traceId("abcabc")
                .spanId("defdef")
                .labels(Labels.of("status", "200"))
                .timestampMillis(exemplarTimestamp)
                .build();
        HistogramSnapshot snapshot = HistogramSnapshot.builder()
                .name("request_size_bytes")
                .help("request sizes in bytes")
                .unit(Unit.BYTES)
                .dataPoint(
                        HistogramDataPointSnapshot.builder()
                                .sum(27000.0)
                                .nativeSchema(5)
                                .nativeZeroCount(2)
                                .nativeZeroThreshold(0.0000001)
                                .classicHistogramBuckets(ClassicHistogramBuckets.builder()
                                        .bucket(Double.POSITIVE_INFINITY, 0)
                                        .bucket(128.0, 7)
                                        .bucket(1024.0, 15)
                                        .build())
                                // The total number of observations in the native and classic histogram
                                // is consistent (22 observations), but the individual bucket counts don't fit.
                                // It doesn't matter for this test, but it would be good to use a more consistent
                                // example in the test.
                                .nativeBucketsForPositiveValues(NativeHistogramBuckets.builder()
                                        .bucket(1, 12)
                                        .bucket(2, 3)
                                        .bucket(4, 2)
                                        .build())
                                .nativeBucketsForNegativeValues(NativeHistogramBuckets.builder()
                                        .bucket(-1, 1)
                                        .bucket(0, 2)
                                        .build())
                                .labels(Labels.of("path", "/"))
                                .exemplars(Exemplars.of(exemplar1))
                                .createdTimestampMillis(createdTimestamp)
                                .scrapeTimestampMillis(scrapeTimestamp)
                                .build())
                .dataPoint(HistogramDataPointSnapshot.builder()
                        .count(3) // TODO how is that not a compile error? This is a protected method!
                                .sum(400.2)
                        .nativeSchema(5)
                                .classicHistogramBuckets(ClassicHistogramBuckets.builder()
                                        .bucket(128.0, 0)
                                        .bucket(1024.0, 4)
                                        .bucket(Double.POSITIVE_INFINITY, 2)
                                        .build())
                        .nativeBucketsForPositiveValues(NativeHistogramBuckets.builder()
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
        SnapshotTestUtil.assertMetadata(snapshot, "request_size_bytes", "request sizes in bytes", "bytes");

        Assertions.assertEquals(2, snapshot.getDataPoints().size());
        HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0); // data is sorted by labels, so the first one should be path="/"
        Assertions.assertTrue(data.hasSum());
        Assertions.assertTrue(data.hasCount());
        Assertions.assertTrue(data.hasCreatedTimestamp());
        Assertions.assertTrue(data.hasScrapeTimestamp());
        Assertions.assertEquals(22, data.getCount());
        Assertions.assertEquals(27000.0, data.getSum(), 0.0);
        Assertions.assertEquals(Labels.of("path", "/"), data.getLabels());
        Assertions.assertEquals(exemplar1.getValue(), data.getExemplars().get(128.0, 1024.0).getValue(), 0.0);
        Assertions.assertEquals(createdTimestamp, data.getCreatedTimestampMillis());
        Assertions.assertEquals(scrapeTimestamp, data.getScrapeTimestampMillis());
        // classic histogram 1
        int i = 0;
        for (ClassicHistogramBucket bucket : data.getClassicBuckets()) {
            switch (i++) {
                case 0:
                    Assertions.assertEquals(128.0, bucket.getUpperBound(), 0.0);
                    Assertions.assertEquals(7, bucket.getCount());
                    break;
                case 1:
                    Assertions.assertEquals(1024.0, bucket.getUpperBound(), 0.0);
                    Assertions.assertEquals(15, bucket.getCount());
                    break;
                case 2:
                    Assertions.assertEquals(Double.POSITIVE_INFINITY, bucket.getUpperBound(), 0.0);
                    Assertions.assertEquals(0, bucket.getCount());
                    break;
            }
        }
        Assertions.assertEquals(3, i, "expecting 3 classic histogram buckets");
        // native histogram 1
        Assertions.assertEquals(5, data.getNativeSchema());
        Assertions.assertEquals(2, data.getNativeZeroCount());
        Assertions.assertEquals(0.0000001, data.getNativeZeroThreshold(), 0.0000001);
        Assertions.assertEquals(3, data.getNativeBucketsForPositiveValues().size());
        i = 0;
        for (NativeHistogramBucket bucket : data.getNativeBucketsForPositiveValues()) {
            switch (i++) {
                case 0:
                    Assertions.assertEquals(1, bucket.getBucketIndex());
                    Assertions.assertEquals(12, bucket.getCount());
                    break;
                case 1:
                    Assertions.assertEquals(2, bucket.getBucketIndex());
                    Assertions.assertEquals(3, bucket.getCount());
                    break;
                case 2:
                    Assertions.assertEquals(4, bucket.getBucketIndex());
                    Assertions.assertEquals(2, bucket.getCount());
                    break;
            }
        }
        Assertions.assertEquals(3, i, "expecting 3 native buckets for positive values");
        i = 0;
        Assertions.assertEquals(2, data.getNativeBucketsForNegativeValues().size());
        for (NativeHistogramBucket bucket : data.getNativeBucketsForNegativeValues()) {
            switch (i++) {
                case 0:
                    Assertions.assertEquals(-1, bucket.getBucketIndex());
                    Assertions.assertEquals(1, bucket.getCount());
                    break;
                case 1:
                    Assertions.assertEquals(0, bucket.getBucketIndex());
                    Assertions.assertEquals(2, bucket.getCount());
                    break;
            }
        }
        Assertions.assertEquals(2, i, "expecting 2 native buckets for positive values");
        // classic histogram 2 (it's ok that this is incomplete, because we covered it with the other tests)
        data = snapshot.getDataPoints().get(1);
        Assertions.assertEquals(6, data.getCount());
        // native histogram 2 (it's ok that this is incomplete, because we covered it with the other tests)
        Assertions.assertEquals(5, data.getNativeSchema());
        Assertions.assertEquals(0, data.getNativeZeroCount());
        Assertions.assertEquals(0, data.getNativeZeroThreshold(), 0);
    }

    @Test
    void testEmptyHistogram() {
        HistogramSnapshot snapshot = HistogramSnapshot.builder()
                .name("empty_histogram")
                .build();
        Assertions.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test
    void testMinimalClassicHistogram() {
        HistogramSnapshot snapshot = HistogramSnapshot.builder()
                .name("minimal_histogram")
                .dataPoint(HistogramDataPointSnapshot.builder()
                        .classicHistogramBuckets(ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{0}))
                        .build())
                .build();
        HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);
        Assertions.assertFalse(data.hasSum());
        Assertions.assertEquals(1, snapshot.getDataPoints().get(0).getClassicBuckets().size());
    }

    @Test
    void testMinimalNativeHistogram() {
        HistogramSnapshot snapshot = HistogramSnapshot.builder()
                .name("hist")
                .dataPoint(HistogramDataPointSnapshot.builder()
                        .nativeSchema(5)
                        .build())
                .build();
        Assertions.assertEquals("hist", snapshot.getMetadata().getName());
        Assertions.assertFalse(snapshot.getMetadata().hasUnit());
        Assertions.assertEquals(1, snapshot.getDataPoints().size());
        HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);
        Assertions.assertFalse(data.hasCreatedTimestamp());
        Assertions.assertFalse(data.hasScrapeTimestamp());
        Assertions.assertTrue(data.hasCount());
        Assertions.assertEquals(0, data.getCount());
        Assertions.assertFalse(data.hasSum());
        Assertions.assertEquals(0, data.getNativeBucketsForNegativeValues().size());
        Assertions.assertEquals(0, data.getNativeBucketsForPositiveValues().size());
    }

    @Test
    void testClassicCount() {
        HistogramSnapshot snapshot = HistogramSnapshot.builder()
                .name("test_histogram")
                .dataPoint(HistogramDataPointSnapshot.builder()
                        .classicHistogramBuckets(ClassicHistogramBuckets.builder()
                                .bucket(1.0, 3)
                                .bucket(2.0, 2)
                                .bucket(Double.POSITIVE_INFINITY, 0)
                                .build())
                        .build())
                .build();
        HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);
        Assertions.assertFalse(data.hasSum());
        Assertions.assertTrue(data.hasCount());
        Assertions.assertEquals(5, data.getCount());
    }

    @Test
    void testEmptyData() {
        // This will fail because one of nativeSchema and classicHistogramBuckets is required
        assertThrows(IllegalArgumentException.class, () -> HistogramDataPointSnapshot.builder().build());
    }

    @Test
    void testEmptyNativeData() {
        HistogramDataPointSnapshot data = HistogramDataPointSnapshot.builder()
                .nativeSchema(5)
                .build();
        Assertions.assertEquals(0, data.getNativeBucketsForNegativeValues().size());
        Assertions.assertEquals(0, data.getNativeBucketsForPositiveValues().size());
    }

    @Test
    void testDataImmutable() {
        HistogramSnapshot snapshot = HistogramSnapshot.builder()
                .name("test_histogram")
                .dataPoint(HistogramDataPointSnapshot.builder()
                        .labels(Labels.of("a", "a"))
                        .classicHistogramBuckets(ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{0}))
                        .build())
                .dataPoint(HistogramDataPointSnapshot.builder()
                        .labels(Labels.of("a", "b"))
                        .classicHistogramBuckets(ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{2}))
                        .build())
                .build();
        Iterator<HistogramDataPointSnapshot> iterator = snapshot.getDataPoints().iterator();
        iterator.next();
        Assertions.assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    void testEmptyClassicBuckets() {
        assertThrows(IllegalArgumentException.class, () -> new HistogramDataPointSnapshot(ClassicHistogramBuckets.EMPTY, Double.NaN, Labels.EMPTY, Exemplars.EMPTY, 0L));
    }

    @Test
    void testMinimalNativeData() {
        new HistogramDataPointSnapshot(ClassicHistogramBuckets.EMPTY, 0, 0, 0.0,
                NativeHistogramBuckets.EMPTY, NativeHistogramBuckets.EMPTY, Double.NaN, Labels.EMPTY, Exemplars.EMPTY, 0L);
    }

    @Test
    void testMinimalClassicData() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.builder()
                .bucket(Double.POSITIVE_INFINITY, 0)
                .build();
        new HistogramDataPointSnapshot(buckets, HistogramSnapshot.CLASSIC_HISTOGRAM, 0, 0.0,
                NativeHistogramBuckets.EMPTY, NativeHistogramBuckets.EMPTY, Double.NaN, Labels.EMPTY, Exemplars.EMPTY, 0L);
    }
}
