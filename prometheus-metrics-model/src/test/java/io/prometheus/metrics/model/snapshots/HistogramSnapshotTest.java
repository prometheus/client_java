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
        Exemplar exemplar1 = Exemplar.newBuilder()
                .withValue(129.0)
                .withTraceId("abcabc")
                .withSpanId("defdef")
                .withLabels(Labels.of("status", "200"))
                .withTimestampMillis(exemplarTimestamp)
                .build();
        HistogramSnapshot snapshot = HistogramSnapshot.newBuilder()
                .withName("request_size_bytes")
                .withHelp("request sizes in bytes")
                .withUnit(Unit.BYTES)
                .addDataPoint(
                        HistogramDataPointSnapshot.newBuilder()
                                .withSum(27000.0)
                                .withNativeSchema(5)
                                .withNativeZeroCount(2)
                                .withNativeZeroThreshold(0.0000001)
                                .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                        .addBucket(Double.POSITIVE_INFINITY, 0)
                                        .addBucket(128.0, 7)
                                        .addBucket(1024.0, 15)
                                        .build())
                                // The total number of observations in the native and classic histogram
                                // is consistent (22 observations), but the individual bucket counts don't fit.
                                // It doesn't matter for this test, but it would be good to use a more consistent
                                // example in the test.
                                .withNativeBucketsForPositiveValues(NativeHistogramBuckets.newBuilder()
                                        .addBucket(1, 12)
                                        .addBucket(2, 3)
                                        .addBucket(4, 2)
                                        .build())
                                .withNativeBucketsForNegativeValues(NativeHistogramBuckets.newBuilder()
                                        .addBucket(-1, 1)
                                        .addBucket(0, 2)
                                        .build())
                                .withLabels(Labels.of("path", "/"))
                                .withExemplars(Exemplars.of(exemplar1))
                                .withCreatedTimestampMillis(createdTimestamp)
                                .withScrapeTimestampMillis(scrapeTimestamp)
                                .build())
                .addDataPoint(HistogramDataPointSnapshot.newBuilder()
                        .withCount(3) // TODO how is that not a compile error? This is a protected method!
                                .withSum(400.2)
                        .withNativeSchema(5)
                                .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                        .addBucket(128.0, 0)
                                        .addBucket(1024.0, 4)
                                        .addBucket(Double.POSITIVE_INFINITY, 2)
                                        .build())
                        .withNativeBucketsForPositiveValues(NativeHistogramBuckets.newBuilder()
                                .addBucket(-1, 1)
                                .addBucket(3, 3)
                                .addBucket(4, 2)
                                .build())
                                .withLabels(Labels.of("path", "/api/v1"))
                                .withExemplars(Exemplars.of(exemplar1))
                                .withCreatedTimestampMillis(createdTimestamp)
                                .withScrapeTimestampMillis(scrapeTimestamp)
                                .build())
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "request_size_bytes", "request sizes in bytes", "bytes");

        Assert.assertEquals(2, snapshot.getData().size());
        HistogramDataPointSnapshot data = snapshot.getData().get(0); // data is sorted by labels, so the first one should be path="/"
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
        data = snapshot.getData().get(1);
        Assert.assertEquals(6, data.getCount());
        // native histogram 2 (it's ok that this is incomplete, because we covered it with the other tests)
        Assert.assertEquals(5, data.getNativeSchema());
        Assert.assertEquals(0, data.getNativeZeroCount());
        Assert.assertEquals(0, data.getNativeZeroThreshold(), 0);
    }

    @Test
    public void testEmptyHistogram() {
        HistogramSnapshot snapshot = HistogramSnapshot.newBuilder()
                .withName("empty_histogram")
                .build();
        Assert.assertEquals(0, snapshot.getData().size());
    }

    @Test
    public void testMinimalClassicHistogram() {
        HistogramSnapshot snapshot = HistogramSnapshot.newBuilder()
                .withName("minimal_histogram")
                .addDataPoint(HistogramDataPointSnapshot.newBuilder()
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{0}))
                        .build())
                .build();
        HistogramDataPointSnapshot data = snapshot.getData().get(0);
        Assert.assertFalse(data.hasSum());
        Assert.assertEquals(1, snapshot.getData().get(0).getClassicBuckets().size());
    }

    @Test
    public void testMinimalNativeHistogram() {
        HistogramSnapshot snapshot = HistogramSnapshot.newBuilder()
                .withName("hist")
                .addDataPoint(HistogramDataPointSnapshot.newBuilder()
                        .withNativeSchema(5)
                        .build())
                .build();
        Assert.assertEquals("hist", snapshot.getMetadata().getName());
        Assert.assertFalse(snapshot.getMetadata().hasUnit());
        Assert.assertEquals(1, snapshot.getData().size());
        HistogramDataPointSnapshot data = snapshot.getData().get(0);
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
        HistogramSnapshot snapshot = HistogramSnapshot.newBuilder()
                .withName("test_histogram")
                .addDataPoint(HistogramDataPointSnapshot.newBuilder()
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(1.0, 3)
                                .addBucket(2.0, 2)
                                .addBucket(Double.POSITIVE_INFINITY, 0)
                                .build())
                        .build())
                .build();
        HistogramDataPointSnapshot data = snapshot.getData().get(0);
        Assert.assertFalse(data.hasSum());
        Assert.assertTrue(data.hasCount());
        Assert.assertEquals(5, data.getCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyData() {
        // This will fail because one of nativeSchema and classicHistogramBuckets is required
        HistogramDataPointSnapshot.newBuilder().build();
    }

    @Test
    public void testEmptyNativeData() {
        HistogramDataPointSnapshot data = HistogramDataPointSnapshot.newBuilder()
                .withNativeSchema(5)
                .build();
        Assert.assertEquals(0, data.getNativeBucketsForNegativeValues().size());
        Assert.assertEquals(0, data.getNativeBucketsForPositiveValues().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
        HistogramSnapshot snapshot = HistogramSnapshot.newBuilder()
                .withName("test_histogram")
                .addDataPoint(HistogramDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("a", "a"))
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{0}))
                        .build())
                .addDataPoint(HistogramDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("a", "b"))
                        .withClassicHistogramBuckets(ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{2}))
                        .build())
                .build();
        Iterator<HistogramDataPointSnapshot> iterator = snapshot.getData().iterator();
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
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.newBuilder()
                .addBucket(Double.POSITIVE_INFINITY, 0)
                .build();
        new HistogramDataPointSnapshot(buckets, HistogramSnapshot.CLASSIC_HISTOGRAM, 0, 0.0,
                NativeHistogramBuckets.EMPTY, NativeHistogramBuckets.EMPTY, Double.NaN, Labels.EMPTY, Exemplars.EMPTY, 0L);
    }
}
