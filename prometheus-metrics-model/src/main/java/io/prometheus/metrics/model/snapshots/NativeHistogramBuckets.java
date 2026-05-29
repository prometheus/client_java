package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Immutable representation of native histogram buckets.
 *
 * <p>The bucket index defines the boundaries of the bucket, depending on the histogram's {@link
 * HistogramSnapshot.HistogramDataPointSnapshot#getNativeSchema() schema}.
 *
 * <pre>
 *     base = 2^(2^-schema)
 *     lower bound = base^(index - 1)
 *     upper bound = base^index
 * </pre>
 */
public class NativeHistogramBuckets implements Iterable<NativeHistogramBucket> {

  public static final NativeHistogramBuckets EMPTY =
      new NativeHistogramBuckets(new int[] {}, new long[] {});
  private final int[] bucketIndexes; // sorted
  private final long[] counts;

  private NativeHistogramBuckets(int[] bucketIndexes, long[] counts) {
    this.bucketIndexes = bucketIndexes;
    this.counts = counts;
  }

  /**
   * To create a new {@link NativeHistogramBuckets} instance, you can either use one of the static
   * {@code of(...)} methods, or use {@link NativeHistogramBuckets#builder()}.
   *
   * @param bucketIndexes see class javadoc of {@link NativeHistogramBuckets}. May be empty.
   * @param counts must have the same length as bucketIndexes
   */
  public static NativeHistogramBuckets of(int[] bucketIndexes, long[] counts) {
    int[] bucketIndexesCopy = Arrays.copyOf(bucketIndexes, bucketIndexes.length);
    long[] countsCopy = Arrays.copyOf(counts, counts.length);
    sortAndValidate(bucketIndexesCopy, countsCopy);
    return new NativeHistogramBuckets(bucketIndexesCopy, countsCopy);
  }

  /**
   * To create a new {@link NativeHistogramBuckets} instance, you can either use one of the static
   * {@code of(...)} methods, or use {@link NativeHistogramBuckets#builder()}.
   *
   * @param bucketIndexes see class javadoc of {@link NativeHistogramBuckets}. May be empty.
   * @param counts must have the same size as bucketIndexes
   */
  public static NativeHistogramBuckets of(List<Integer> bucketIndexes, List<Long> counts) {
    int[] bucketIndexesCopy = new int[bucketIndexes.size()];
    for (int i = 0; i < bucketIndexes.size(); i++) {
      bucketIndexesCopy[i] = bucketIndexes.get(i);
    }
    long[] countsCopy = new long[counts.size()];
    for (int i = 0; i < counts.size(); i++) {
      countsCopy[i] = counts.get(i);
    }
    sortAndValidate(bucketIndexesCopy, countsCopy);
    return new NativeHistogramBuckets(bucketIndexesCopy, countsCopy);
  }

  public int size() {
    return bucketIndexes.length;
  }

  private List<NativeHistogramBucket> asList() {
    List<NativeHistogramBucket> result = new ArrayList<>(size());
    for (int i = 0; i < bucketIndexes.length; i++) {
      result.add(new NativeHistogramBucket(bucketIndexes[i], counts[i]));
    }
    return Collections.unmodifiableList(result);
  }

  @Override
  public Iterator<NativeHistogramBucket> iterator() {
    return asList().iterator();
  }

  public Stream<NativeHistogramBucket> stream() {
    return asList().stream();
  }

  public int getBucketIndex(int i) {
    return bucketIndexes[i];
  }

  public long getCount(int i) {
    return counts[i];
  }

  private static void sortAndValidate(int[] bucketIndexes, long[] counts) {
    if (bucketIndexes.length != counts.length) {
      throw new IllegalArgumentException(
          "bucketIndexes.length == "
              + bucketIndexes.length
              + " but counts.length == "
              + counts.length
              + ". Expected the same length.");
    }
    sort(bucketIndexes, counts);
    validate(bucketIndexes, counts);
  }

  /**
   * Sorts bucketIndexes and counts in place using introspective quicksort.
   *
   * <p>Algorithm: 3-way quicksort with insertion sort for tiny partitions and heapsort fallback at
   * the recursion depth limit. Parallel arrays are swapped in lockstep.
   *
   * <p>Complexity: O(n log n) average and worst case.
   */
  private static void sort(int[] bucketIndexes, long[] counts) {
    IntArraySorter.sort(bucketIndexes, counts);
  }

  private static void validate(int[] bucketIndexes, long[] counts) {
    // Preconditions:
    // * bucketIndexes sorted
    // * bucketIndexes and counts have the same length
    for (int i = 0; i < bucketIndexes.length; i++) {
      if (counts[i] < 0) {
        throw new IllegalArgumentException("Bucket counts cannot be negative.");
      }
      if (i > 0) {
        if (bucketIndexes[i - 1] == bucketIndexes[i]) {
          throw new IllegalArgumentException("Duplicate bucket index " + bucketIndexes[i]);
        }
      }
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private final List<Integer> bucketIndexes = new ArrayList<>();
    private final List<Long> counts = new ArrayList<>();

    private Builder() {}

    /** Add a native histogram bucket. Call multiple times to add multiple buckets. */
    public Builder bucket(int bucketIndex, long count) {
      bucketIndexes.add(bucketIndex);
      counts.add(count);
      return this;
    }

    public NativeHistogramBuckets build() {
      return NativeHistogramBuckets.of(bucketIndexes, counts);
    }
  }

  /**
   * In-place introsort for {@code bucketIndexes} and parallel {@code counts}.
   *
   * <p>Uses 3-way quicksort partitioning for large ranges, insertion sort for tiny ranges, and a
   * heapsort fallback at the recursion-depth limit to guarantee O(n log n) worst-case complexity.
   */
  private static final class IntArraySorter {

    private static final int INSERTION_SORT_THRESHOLD = 24;

    private static void sort(int[] bucketIndexes, long[] counts) {
      int right = bucketIndexes.length - 1;
      if (right <= 0) {
        return;
      }
      introSort(bucketIndexes, counts, 0, right, depthLimit(bucketIndexes.length));
    }

    private static void introSort(
        int[] bucketIndexes, long[] counts, int left, int right, int depthLimit) {
      while (left < right) {
        if (right - left + 1 <= INSERTION_SORT_THRESHOLD) {
          insertionSort(bucketIndexes, counts, left, right);
          return;
        }
        if (depthLimit == 0) {
          heapSort(bucketIndexes, counts, left, right);
          return;
        }
        depthLimit--;

        int mid = left + ((right - left) >>> 1);
        int pivotIndex = medianOf3(bucketIndexes, left, mid, right);
        int pivot = bucketIndexes[pivotIndex];

        int lt = left;
        int i = left;
        int gt = right;
        while (i <= gt) {
          int cmp = compare(bucketIndexes[i], pivot);
          if (cmp < 0) {
            swap(i, lt, bucketIndexes, counts);
            i++;
            lt++;
          } else if (cmp > 0) {
            swap(i, gt, bucketIndexes, counts);
            gt--;
          } else {
            i++;
          }
        }

        if (lt - left < right - gt) {
          introSort(bucketIndexes, counts, left, lt - 1, depthLimit);
          left = gt + 1;
        } else {
          introSort(bucketIndexes, counts, gt + 1, right, depthLimit);
          right = lt - 1;
        }
      }
    }

    private static void insertionSort(int[] bucketIndexes, long[] counts, int left, int right) {
      for (int i = left + 1; i <= right; i++) {
        int bucketIndex = bucketIndexes[i];
        long count = counts[i];
        int j = i - 1;
        while (j >= left && compare(bucketIndexes[j], bucketIndex) > 0) {
          bucketIndexes[j + 1] = bucketIndexes[j];
          counts[j + 1] = counts[j];
          j--;
        }
        bucketIndexes[j + 1] = bucketIndex;
        counts[j + 1] = count;
      }
    }

    private static void heapSort(int[] bucketIndexes, long[] counts, int left, int right) {
      int size = right - left + 1;
      for (int i = (size >>> 1) - 1; i >= 0; i--) {
        siftDown(bucketIndexes, counts, left, i, size);
      }
      for (int end = size - 1; end > 0; end--) {
        swap(left, left + end, bucketIndexes, counts);
        siftDown(bucketIndexes, counts, left, 0, end);
      }
    }

    private static void siftDown(int[] bucketIndexes, long[] counts, int base, int root, int size) {
      while (true) {
        int child = (root << 1) + 1;
        if (child >= size) {
          return;
        }
        int rightChild = child + 1;
        if (rightChild < size
            && compare(bucketIndexes[base + child], bucketIndexes[base + rightChild]) < 0) {
          child = rightChild;
        }
        if (compare(bucketIndexes[base + root], bucketIndexes[base + child]) >= 0) {
          return;
        }
        swap(base + root, base + child, bucketIndexes, counts);
        root = child;
      }
    }

    private static int depthLimit(int length) {
      int result = 0;
      while (length > 1) {
        result++;
        length >>>= 1;
      }
      return result << 1;
    }

    private static int medianOf3(int[] bucketIndexes, int i, int j, int k) {
      if (compare(bucketIndexes[i], bucketIndexes[j]) > 0) {
        int tmp = i;
        i = j;
        j = tmp;
      }
      if (compare(bucketIndexes[j], bucketIndexes[k]) > 0) {
        int tmp = j;
        j = k;
        k = tmp;
      }
      if (compare(bucketIndexes[i], bucketIndexes[j]) > 0) {
        int tmp = i;
        i = j;
        j = tmp;
      }
      return j;
    }

    private static int compare(int a, int b) {
      return Integer.compare(a, b);
    }

    private static void swap(int i, int j, int[] bucketIndexes, long[] counts) {
      if (i == j) {
        return;
      }
      int bucketIndex = bucketIndexes[i];
      bucketIndexes[i] = bucketIndexes[j];
      bucketIndexes[j] = bucketIndex;
      long count = counts[i];
      counts[i] = counts[j];
      counts[j] = count;
    }
  }
}
