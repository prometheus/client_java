package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ClassicHistogramBucketsTest {

  @Test
  void testGoodCase() {
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
  void testSort() {
    ClassicHistogramBuckets buckets =
        ClassicHistogramBuckets.builder()
            .bucket(7, 2)
            .bucket(2, 0)
            .bucket(Double.POSITIVE_INFINITY, 3)
            .build();
    assertThat(buckets.size()).isEqualTo(3);
    assertThat(buckets.getUpperBound(0)).isEqualTo(2);
    assertThat(buckets.getUpperBound(1)).isEqualTo(7);
    assertThat(buckets.getUpperBound(2)).isEqualTo(Double.POSITIVE_INFINITY);
    assertThat(buckets.getCount(0)).isZero();
    assertThat(buckets.getCount(1)).isEqualTo(2);
    assertThat(buckets.getCount(2)).isEqualTo(3);
  }

  @Test
  void testMinimalBuckets() {
    ClassicHistogramBuckets buckets =
        ClassicHistogramBuckets.builder().bucket(Double.POSITIVE_INFINITY, 0).build();
    assertThat(buckets.size()).isOne();
  }

  @Test
  void testInfBucketMissing() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () -> ClassicHistogramBuckets.builder().bucket(Double.NEGATIVE_INFINITY, 0).build());
  }

  @Test
  void testNegativeCount() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                ClassicHistogramBuckets.builder()
                    .bucket(0.0, 10)
                    .bucket(Double.POSITIVE_INFINITY, -1)
                    .build());
  }

  @Test
  void testNaNBoundary() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                ClassicHistogramBuckets.builder()
                    .bucket(0.0, 1)
                    .bucket(Double.NaN, 2)
                    .bucket(Double.POSITIVE_INFINITY, 0)
                    .build());
  }

  @Test
  void testDuplicateBoundary() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                ClassicHistogramBuckets.builder()
                    .bucket(1.0, 1)
                    .bucket(2.0, 2)
                    .bucket(1.0, 2)
                    .bucket(Double.POSITIVE_INFINITY, 0)
                    .build());
  }

  @Test
  void testEmptyBuckets() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> ClassicHistogramBuckets.builder().build());
  }

  @Test
  void testDifferentLength() {
    double[] upperBounds = new double[] {0.7, 1.3, Double.POSITIVE_INFINITY};
    long[] counts = new long[] {13, 178, 1024, 3000};
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> ClassicHistogramBuckets.of(upperBounds, counts));
  }

  @Test
  void testImmutable() {
    ClassicHistogramBuckets buckets =
        ClassicHistogramBuckets.builder()
            .bucket(1.0, 7)
            .bucket(2.0, 8)
            .bucket(Double.POSITIVE_INFINITY, 0)
            .build();
    Iterator<ClassicHistogramBucket> iterator = buckets.iterator();
    iterator.next();
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(iterator::remove);
  }

  @Test
  void compare() {
    ClassicHistogramBuckets buckets =
        ClassicHistogramBuckets.builder()
            .bucket(1.0, 7)
            .bucket(2.0, 8)
            .bucket(Double.POSITIVE_INFINITY, 0)
            .build();
    List<ClassicHistogramBucket> list = buckets.stream().collect(Collectors.toList());
    assertThat(list.get(0)).isNotEqualByComparingTo(list.get(1));
  }

  @Test
  void testSortSmallInputMaintainsPairs() {
    int size = 5;
    double[] upperBounds = new double[size];
    long[] counts = new long[size];
    Map<Double, Long> expectedCounts = new HashMap<>();
    for (int i = 0; i < size - 1; i++) {
      upperBounds[i] = i + 0.5;
      counts[i] = 100L + i;
      expectedCounts.put(upperBounds[i], counts[i]);
    }
    upperBounds[size - 1] = Double.POSITIVE_INFINITY;
    counts[size - 1] = 200L;
    expectedCounts.put(upperBounds[size - 1], counts[size - 1]);

    Random random = new Random(10L);
    for (int i = size - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      double upperBound = upperBounds[i];
      upperBounds[i] = upperBounds[j];
      upperBounds[j] = upperBound;
      long count = counts[i];
      counts[i] = counts[j];
      counts[j] = count;
    }

    ClassicHistogramBuckets buckets = ClassicHistogramBuckets.of(upperBounds, counts);
    for (int i = 1; i < buckets.size(); i++) {
      assertThat(buckets.getUpperBound(i - 1)).isLessThan(buckets.getUpperBound(i));
    }
    for (int i = 0; i < buckets.size(); i++) {
      assertThat(buckets.getCount(i)).isEqualTo(expectedCounts.get(buckets.getUpperBound(i)));
    }
  }

  @Test
  void testSortMediumInputMaintainsPairs() {
    int size = 25;
    double[] upperBounds = new double[size];
    long[] counts = new long[size];
    Map<Double, Long> expectedCounts = new HashMap<>();
    for (int i = 0; i < size - 1; i++) {
      upperBounds[i] = i + 0.125;
      counts[i] = 1000L + i;
      expectedCounts.put(upperBounds[i], counts[i]);
    }
    upperBounds[size - 1] = Double.POSITIVE_INFINITY;
    counts[size - 1] = 2000L;
    expectedCounts.put(upperBounds[size - 1], counts[size - 1]);

    Random random = new Random(11L);
    for (int i = size - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      double upperBound = upperBounds[i];
      upperBounds[i] = upperBounds[j];
      upperBounds[j] = upperBound;
      long count = counts[i];
      counts[i] = counts[j];
      counts[j] = count;
    }

    ClassicHistogramBuckets buckets = ClassicHistogramBuckets.of(upperBounds, counts);
    for (int i = 1; i < buckets.size(); i++) {
      assertThat(buckets.getUpperBound(i - 1)).isLessThan(buckets.getUpperBound(i));
    }
    for (int i = 0; i < buckets.size(); i++) {
      assertThat(buckets.getCount(i)).isEqualTo(expectedCounts.get(buckets.getUpperBound(i)));
    }
  }

  @Test
  void testSortLargeInputMaintainsPairs() {
    int size = 64;
    double[] upperBounds = new double[size];
    long[] counts = new long[size];
    Map<Double, Long> expectedCounts = new HashMap<>();
    for (int i = 0; i < size - 1; i++) {
      upperBounds[i] = i + 0.125;
      counts[i] = 1000L + i;
      expectedCounts.put(upperBounds[i], counts[i]);
    }
    upperBounds[size - 1] = Double.POSITIVE_INFINITY;
    counts[size - 1] = 2000L;
    expectedCounts.put(upperBounds[size - 1], counts[size - 1]);

    Random random = new Random(1L);
    for (int i = size - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      double upperBound = upperBounds[i];
      upperBounds[i] = upperBounds[j];
      upperBounds[j] = upperBound;
      long count = counts[i];
      counts[i] = counts[j];
      counts[j] = count;
    }

    ClassicHistogramBuckets buckets = ClassicHistogramBuckets.of(upperBounds, counts);
    for (int i = 1; i < buckets.size(); i++) {
      assertThat(buckets.getUpperBound(i - 1)).isLessThan(buckets.getUpperBound(i));
    }
    for (int i = 0; i < buckets.size(); i++) {
      assertThat(buckets.getCount(i)).isEqualTo(expectedCounts.get(buckets.getUpperBound(i)));
    }
  }
}
