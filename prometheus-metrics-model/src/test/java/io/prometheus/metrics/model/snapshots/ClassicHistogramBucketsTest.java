package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.ClassicHistogramBucket;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class ClassicHistogramBucketsTest {

    @Test
    public void testGoodCase() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.newBuilder()
                .addBucket(Double.NEGATIVE_INFINITY, 0)
                .addBucket(-10.0, 7)
                .addBucket(1024, 3)
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
        Assert.assertEquals(0, buckets.getCount(0));
        Assert.assertEquals(2, buckets.getCount(1));
        Assert.assertEquals(3, buckets.getCount(2));
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
    public void testNegativeCount() {
        ClassicHistogramBuckets.newBuilder()
                .addBucket(0.0, 10)
                .addBucket(Double.POSITIVE_INFINITY, -1)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNaNBoundary() {
        ClassicHistogramBuckets.newBuilder()
                .addBucket(0.0, 1)
                .addBucket(Double.NaN, 2)
                .addBucket(Double.POSITIVE_INFINITY, 0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateBoundary() {
        ClassicHistogramBuckets.newBuilder()
                .addBucket(1.0, 1)
                .addBucket(2.0, 2)
                .addBucket(1.0, 2)
                .addBucket(Double.POSITIVE_INFINITY, 0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyBuckets() {
        ClassicHistogramBuckets.newBuilder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDifferentLength() {
        double[] upperBounds = new double[] {0.7, 1.3, Double.POSITIVE_INFINITY};
        long[] counts = new long[] {13, 178, 1024, 3000};
        ClassicHistogramBuckets.of(upperBounds, counts);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutable() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.newBuilder()
                .addBucket(1.0, 7)
                .addBucket(2.0, 8)
                .addBucket(Double.POSITIVE_INFINITY, 0)
                .build();
        Iterator<ClassicHistogramBucket> iterator = buckets.iterator();
        iterator.next();
        iterator.remove();
    }
}
