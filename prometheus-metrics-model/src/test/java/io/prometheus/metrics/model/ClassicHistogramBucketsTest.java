package io.prometheus.metrics.model;

import org.junit.Assert;
import org.junit.Test;

public class ClassicHistogramBucketsTest {

    @Test
    public void testGoodCase() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.newBuilder()
                .addBucket(Double.NEGATIVE_INFINITY, 0)
                .addBucket(-10.0, 7)
                .addBucket(1024, 8)
                .addBucket(Double.POSITIVE_INFINITY, 8)
                .build();
        Assert.assertEquals(4, buckets.size());
    }

    @Test
    public void testSort() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.newBuilder()
                .addBucket(7, 2)
                .addBucket(2, 0)
                .addBucket(Double.POSITIVE_INFINITY, 3)
                .build();
        Assert.assertEquals(3, buckets.size());
        Assert.assertEquals(2, buckets.getUpperBound(0), 0.0);
        Assert.assertEquals(7, buckets.getUpperBound(1), 0.0);
        Assert.assertEquals(Double.POSITIVE_INFINITY, buckets.getUpperBound(2), 0.0);
        Assert.assertEquals(0, buckets.getCumulativeCount(0));
        Assert.assertEquals(2, buckets.getCumulativeCount(1));
        Assert.assertEquals(3, buckets.getCumulativeCount(2));
    }

    @Test
    public void testMinimalBuckets() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.newBuilder()
                .addBucket(Double.POSITIVE_INFINITY, 0)
                .build();
        Assert.assertEquals(1, buckets.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInfBucketMissing() {
        ClassicHistogramBuckets.newBuilder()
                .addBucket(Double.NEGATIVE_INFINITY, 0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotCumulative() {
        ClassicHistogramBuckets.newBuilder()
                .addBucket(0.0, 10)
                .addBucket(Double.POSITIVE_INFINITY, 7)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyBuckets() {
        ClassicHistogramBuckets.newBuilder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDifferentLength() {
        double[] upperBounds = new double[] {0.7, 1.3, Double.POSITIVE_INFINITY};
        long[] cumulativeCounts = new long[] {13, 178, 1024, 3000};
        ClassicHistogramBuckets.of(upperBounds, cumulativeCounts);
    }
}
