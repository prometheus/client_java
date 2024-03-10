package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ClassicHistogramBucketsTest {

    @Test
    void testGoodCase() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.builder()
                .bucket(Double.NEGATIVE_INFINITY, 0)
                .bucket(-10.0, 7)
                .bucket(1024, 3)
                .bucket(Double.POSITIVE_INFINITY, 8)
                .build();
        Assertions.assertEquals(4, buckets.size());
    }

    @Test
    void testSort() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.builder()
                .bucket(7, 2)
                .bucket(2, 0)
                .bucket(Double.POSITIVE_INFINITY, 3)
                .build();
        Assertions.assertEquals(3, buckets.size());
        Assertions.assertEquals(2, buckets.getUpperBound(0), 0.0);
        Assertions.assertEquals(7, buckets.getUpperBound(1), 0.0);
        Assertions.assertEquals(Double.POSITIVE_INFINITY, buckets.getUpperBound(2), 0.0);
        Assertions.assertEquals(0, buckets.getCount(0));
        Assertions.assertEquals(2, buckets.getCount(1));
        Assertions.assertEquals(3, buckets.getCount(2));
    }

    @Test
    void testMinimalBuckets() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.builder()
                .bucket(Double.POSITIVE_INFINITY, 0)
                .build();
        Assertions.assertEquals(1, buckets.size());
    }

    @Test
    void testInfBucketMissing() {
        assertThrows(IllegalArgumentException.class,
                () -> ClassicHistogramBuckets.builder()
                        .bucket(Double.NEGATIVE_INFINITY, 0)
                        .build());
    }

    @Test
    void testNegativeCount() {
        assertThrows(IllegalArgumentException.class,
                () -> ClassicHistogramBuckets.builder()
                        .bucket(0.0, 10)
                        .bucket(Double.POSITIVE_INFINITY, -1)
                        .build());
    }

    @Test
    void testNaNBoundary() {
        assertThrows(IllegalArgumentException.class,
                () -> ClassicHistogramBuckets.builder()
                        .bucket(0.0, 1)
                        .bucket(Double.NaN, 2)
                        .bucket(Double.POSITIVE_INFINITY, 0)
                        .build());
    }

    @Test
    void testDuplicateBoundary() {
        assertThrows(IllegalArgumentException.class,
                () -> ClassicHistogramBuckets.builder()
                        .bucket(1.0, 1)
                        .bucket(2.0, 2)
                        .bucket(1.0, 2)
                        .bucket(Double.POSITIVE_INFINITY, 0)
                        .build());
    }

    @Test
    void testEmptyBuckets() {
        assertThrows(IllegalArgumentException.class, () -> ClassicHistogramBuckets.builder().build());
    }

    @Test
    void testDifferentLength() {
        double[] upperBounds = new double[] {0.7, 1.3, Double.POSITIVE_INFINITY};
        long[] counts = new long[] {13, 178, 1024, 3000};
        assertThrows(IllegalArgumentException.class, () -> ClassicHistogramBuckets.of(upperBounds, counts));
    }

    @Test
    void testImmutable() {
        ClassicHistogramBuckets buckets = ClassicHistogramBuckets.builder()
                .bucket(1.0, 7)
                .bucket(2.0, 8)
                .bucket(Double.POSITIVE_INFINITY, 0)
                .build();
        Iterator<ClassicHistogramBucket> iterator = buckets.iterator();
        iterator.next();
        assertThrows(UnsupportedOperationException.class, iterator::remove);
    }
}
