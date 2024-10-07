package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import org.junit.jupiter.api.Test;

public class NativeHistogramBucketsTest {

  @Test
  public void testGoodCase() {
    NativeHistogramBuckets buckets =
        NativeHistogramBuckets.builder().bucket(-10, 12).bucket(120, 17).build();
    assertThat(buckets.size()).isEqualTo(2);
    assertThat(buckets.getBucketIndex(0)).isEqualTo(-10);
    assertThat(buckets.getCount(0)).isEqualTo(12);
    assertThat(buckets.getBucketIndex(1)).isEqualTo(120);
    assertThat(buckets.getCount(1)).isEqualTo(17);
  }

  @Test
  public void testEmpty() {
    NativeHistogramBuckets buckets = NativeHistogramBuckets.builder().build();
    assertThat(buckets.size()).isZero();
  }

  @Test
  public void testSort() {
    NativeHistogramBuckets buckets =
        NativeHistogramBuckets.builder().bucket(7, 4).bucket(2, 0).bucket(5, 3).build();
    assertThat(buckets.size()).isEqualTo(3);
    assertThat(buckets.getBucketIndex(0)).isEqualTo(2);
    assertThat(buckets.getBucketIndex(1)).isEqualTo(5);
    assertThat(buckets.getBucketIndex(2)).isEqualTo(7);
    assertThat(buckets.getCount(0)).isZero();
    assertThat(buckets.getCount(1)).isEqualTo(3);
    assertThat(buckets.getCount(2)).isEqualTo(4);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDifferentLength() {
    int[] bucketIndexes = new int[] {0, 1, 2};
    long[] cumulativeCounts = new long[] {13, 178, 1024, 3000};
    NativeHistogramBuckets.of(bucketIndexes, cumulativeCounts);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testImmutable() {
    NativeHistogramBuckets buckets =
        NativeHistogramBuckets.builder().bucket(1, 1).bucket(2, 1).build();
    Iterator<NativeHistogramBucket> iterator = buckets.iterator();
    iterator.next();
    iterator.remove();
  }
}
