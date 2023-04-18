package io.prometheus.metrics.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class NativeHistogramSnapshotTest {

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
        NativeHistogramSnapshot snapshot = NativeHistogramSnapshot.newBuilder()
                .withName("request_size_bytes")
                .withHelp("request sizes in bytes")
                .withUnit(Unit.BYTES)
                .addData(
                        NativeHistogramSnapshot.NativeHistogramData.newBuilder()
                                .withCount(12) // TODO how is that not a compile error? This is a protected method!
                                .withSum(27000.0)
                                .withSchema(5)
                                .withZeroCount(2)
                                .withZeroThreshold(0.0000001)
                                .withBucketsForPositiveValues(NativeHistogramBuckets.newBuilder()
                                        .addBucket(1, 12)
                                        .addBucket(2, 13)
                                        .addBucket(4, 12)
                                        .build())
                                .withBucketsForNegativeValues(NativeHistogramBuckets.newBuilder()
                                        .addBucket(-1, 1)
                                        .addBucket(0, 2)
                                        .build())
                                .withLabels(Labels.of("path", "/"))
                                .withExemplars(Exemplars.of(exemplar1))
                                .withCreatedTimestampMillis(createdTimestamp)
                                .withScrapeTimestampMillis(scrapeTimestamp)
                                .build())
                .addData(
                        NativeHistogramSnapshot.NativeHistogramData.newBuilder()
                                .withCount(3) // TODO how is that not a compile error? This is a protected method!
                                .withSum(400.2)
                                .withSchema(5)
                                .withBucketsForPositiveValues(NativeHistogramBuckets.newBuilder()
                                        .addBucket(-1, 1)
                                        .addBucket(3, 2)
                                        .addBucket(4, 5)
                                        .build())
                                .withLabels(Labels.of("path", "/api/v1"))
                                .withExemplars(Exemplars.of(exemplar1))
                                .withCreatedTimestampMillis(createdTimestamp)
                                .withScrapeTimestampMillis(scrapeTimestamp)
                                .build())
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "request_size_bytes", "request sizes in bytes", "bytes");

        Assert.assertEquals(2, snapshot.getData().size());
        NativeHistogramSnapshot.NativeHistogramData data = snapshot.getData().get(0); // data is sorted by labels, so the first one should be path="/"
        Assert.assertTrue(data.hasSum());
        Assert.assertTrue(data.hasCount());
        Assert.assertTrue(data.hasCreatedTimestamp());
        Assert.assertTrue(data.hasScrapeTimestamp());
        Assert.assertEquals(42, data.getCount());
        Assert.assertEquals(27000.0, data.getSum(), 0.0);
        Assert.assertEquals(createdTimestamp, data.getCreatedTimestampMillis());
        Assert.assertEquals(scrapeTimestamp, data.getScrapeTimestampMillis());
        Assert.assertEquals(5, data.getSchema());
        Assert.assertEquals(2, data.getZeroCount());
        Assert.assertEquals(0.0000001, data.getZeroThreshold(), 0.0000001);
        Assert.assertEquals(3, data.getBucketsForPositiveValues().size());
        int i = 0;
        for (NativeHistogramBucket bucket : data.getBucketsForPositiveValues()) {
            switch (i++) {
                case 0:
                    Assert.assertEquals(1, bucket.getBucketIndex());
                    Assert.assertEquals(12, bucket.getCount());
                    break;
                case 1:
                    Assert.assertEquals(2, bucket.getBucketIndex());
                    Assert.assertEquals(13, bucket.getCount());
                    break;
                case 2:
                    Assert.assertEquals(4, bucket.getBucketIndex());
                    Assert.assertEquals(12, bucket.getCount());
                    break;
            }
        }
        i = 0;
        Assert.assertEquals(2, data.getBucketsForNegativeValues().size());
        for (NativeHistogramBucket bucket : data.getBucketsForNegativeValues()) {
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
        Assert.assertEquals(Labels.of("path", "/"), data.getLabels());
        Assert.assertEquals(exemplar1.getValue(), data.getExemplars().get(128.0, 1024.0).getValue(), 0.0);
        Assert.assertEquals(createdTimestamp, data.getCreatedTimestampMillis());
        Assert.assertEquals(scrapeTimestamp, data.getScrapeTimestampMillis());

        data = snapshot.getData().get(1);
        Assert.assertEquals(8, data.getCount());
        Assert.assertEquals(5, data.getSchema());
        Assert.assertEquals(0, data.getZeroCount());
        Assert.assertEquals(0, data.getZeroThreshold(), 0);
    }

    @Test
    public void testGoodCaseMinimal() {
        NativeHistogramSnapshot snapshot = NativeHistogramSnapshot.newBuilder()
                .withName("hist")
                .addData(NativeHistogramSnapshot.NativeHistogramData.newBuilder()
                        .withSchema(5)
                        .build())
                .build();
        Assert.assertEquals("hist", snapshot.getMetadata().getName());
        Assert.assertFalse(snapshot.getMetadata().hasUnit());
        Assert.assertEquals(1, snapshot.getData().size());
        NativeHistogramSnapshot.NativeHistogramData data = snapshot.getData().get(0);
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
        Assert.assertTrue(data.hasCount());
        Assert.assertEquals(0, data.getCount());
        Assert.assertFalse(data.hasSum());
        Assert.assertEquals(0, data.getBucketsForNegativeValues().size());
        Assert.assertEquals(0, data.getBucketsForPositiveValues().size());
    }

    @Test
    public void testEmptySnapshot() {
        NativeHistogramSnapshot snapshot = NativeHistogramSnapshot.newBuilder()
                .withName("hist")
                .build();
        Assert.assertEquals(0, snapshot.getData().size());
    }

    @Test
    public void testEmptyData() {
        NativeHistogramSnapshot.NativeHistogramData data = NativeHistogramSnapshot.NativeHistogramData.newBuilder().withSchema(5).build();
        Assert.assertEquals(0, data.getBucketsForNegativeValues().size());
        Assert.assertEquals(0, data.getBucketsForPositiveValues().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSchemaMissing() {
        NativeHistogramSnapshot.NativeHistogramData.newBuilder().build();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
        NativeHistogramSnapshot snapshot = NativeHistogramSnapshot.newBuilder()
                .withName("test")
                .addData(NativeHistogramSnapshot.NativeHistogramData.newBuilder()
                        .withLabels(Labels.of("a", "a"))
                        .withSchema(5)
                        .build())
                .addData(NativeHistogramSnapshot.NativeHistogramData.newBuilder()
                        .withLabels(Labels.of("a", "b"))
                        .withSchema(5)
                        .build())
                .build();
        Iterator<NativeHistogramSnapshot.NativeHistogramData> iterator = snapshot.getData().iterator();
        iterator.next();
        iterator.remove();
    }
}
