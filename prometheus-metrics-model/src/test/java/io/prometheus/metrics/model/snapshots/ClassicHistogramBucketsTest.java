package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class ClassicHistogramBucketsTest {

    @Test
    public void testGoodCase() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.builder()
                .bucket(Double.NEGATIVE_INFINITY, 0)
                .bucket(-10.0, 7)
                .bucket(1024, 3)
                .bucket(Double.POSITIVE_INFINITY, 8)
                .build();
        Assert.assertEquals(4, buckets.size());
    }

    @Test
    public void testSort() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.builder()
                .bucket(7, 2)
                .bucket(2, 0)
                .bucket(Double.POSITIVE_INFINITY, 3)
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
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.builder()
                .bucket(Double.POSITIVE_INFINITY, 0)
                .build();
        Assert.assertEquals(1, buckets.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInfBucketMissing() {
        ClassicHistogramBuckets.builder()
                .bucket(Double.NEGATIVE_INFINITY, 0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeCount() {
        ClassicHistogramBuckets.builder()
                .bucket(0.0, 10)
                .bucket(Double.POSITIVE_INFINITY, -1)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNaNBoundary() {
        ClassicHistogramBuckets.builder()
                .bucket(0.0, 1)
                .bucket(Double.NaN, 2)
                .bucket(Double.POSITIVE_INFINITY, 0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateBoundary() {
        ClassicHistogramBuckets.builder()
                .bucket(1.0, 1)
                .bucket(2.0, 2)
                .bucket(1.0, 2)
                .bucket(Double.POSITIVE_INFINITY, 0)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyBuckets() {
        ClassicHistogramBuckets.builder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDifferentLength() {
        double[] upperBounds = new double[] {0.7, 1.3, Double.POSITIVE_INFINITY};
        long[] counts = new long[] {13, 178, 1024, 3000};
        ClassicHistogramBuckets.of(upperBounds, counts);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutable() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.builder()
                .bucket(1.0, 7)
                .bucket(2.0, 8)
                .bucket(Double.POSITIVE_INFINITY, 0)
                .build();
        Iterator<ClassicHistogramBucket> iterator = buckets.iterator();
        iterator.next();
        iterator.remove();
    }
}
