package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.annotations.StableApi;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Immutable container for histogram buckets with fixed bucket boundaries. Note that the counts are
 * <i>not</i> cumulative.
 */
@StableApi
public class ClassicHistogramBuckets implements Iterable<ClassicHistogramBucket> {

  /** Used in native histograms to indicate that no classic histogram buckets are present. */
  public static final ClassicHistogramBuckets EMPTY =
      new ClassicHistogramBuckets(new double[] {}, new long[] {});

  private final double[] upperBounds;
  private final long[] counts; // not cumulative

  private ClassicHistogramBuckets(double[] upperBounds, long[] counts) {
    this.upperBounds = upperBounds;
    this.counts = counts;
  }

  /**
   * To create new {@link ClassicHistogramBuckets}, you can either use one of the static {@code
   * of(...)} methods, or use {@link ClassicHistogramBuckets#builder()}.
   *
   * <p>This method will create a copy of upperBounds and counts.
   *
   * @param upperBounds must have the same length as counts. Must not contain duplicates. Must
   *     contain at least {@link Double#POSITIVE_INFINITY} for the {@code +Inf} bucket. An upper
   *     bound must not be {@link Double#NaN}. The upperBounds array does not need to be sorted.
   * @param counts must have the same length as {@code upperBounds}. The entry at index {@code i} is
   *     the count for the {@code upperBounds} at index {@code i}. For each count, {@link
   *     Number#longValue()} is called to get the value. Counts are <i>not</i> cumulative. Counts
   *     must not be negative.
   */
  public static ClassicHistogramBuckets of(
      List<Double> upperBounds, List<? extends Number> counts) {
    double[] upperBoundsCopy = new double[upperBounds.size()];
    for (int i = 0; i < upperBounds.size(); i++) {
      upperBoundsCopy[i] = upperBounds.get(i);
    }
    long[] countsCopy = new long[counts.size()];
    for (int i = 0; i < counts.size(); i++) {
      countsCopy[i] = counts.get(i).longValue();
    }
    sortAndValidate(upperBoundsCopy, countsCopy);
    return new ClassicHistogramBuckets(upperBoundsCopy, countsCopy);
  }

  /**
   * To create new {@link ClassicHistogramBuckets}, you can either use one of the static {@code
   * of(...)} methods, or use {@link ClassicHistogramBuckets#builder()}.
   *
   * <p>This method will create a copy of upperBounds and counts.
   *
   * @param upperBounds must have the same length as counts. Must not contain duplicates. Must
   *     contain at least {@link Double#POSITIVE_INFINITY} for the {@code +Inf} bucket. An upper
   *     bound must not be {@link Double#NaN}. The upperBounds array does not need to be sorted.
   * @param counts must have the same length as {@code upperBounds}. The entry at index {@code i} is
   *     the count for the {@code upperBounds} at index {@code i}. For each count, {@link
   *     Number#longValue()} is called to get the value. Counts are <i>not</i> cumulative. Counts
   *     must not be negative.
   */
  public static ClassicHistogramBuckets of(double[] upperBounds, Number[] counts) {
    double[] upperBoundsCopy = Arrays.copyOf(upperBounds, upperBounds.length);
    long[] countsCopy = new long[counts.length];
    for (int i = 0; i < counts.length; i++) {
      countsCopy[i] = counts[i].longValue();
    }
    sortAndValidate(upperBoundsCopy, countsCopy);
    return new ClassicHistogramBuckets(upperBoundsCopy, countsCopy);
  }

  /**
   * To create new {@link ClassicHistogramBuckets}, you can either use one of the static {@code
   * of(...)} methods, or use {@link ClassicHistogramBuckets#builder()}.
   *
   * <p>This method will create a copy of upperBounds and counts.
   *
   * @param upperBounds must have the same length as counts. Must not contain duplicates. Must
   *     contain at least {@link Double#POSITIVE_INFINITY} for the {@code +Inf} bucket. An upper
   *     bound must not be {@link Double#NaN}. The upperBounds array does not need to be sorted.
   * @param counts must have the same length as {@code upperBounds}. The entry at index {@code i} is
   *     the count for the {@code upperBounds} at index {@code i}. Counts are <i>not</i> cumulative.
   *     Counts must not be negative.
   */
  public static ClassicHistogramBuckets of(double[] upperBounds, long[] counts) {
    double[] upperBoundsCopy = Arrays.copyOf(upperBounds, upperBounds.length);
    long[] countsCopy = Arrays.copyOf(counts, counts.length);
    sortAndValidate(upperBoundsCopy, countsCopy);
    return new ClassicHistogramBuckets(upperBoundsCopy, countsCopy);
  }

  private static void sortAndValidate(double[] upperBounds, long[] counts) {
    if (upperBounds.length != counts.length) {
      throw new IllegalArgumentException(
          "upperBounds.length == "
              + upperBounds.length
              + " but counts.length == "
              + counts.length
              + ". Expected the same length.");
    }
    sort(upperBounds, counts);
    validate(upperBounds, counts);
  }

  /**
   * Sorts upperBounds and counts in place using introspective quicksort.
   *
   * <p>Algorithm: 3-way quicksort with insertion sort for tiny partitions and heapsort fallback at
   * the recursion depth limit. Parallel arrays are swapped in lockstep.
   *
   * <p>Complexity: O(n log n) average and worst case.
   */
  private static void sort(double[] upperBounds, long[] counts) {
    DoubleArraySorter.sort(upperBounds, counts);
  }

  private static void validate(double[] upperBounds, long[] counts) {
    // Preconditions:
    // * upperBounds sorted
    // * upperBounds and counts have the same length
    if (upperBounds.length == 0) {
      throw new IllegalArgumentException(
          ClassicHistogramBuckets.class.getSimpleName()
              + " cannot be empty. They must contain at least the +Inf bucket.");
    }
    if (upperBounds[upperBounds.length - 1] != Double.POSITIVE_INFINITY) {
      throw new IllegalArgumentException(
          ClassicHistogramBuckets.class.getSimpleName() + " must contain the +Inf bucket.");
    }
    for (int i = 0; i < upperBounds.length; i++) {
      if (Double.isNaN(upperBounds[i])) {
        throw new IllegalArgumentException(
            "Cannot use NaN as an upper bound in " + ClassicHistogramBuckets.class.getSimpleName());
      }
      if (counts[i] < 0) {
        throw new IllegalArgumentException(
            "Counts in " + ClassicHistogramBuckets.class.getSimpleName() + " cannot be negative.");
      }
      if (i > 0) {
        if (upperBounds[i - 1] == upperBounds[i]) {
          throw new IllegalArgumentException("Duplicate upper bound " + upperBounds[i]);
        }
      }
    }
  }

  public int size() {
    return upperBounds.length;
  }

  public double getUpperBound(int i) {
    return upperBounds[i];
  }

  /** The count is <i>not</i> cumulative. */
  public long getCount(int i) {
    return counts[i];
  }

  public boolean isEmpty() {
    return this.upperBounds.length == 0;
  }

  private List<ClassicHistogramBucket> asList() {
    List<ClassicHistogramBucket> result = new ArrayList<>(size());
    for (int i = 0; i < upperBounds.length; i++) {
      result.add(new ClassicHistogramBucket(upperBounds[i], counts[i]));
    }
    return Collections.unmodifiableList(result);
  }

  @Override
  public Iterator<ClassicHistogramBucket> iterator() {
    return asList().iterator();
  }

  public Stream<ClassicHistogramBucket> stream() {
    return asList().stream();
  }

  /**
   * To create new {@link ClassicHistogramBuckets}, you can either use one of the static {@code
   * of(...)} methods, or use {@code builder()}.
   */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final List<Double> upperBounds = new ArrayList<>();
    private final List<Long> counts = new ArrayList<>();

    private Builder() {}

    /** Must be called at least once for the {@link Double#POSITIVE_INFINITY} bucket. */
    public Builder bucket(double upperBound, long count) {
      upperBounds.add(upperBound);
      counts.add(count);
      return this;
    }

    /**
     * Will throw an {@link IllegalArgumentException} if the {@link Double#POSITIVE_INFINITY} bucket
     * is missing.
     */
    public ClassicHistogramBuckets build() {
      return ClassicHistogramBuckets.of(upperBounds, counts);
    }
  }

  /**
   * In-place introsort for {@code upperBounds} and parallel {@code counts}.
   *
   * <p>Uses 3-way quicksort partitioning for large ranges, insertion sort for tiny ranges, and a
   * heapsort fallback at the recursion-depth limit to guarantee O(n log n) worst-case complexity.
   */
  private static final class DoubleArraySorter {

    private static final int INSERTION_SORT_THRESHOLD = 24;

    private static void sort(double[] upperBounds, long[] counts) {
      int right = upperBounds.length - 1;
      if (right <= 0) {
        return;
      }
      introSort(upperBounds, counts, 0, right, depthLimit(upperBounds.length));
    }

    private static void introSort(
        double[] upperBounds, long[] counts, int left, int right, int depthLimit) {
      while (left < right) {
        if (right - left + 1 <= INSERTION_SORT_THRESHOLD) {
          insertionSort(upperBounds, counts, left, right);
          return;
        }
        if (depthLimit == 0) {
          heapSort(upperBounds, counts, left, right);
          return;
        }
        depthLimit--;

        int mid = left + ((right - left) >>> 1);
        int pivotIndex = medianOf3(upperBounds, left, mid, right);
        double pivot = upperBounds[pivotIndex];

        int lt = left;
        int i = left;
        int gt = right;
        while (i <= gt) {
          int cmp = compare(upperBounds[i], pivot);
          if (cmp < 0) {
            swap(i, lt, upperBounds, counts);
            i++;
            lt++;
          } else if (cmp > 0) {
            swap(i, gt, upperBounds, counts);
            gt--;
          } else {
            i++;
          }
        }

        if (lt - left < right - gt) {
          introSort(upperBounds, counts, left, lt - 1, depthLimit);
          left = gt + 1;
        } else {
          introSort(upperBounds, counts, gt + 1, right, depthLimit);
          right = lt - 1;
        }
      }
    }

    private static void insertionSort(double[] upperBounds, long[] counts, int left, int right) {
      for (int i = left + 1; i <= right; i++) {
        double upperBound = upperBounds[i];
        long count = counts[i];
        int j = i - 1;
        while (j >= left && compare(upperBounds[j], upperBound) > 0) {
          upperBounds[j + 1] = upperBounds[j];
          counts[j + 1] = counts[j];
          j--;
        }
        upperBounds[j + 1] = upperBound;
        counts[j + 1] = count;
      }
    }

    private static void heapSort(double[] upperBounds, long[] counts, int left, int right) {
      int size = right - left + 1;
      for (int i = (size >>> 1) - 1; i >= 0; i--) {
        siftDown(upperBounds, counts, left, i, size);
      }
      for (int end = size - 1; end > 0; end--) {
        swap(left, left + end, upperBounds, counts);
        siftDown(upperBounds, counts, left, 0, end);
      }
    }

    private static void siftDown(
        double[] upperBounds, long[] counts, int base, int root, int size) {
      while (true) {
        int child = (root << 1) + 1;
        if (child >= size) {
          return;
        }
        int rightChild = child + 1;
        if (rightChild < size
            && compare(upperBounds[base + child], upperBounds[base + rightChild]) < 0) {
          child = rightChild;
        }
        if (compare(upperBounds[base + root], upperBounds[base + child]) >= 0) {
          return;
        }
        swap(base + root, base + child, upperBounds, counts);
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

    private static int medianOf3(double[] upperBounds, int i, int j, int k) {
      if (compare(upperBounds[i], upperBounds[j]) > 0) {
        int tmp = i;
        i = j;
        j = tmp;
      }
      if (compare(upperBounds[j], upperBounds[k]) > 0) {
        int tmp = j;
        j = k;
        k = tmp;
      }
      if (compare(upperBounds[i], upperBounds[j]) > 0) {
        int tmp = i;
        i = j;
        j = tmp;
      }
      return j;
    }

    private static int compare(double a, double b) {
      return Double.compare(a, b);
    }

    private static void swap(int i, int j, double[] upperBounds, long[] counts) {
      if (i == j) {
        return;
      }
      double upperBound = upperBounds[i];
      upperBounds[i] = upperBounds[j];
      upperBounds[j] = upperBound;
      long count = counts[i];
      counts[i] = counts[j];
      counts[j] = count;
    }
  }
}
