package io.prometheus.metrics.model;

import io.prometheus.metrics.model.FixedHistogramSnapshot.FixedHistogramData;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class FixedHistogramSnapshotTest {

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
        FixedHistogramSnapshot snapshot = FixedHistogramSnapshot.newBuilder()
                .withName("request_size_bytes")
                .withHelp("request sizes in bytes")
                .withUnit(Unit.BYTES)
                .addData(
                        FixedHistogramData.newBuilder()
                                .withCount(12)
                                .withSum(27000.0)
                                .withBuckets(FixedHistogramBuckets.newBuilder()
                                        .addBucket(Double.POSITIVE_INFINITY, 12)
                                        .addBucket(128.0, 7)
                                        .addBucket(1024.0, 10)
                                        .build())
                                .withLabels(Labels.of("path", "/"))
                                .withExemplars(Exemplars.of(exemplar1))
                                .withCreatedTimestampMillis(createdTimestamp)
                                .withScrapeTimestampMillis(scrapeTimestamp)
                                .build())
                .addData(
                        FixedHistogramData.newBuilder()
                                .withCount(3)
                                .withSum(400.2)
                                .withBuckets(FixedHistogramBuckets.newBuilder()
                                        .addBucket(128.0, 0)
                                        .addBucket(1024.0, 2)
                                        .addBucket(Double.POSITIVE_INFINITY, 2)
                                        .build())
                                .withLabels(Labels.of("path", "/api/v1"))
                                .withExemplars(Exemplars.of(exemplar1))
                                .withCreatedTimestampMillis(createdTimestamp)
                                .withScrapeTimestampMillis(scrapeTimestamp)
                                .build())
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "request_size_bytes", "request sizes in bytes", "bytes");

        Assert.assertEquals(2, snapshot.getData().size());
        FixedHistogramData data = snapshot.getData().get(0); // data is sorted by labels, so the first one should be path="/"
        Assert.assertEquals(12, data.getCount());
        Assert.assertEquals(27000.0, data.getSum(), 0.0);
        int i = 0;
        for (FixedHistogramBucket bucket : data.getBuckets()) {
            switch (i++) {
                case 0:
                    Assert.assertEquals(128.0, bucket.getUpperBound(), 0.0);
                    Assert.assertEquals(7, bucket.getCumulativeCount());
                    break;
                case 1:
                    Assert.assertEquals(1024.0, bucket.getUpperBound(), 0.0);
                    Assert.assertEquals(10, bucket.getCumulativeCount());
                    break;
                case 2:
                    Assert.assertEquals(Double.POSITIVE_INFINITY, bucket.getUpperBound(), 0.0);
                    Assert.assertEquals(12, bucket.getCumulativeCount());
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
        FixedHistogramSnapshot snapshot = FixedHistogramSnapshot.newBuilder()
                .withName("empty_histogram")
                .build();
        Assert.assertEquals(0, snapshot.getData().size());
    }

    @Test
    public void testMinimalHistogram() {
        FixedHistogramSnapshot snapshot = FixedHistogramSnapshot.newBuilder()
                .withName("minimal_histogram")
                .addData(FixedHistogramData.newBuilder()
                        .withBuckets(FixedHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{0}))
                        .build())
                .build();
        FixedHistogramData data = snapshot.getData().get(0);
        Assert.assertFalse(data.hasCount());
        Assert.assertFalse(data.hasSum());
        Assert.assertEquals(1, snapshot.getData().get(0).getBuckets().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyData() {
        FixedHistogramData.newBuilder().build();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
        FixedHistogramSnapshot snapshot = FixedHistogramSnapshot.newBuilder()
                .withName("test_histogram")
                .addData(FixedHistogramData.newBuilder()
                        .withLabels(Labels.of("a", "a"))
                        .withBuckets(FixedHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{0}))
                        .build())
                .addData(FixedHistogramData.newBuilder()
                        .withLabels(Labels.of("a", "b"))
                        .withBuckets(FixedHistogramBuckets.of(new double[]{Double.POSITIVE_INFINITY}, new long[]{2}))
                        .build())
                .build();
        Iterator<FixedHistogramSnapshot.FixedHistogramData> iterator = snapshot.getData().iterator();
        iterator.next();
        iterator.remove();
    }
}
