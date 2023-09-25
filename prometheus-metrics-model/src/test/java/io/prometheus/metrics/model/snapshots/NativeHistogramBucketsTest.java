package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class NativeHistogramBucketsTest {

    @Test
    public void testGoodCase() {
        NativeHistogramBuckets buckets = NativeHistogramBuckets.builder()
                .bucket(-10, 12)
                .bucket(120, 17)
                .build();
        Assert.assertEquals(2, buckets.size());
        Assert.assertEquals(-10, buckets.getBucketIndex(0));
        Assert.assertEquals(12, buckets.getCount(0));
        Assert.assertEquals(120, buckets.getBucketIndex(1));
        Assert.assertEquals(17, buckets.getCount(1));
    }

    @Test
    public void testEmpty() {
        NativeHistogramBuckets buckets = NativeHistogramBuckets.builder().build();
        Assert.assertEquals(0, buckets.size());
    }

    @Test
    public void testSort() {
        NativeHistogramBuckets buckets = NativeHistogramBuckets.builder()
                .bucket(7, 4)
                .bucket(2, 0)
                .bucket(5, 3)
                .build();
        Assert.assertEquals(3, buckets.size());
        Assert.assertEquals(2, buckets.getBucketIndex(0));
        Assert.assertEquals(5, buckets.getBucketIndex(1));
        Assert.assertEquals(7, buckets.getBucketIndex(2));
        Assert.assertEquals(0, buckets.getCount(0));
        Assert.assertEquals(3, buckets.getCount(1));
        Assert.assertEquals(4, buckets.getCount(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDifferentLength() {
        int[] bucketIndexes = new int[] {0, 1, 2};
        long[] cumulativeCounts = new long[] {13, 178, 1024, 3000};
        NativeHistogramBuckets.of(bucketIndexes, cumulativeCounts);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testImmutable() {
        NativeHistogramBuckets buckets = NativeHistogramBuckets.builder()
                .bucket(1, 1)
                .bucket(2, 1)
                .build();
        Iterator<NativeHistogramBucket> iterator = buckets.iterator();
        iterator.next();
        iterator.remove();
    }

}
