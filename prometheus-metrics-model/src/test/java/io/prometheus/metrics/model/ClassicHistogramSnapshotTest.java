package io.prometheus.metrics.model;

import io.prometheus.metrics.model.ClassicHistogramSnapshot.ClassicHistogramData;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class ClassicHistogramSnapshotTest {

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
        ClassicHistogramSnapshot snapshot = ClassicHistogramSnapshot.newBuilder()
                .withName("request_size_bytes")
                .withHelp("request sizes in bytes")
                .withUnit(Unit.BYTES)
                .addData(
                        ClassicHistogramData.newBuilder()
                                .withCount(12)
                                .withSum(27000.0)
                                .withBuckets(ClassicHistogramBuckets.newBuilder()
                                        .addBucket(Double.POSITIVE_INFINITY, 0)
                                        .addBucket(128.0, 7)
                                        .addBucket(1024.0, 10)
                                        .build())
                                .withLabels(Labels.of("path", "/"))
                                .withExemplars(Exemplars.of(exemplar1))
                                .withCreatedTimestampMillis(createdTimestamp)
                                .withScrapeTimestampMillis(scrapeTimestamp)
                                .build())
                .addData(
                        ClassicHistogramData.newBuilder()
                                .withCount(3)
                                .withSum(400.2)
                                .withBuckets(ClassicHistogramBuckets.newBuilder()
                                        .addBucket(128.0, 0)
                                        .addBucket(1024.0, 2)
                                        .addBucket(Double.POSITIVE_INFINITY, 1)
                                        .build())
                                .withLabels(Labels.of("path", "/api/v1"))
                                .withExemplars(Exemplars.of(exemplar1))
                                .withCreatedTimestampMillis(createdTimestamp)
                                .withScrapeTimestampMillis(scrapeTimestamp)
                                .build())
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "request_size_bytes", "request sizes in bytes", "bytes");

        Assert.assertEquals(2, snapshot.getData().size());
        ClassicHistogramData data = snapshot.getData().get(0); // data is sorted by labels, so the first one should be path="/"
        Assert.assertEquals(17, data.getCount());
        Assert.assertEquals(27000.0, data.getSum(), 0.0);
        int i = 0;
        for (ClassicHistogramBucket bucket : data.getBuckets()) {
            switch (i++) {
                case 0:
                    Assert.assertEquals(128.0, bucket.getUpperBound(), 0.0);
                    Assert.assertEquals(7, bucket.getCount());
                    break;
                case 1:
                    Assert.assertEquals(1024.0, bucket.getUpperBound(), 0.0);
                    Assert.assertEquals(10, bucket.getCount());
                    break;
                case 2:
                    Assert.assertEquals(Double.POSITIVE_INFINITY, bucket.getUpperBound(), 0.0);
                    Assert.assertEquals(0, bucket.getCount());
                    break;
            }
        }
        Assert.assertEquals("expecting 3 buckets", 3, i);
        Assert.assertEquals(Labels.of("path", "/"), data.getLabels());
        Assert.assertEquals(exemplar1.getValue(), data.getExemplars().get(128.0, 1024.0).getValue(), 0.0);
        Assert.assertEquals(createdTimestamp, data.getCreatedTimestampMillis());
        Assert.assertEquals(scrapeTimestamp, data.getScrapeTimestampMillis());

        // FixedHistogramData 2
        data = snapshot.getData().get(1);
        Assert.assertEquals(3, data.getCount());
        // skip the rest, because we covered it with other tests.
    }

    @Test
    public void testEmptyHistogram() {
        ClassicHistogramSnapshot snapshot = ClassicHistogramSnapshot.newBuilder()
                .withName("empty_histogram")
                .build();
        Assert.assertEquals(0, snapshot.getData().size());
    }

    @Test
    public void testMinimalHistogram() {
        ClassicHistogramSnapshot snapshot = ClassicHistogramSnapshot.newBuilder()
                .withName("minimal_histogram")
                .addData(ClassicHistogramData.newBuilder()
                        .withBuckets(ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{0}))
                        .build())
                .build();
        ClassicHistogramData data = snapshot.getData().get(0);
        Assert.assertFalse(data.hasSum());
        Assert.assertEquals(1, snapshot.getData().get(0).getBuckets().size());
    }

    @Test
    public void testCount() {
        ClassicHistogramSnapshot snapshot = ClassicHistogramSnapshot.newBuilder()
                .withName("test_histogram")
                .addData(ClassicHistogramData.newBuilder()
                        .withBuckets(ClassicHistogramBuckets.newBuilder()
                                .addBucket(1.0, 3)
                                .addBucket(2.0, 2)
                                .addBucket(Double.POSITIVE_INFINITY, 0)
                                .build())
                        .build())
                .build();
        ClassicHistogramData data = snapshot.getData().get(0);
        Assert.assertFalse(data.hasSum());
        Assert.assertTrue(data.hasCount());
        Assert.assertEquals(5, data.getCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyData() {
        ClassicHistogramData.newBuilder().build();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
        ClassicHistogramSnapshot snapshot = ClassicHistogramSnapshot.newBuilder()
                .withName("test_histogram")
                .addData(ClassicHistogramData.newBuilder()
                        .withLabels(Labels.of("a", "a"))
                        .withBuckets(ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{0}))
                        .build())
                .addData(ClassicHistogramData.newBuilder()
                        .withLabels(Labels.of("a", "b"))
                        .withBuckets(ClassicHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{2}))
                        .build())
                .build();
        Iterator<ClassicHistogramData> iterator = snapshot.getData().iterator();
        iterator.next();
        iterator.remove();
    }
}
