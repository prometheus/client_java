package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.HistogramSnapshot.HistogramDataPointSnapshot;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class HistogramSnapshotTest {

    @Test
    public void testGoodCaseComplete() {
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

        Assert.assertEquals(2, snapshot.getDataPoints().size());
        HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0); // data is sorted by labels, so the first one should be path="/"
        Assert.assertTrue(data.hasSum());
        Assert.assertTrue(data.hasCount());
        Assert.assertTrue(data.hasCreatedTimestamp());
        Assert.assertTrue(data.hasScrapeTimestamp());
        Assert.assertEquals(22, data.getCount());
        Assert.assertEquals(27000.0, data.getSum(), 0.0);
        Assert.assertEquals(Labels.of("path", "/"), data.getLabels());
        Assert.assertEquals(exemplar1.getValue(), data.getExemplars().get(128.0, 1024.0).getValue(), 0.0);
        Assert.assertEquals(createdTimestamp, data.getCreatedTimestampMillis());
        Assert.assertEquals(scrapeTimestamp, data.getScrapeTimestampMillis());
        // classic histogram 1
        int i = 0;
        for (ClassicHistogramBucket bucket : data.getClassicBuckets()) {
            switch (i++) {
                case 0:
                    Assert.assertEquals(128.0, bucket.getUpperBound(), 0.0);
                    Assert.assertEquals(7, bucket.getCount());
                    break;
                case 1:
                    Assert.assertEquals(1024.0, bucket.getUpperBound(), 0.0);
                    Assert.assertEquals(15, bucket.getCount());
                    break;
                case 2:
                    Assert.assertEquals(Double.POSITIVE_INFINITY, bucket.getUpperBound(), 0.0);
                    Assert.assertEquals(0, bucket.getCount());
                    break;
            }
        }
        Assert.assertEquals("expecting 3 classic histogram buckets", 3, i);
        // native histogram 1
        Assert.assertEquals(5, data.getNativeSchema());
        Assert.assertEquals(2, data.getNativeZeroCount());
        Assert.assertEquals(0.0000001, data.getNativeZeroThreshold(), 0.0000001);
        Assert.assertEquals(3, data.getNativeBucketsForPositiveValues().size());
        i = 0;
        for (NativeHistogramBucket bucket : data.getNativeBucketsForPositiveValues()) {
            switch (i++) {
                case 0:
                    Assert.assertEquals(1, bucket.getBucketIndex());
                    Assert.assertEquals(12, bucket.getCount());
                    break;
                case 1:
                    Assert.assertEquals(2, bucket.getBucketIndex());
                    Assert.assertEquals(3, bucket.getCount());
                    break;
                case 2:
                    Assert.assertEquals(4, bucket.getBucketIndex());
                    Assert.assertEquals(2, bucket.getCount());
                    break;
            }
        }
        Assert.assertEquals("expecting 3 native buckets for positive values", 3, i);
        i = 0;
        Assert.assertEquals(2, data.getNativeBucketsForNegativeValues().size());
        for (NativeHistogramBucket bucket : data.getNativeBucketsForNegativeValues()) {
            switch (i++) {
                case 0:
                    Assert.assertEquals(-1, bucket.getBucketIndex());
                    Assert.assertEquals(1, bucket.getCount());
                    break;
                case 1:
                    Assert.assertEquals(0, bucket.getBucketIndex());
                    Assert.assertEquals(2, bucket.getCount());
                    break;
            }
        }
        Assert.assertEquals("expecting 2 native buckets for positive values", 2, i);
        // classic histogram 2 (it's ok that this is incomplete, because we covered it with the other tests)
        data = snapshot.getDataPoints().get(1);
        Assert.assertEquals(6, data.getCount());
        // native histogram 2 (it's ok that this is incomplete, because we covered it with the other tests)
        Assert.assertEquals(5, data.getNativeSchema());
        Assert.assertEquals(0, data.getNativeZeroCount());
        Assert.assertEquals(0, data.getNativeZeroThreshold(), 0);
    }

    @Test
    public void testEmptyHistogram() {
        HistogramSnapshot snapshot = HistogramSnapshot.builder()
                .name("empty_histogram")
                .build();
        Assert.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test
    public void testMinimalClassicHistogram() {
        HistogramSnapshot snapshot = HistogramSnapshot.builder()
                .name("minimal_histogram")
                .dataPoint(HistogramDataPointSnapshot.builder()
                        .classicHistogramBuckets(ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{0}))
                        .build())
                .build();
        HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);
        Assert.assertFalse(data.hasSum());
        Assert.assertEquals(1, snapshot.getDataPoints().get(0).getClassicBuckets().size());
    }

    @Test
    public void testMinimalNativeHistogram() {
        HistogramSnapshot snapshot = HistogramSnapshot.builder()
                .name("hist")
                .dataPoint(HistogramDataPointSnapshot.builder()
                        .nativeSchema(5)
                        .build())
                .build();
        Assert.assertEquals("hist", snapshot.getMetadata().getName());
        Assert.assertFalse(snapshot.getMetadata().hasUnit());
        Assert.assertEquals(1, snapshot.getDataPoints().size());
        HistogramDataPointSnapshot data = snapshot.getDataPoints().get(0);
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
        Assert.assertTrue(data.hasCount());
        Assert.assertEquals(0, data.getCount());
        Assert.assertFalse(data.hasSum());
        Assert.assertEquals(0, data.getNativeBucketsForNegativeValues().size());
        Assert.assertEquals(0, data.getNativeBucketsForPositiveValues().size());
    }

    @Test
    public void testClassicCount() {
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
        Assert.assertFalse(data.hasSum());
        Assert.assertTrue(data.hasCount());
        Assert.assertEquals(5, data.getCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyData() {
        // This will fail because one of nativeSchema and classicHistogramBuckets is required
        HistogramDataPointSnapshot.builder().build();
    }

    @Test
    public void testEmptyNativeData() {
        HistogramDataPointSnapshot data = HistogramDataPointSnapshot.builder()
                .nativeSchema(5)
                .build();
        Assert.assertEquals(0, data.getNativeBucketsForNegativeValues().size());
        Assert.assertEquals(0, data.getNativeBucketsForPositiveValues().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
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
        iterator.remove();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyClassicBuckets() {
        new HistogramDataPointSnapshot(ClassicHistogramBuckets.EMPTY, Double.NaN, Labels.EMPTY, Exemplars.EMPTY, 0L);
    }

    @Test
    public void testMinimalNativeData() {
        new HistogramDataPointSnapshot(ClassicHistogramBuckets.EMPTY, 0, 0, 0.0,
                NativeHistogramBuckets.EMPTY, NativeHistogramBuckets.EMPTY, Double.NaN, Labels.EMPTY, Exemplars.EMPTY, 0L);
    }

    @Test
    public void testMinimalClassicData() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.builder()
                .bucket(Double.POSITIVE_INFINITY, 0)
                .build();
        new HistogramDataPointSnapshot(buckets, HistogramSnapshot.CLASSIC_HISTOGRAM, 0, 0.0,
                NativeHistogramBuckets.EMPTY, NativeHistogramBuckets.EMPTY, Double.NaN, Labels.EMPTY, Exemplars.EMPTY, 0L);
    }
}
