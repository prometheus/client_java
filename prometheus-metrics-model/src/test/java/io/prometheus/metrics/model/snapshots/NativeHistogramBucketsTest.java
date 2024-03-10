package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertThrows;

class NativeHistogramBucketsTest {

    @Test
    void testGoodCase() {
        NativeHistogramBuckets buckets = NativeHistogramBuckets.builder()
                .bucket(-10, 12)
                .bucket(120, 17)
                .build();
        Assertions.assertEquals(2, buckets.size());
        Assertions.assertEquals(-10, buckets.getBucketIndex(0));
        Assertions.assertEquals(12, buckets.getCount(0));
        Assertions.assertEquals(120, buckets.getBucketIndex(1));
        Assertions.assertEquals(17, buckets.getCount(1));
    }

    @Test
    void testEmpty() {
        NativeHistogramBuckets buckets = NativeHistogramBuckets.builder().build();
        Assertions.assertEquals(0, buckets.size());
    }

    @Test
    void testSort() {
        NativeHistogramBuckets buckets = NativeHistogramBuckets.builder()
                .bucket(7, 4)
                .bucket(2, 0)
                .bucket(5, 3)
                .build();
        Assertions.assertEquals(3, buckets.size());
        Assertions.assertEquals(2, buckets.getBucketIndex(0));
        Assertions.assertEquals(5, buckets.getBucketIndex(1));
        Assertions.assertEquals(7, buckets.getBucketIndex(2));
        Assertions.assertEquals(0, buckets.getCount(0));
        Assertions.assertEquals(3, buckets.getCount(1));
        Assertions.assertEquals(4, buckets.getCount(2));
    }

    @Test
    void testDifferentLength() {
        int[] bucketIndexes = new int[] {0, 1, 2};
        long[] cumulativeCounts = new long[] {13, 178, 1024, 3000};
        assertThrows(IllegalArgumentException.class, () -> NativeHistogramBuckets.of(bucketIndexes, cumulativeCounts));
    }

    @Test
    void testImmutable() {
        NativeHistogramBuckets buckets = NativeHistogramBuckets.builder()
                .bucket(1, 1)
                .bucket(2, 1)
                .build();
        Iterator<NativeHistogramBucket> iterator = buckets.iterator();
        iterator.next();
        Assertions.assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

}
