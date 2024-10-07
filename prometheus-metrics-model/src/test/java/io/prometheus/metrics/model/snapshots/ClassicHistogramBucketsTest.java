package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import java.util.Iterator;
import org.junit.Test;

public class ClassicHistogramBucketsTest {

  @Test
  public void testGoodCase() {
    ClassicHistogramBuckets buckets =
        ClassicHistogramBuckets.builder()
            .bucket(Double.NEGATIVE_INFINITY, 0)
            .bucket(-10.0, 7)
            .bucket(1024, 3)
            .bucket(Double.POSITIVE_INFINITY, 8)
            .build();
    assertThat(buckets.size()).isEqualTo(4);
  }

  @Test
  public void testSort() {
    ClassicHistogramBuckets buckets =
        ClassicHistogramBuckets.builder()
            .bucket(7, 2)
            .bucket(2, 0)
            .bucket(Double.POSITIVE_INFINITY, 3)
            .build();
    assertThat(buckets.size()).isEqualTo(3);
    assertThat(buckets.getUpperBound(0)).isCloseTo(2, offset(0.0));
    assertThat(buckets.getUpperBound(1)).isCloseTo(7, offset(0.0));
    assertThat(buckets.getUpperBound(2)).isCloseTo(Double.POSITIVE_INFINITY, offset(0.0));
    assertThat(buckets.getCount(0)).isZero();
    assertThat(buckets.getCount(1)).isEqualTo(2);
    assertThat(buckets.getCount(2)).isEqualTo(3);
  }

  @Test
  public void testMinimalBuckets() {
    ClassicHistogramBuckets buckets =
        ClassicHistogramBuckets.builder().bucket(Double.POSITIVE_INFINITY, 0).build();
    assertThat(buckets.size()).isOne();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInfBucketMissing() {
    ClassicHistogramBuckets.builder().bucket(Double.NEGATIVE_INFINITY, 0).build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeCount() {
    ClassicHistogramBuckets.builder().bucket(0.0, 10).bucket(Double.POSITIVE_INFINITY, -1).build();
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
    ClassicHistogramBuckets buckets =
        ClassicHistogramBuckets.builder()
            .bucket(1.0, 7)
            .bucket(2.0, 8)
            .bucket(Double.POSITIVE_INFINITY, 0)
            .build();
    Iterator<ClassicHistogramBucket> iterator = buckets.iterator();
    iterator.next();
    iterator.remove();
  }
}
