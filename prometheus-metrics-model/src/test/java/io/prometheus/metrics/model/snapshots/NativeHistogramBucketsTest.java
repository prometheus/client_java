package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class NativeHistogramBucketsTest {

  @Test
  void testGoodCase() {
    NativeHistogramBuckets buckets =
        NativeHistogramBuckets.builder().bucket(-10, 12).bucket(120, 17).build();
    assertThat(buckets.size()).isEqualTo(2);
    assertThat(buckets.getBucketIndex(0)).isEqualTo(-10);
    assertThat(buckets.getCount(0)).isEqualTo(12);
    assertThat(buckets.getBucketIndex(1)).isEqualTo(120);
    assertThat(buckets.getCount(1)).isEqualTo(17);
  }

  @Test
  void testEmpty() {
    NativeHistogramBuckets buckets = NativeHistogramBuckets.builder().build();
    assertThat(buckets.size()).isZero();
  }

  @Test
  void testSort() {
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

  @Test
  void testDifferentLength() {
    int[] bucketIndexes = new int[] {0, 1, 2};
    long[] cumulativeCounts = new long[] {13, 178, 1024, 3000};
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> NativeHistogramBuckets.of(bucketIndexes, cumulativeCounts));
  }

  @Test
  void testImmutable() {
    NativeHistogramBuckets buckets =
        NativeHistogramBuckets.builder().bucket(1, 1).bucket(2, 1).build();
    Iterator<NativeHistogramBucket> iterator = buckets.iterator();
    iterator.next();
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(iterator::remove);
  }

  @Test
  void testSortSmallInputMaintainsPairs() {
    int size = 5;
    int[] bucketIndexes = new int[size];
    long[] counts = new long[size];
    Map<Integer, Long> expectedCounts = new HashMap<>();
    for (int i = 0; i < size; i++) {
      bucketIndexes[i] = (i * 3) - 5;
      counts[i] = 100L + i;
      expectedCounts.put(bucketIndexes[i], counts[i]);
    }

    Random random = new Random(12L);
    for (int i = size - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      int bucketIndex = bucketIndexes[i];
      bucketIndexes[i] = bucketIndexes[j];
      bucketIndexes[j] = bucketIndex;
      long count = counts[i];
      counts[i] = counts[j];
      counts[j] = count;
    }

    NativeHistogramBuckets buckets = NativeHistogramBuckets.of(bucketIndexes, counts);
    for (int i = 1; i < buckets.size(); i++) {
      assertThat(buckets.getBucketIndex(i - 1)).isLessThan(buckets.getBucketIndex(i));
    }
    for (int i = 0; i < buckets.size(); i++) {
      assertThat(buckets.getCount(i)).isEqualTo(expectedCounts.get(buckets.getBucketIndex(i)));
    }
  }

  @Test
  void testSortMediumInputMaintainsPairs() {
    int size = 25;
    int[] bucketIndexes = new int[size];
    long[] counts = new long[size];
    Map<Integer, Long> expectedCounts = new HashMap<>();
    for (int i = 0; i < size; i++) {
      bucketIndexes[i] = (i * 3) - 25;
      counts[i] = 1000L + i;
      expectedCounts.put(bucketIndexes[i], counts[i]);
    }

    Random random = new Random(13L);
    for (int i = size - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      int bucketIndex = bucketIndexes[i];
      bucketIndexes[i] = bucketIndexes[j];
      bucketIndexes[j] = bucketIndex;
      long count = counts[i];
      counts[i] = counts[j];
      counts[j] = count;
    }

    NativeHistogramBuckets buckets = NativeHistogramBuckets.of(bucketIndexes, counts);
    for (int i = 1; i < buckets.size(); i++) {
      assertThat(buckets.getBucketIndex(i - 1)).isLessThan(buckets.getBucketIndex(i));
    }
    for (int i = 0; i < buckets.size(); i++) {
      assertThat(buckets.getCount(i)).isEqualTo(expectedCounts.get(buckets.getBucketIndex(i)));
    }
  }

  @Test
  void testSortLargeInputMaintainsPairs() {
    int size = 64;
    int[] bucketIndexes = new int[size];
    long[] counts = new long[size];
    Map<Integer, Long> expectedCounts = new HashMap<>();
    for (int i = 0; i < size; i++) {
      bucketIndexes[i] = (i * 3) - 50;
      counts[i] = 1000L + i;
      expectedCounts.put(bucketIndexes[i], counts[i]);
    }

    Random random = new Random(2L);
    for (int i = size - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      int bucketIndex = bucketIndexes[i];
      bucketIndexes[i] = bucketIndexes[j];
      bucketIndexes[j] = bucketIndex;
      long count = counts[i];
      counts[i] = counts[j];
      counts[j] = count;
    }

    NativeHistogramBuckets buckets = NativeHistogramBuckets.of(bucketIndexes, counts);
    for (int i = 1; i < buckets.size(); i++) {
      assertThat(buckets.getBucketIndex(i - 1)).isLessThan(buckets.getBucketIndex(i));
    }
    for (int i = 0; i < buckets.size(); i++) {
      assertThat(buckets.getCount(i)).isEqualTo(expectedCounts.get(buckets.getBucketIndex(i)));
    }
  }
}
