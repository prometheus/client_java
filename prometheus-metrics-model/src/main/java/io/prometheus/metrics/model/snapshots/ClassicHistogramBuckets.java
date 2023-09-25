package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Immutable container for histogram buckets with fixed bucket boundaries.
 * Note that the counts are <i>not</i> cumulative.
 */
public class ClassicHistogramBuckets implements Iterable<ClassicHistogramBucket> {

    /**
     * Used in native histograms to indicate that no classic histogram buckets are present.
     */
    public static final ClassicHistogramBuckets EMPTY = new ClassicHistogramBuckets(new double[]{}, new long[]{});

    private final double[] upperBounds;
    private final long[] counts; // not cumulative

    private ClassicHistogramBuckets(double[] upperBounds, long[] counts) {
        this.upperBounds = upperBounds;
        this.counts = counts;
    }

    /**
     * To create new {@link ClassicHistogramBuckets}, you can either use one of the static {@code of(...)} methods,
     * or use {@link ClassicHistogramBuckets#builder()}.
     * <p>
     * This method will create a copy of upperBounds and counts.
     *
     * @param upperBounds must have the same length as counts. Must not contain duplicates.
     *                    Must contain at least {@link Double#POSITIVE_INFINITY} for the {@code +Inf} bucket.
     *                    An upper bound must not be {@link Double#NaN}.
     *                    The upperBounds array does not need to be sorted.
     * @param counts      must have the same length as {@code upperBounds}.
     *                    The entry at index {@code i} is the count for the {@code upperBound} at index {@code i}.
     *                    For each count, {@link Number#longValue()} is called to get the value.
     *                    Counts are <i>not</i> cumulative. Counts must not be negative.
     */
    public static ClassicHistogramBuckets of(List<Double> upperBounds, List<? extends Number> counts) {
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
     * To create new {@link ClassicHistogramBuckets}, you can either use one of the static {@code of(...)} methods,
     * or use {@link ClassicHistogramBuckets#builder()}.
     * <p>
     * This method will create a copy of upperBounds and counts.
     *
     * @param upperBounds must have the same length as counts. Must not contain duplicates.
     *                    Must contain at least {@link Double#POSITIVE_INFINITY} for the {@code +Inf} bucket.
     *                    An upper bound must not be {@link Double#NaN}.
     *                    The upperBounds array does not need to be sorted.
     * @param counts      must have the same length as {@code upperBounds}.
     *                    The entry at index {@code i} is the count for the {@code upperBound} at index {@code i}.
     *                    For each count, {@link Number#longValue()} is called to get the value.
     *                    Counts are <i>not</i> cumulative. Counts must not be negative.
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
     * To create new {@link ClassicHistogramBuckets}, you can either use one of the static {@code of(...)} methods,
     * or use {@link ClassicHistogramBuckets#builder()}.
     * <p>
     * This method will create a copy of upperBounds and counts.
     *
     * @param upperBounds must have the same length as counts. Must not contain duplicates.
     *                    Must contain at least {@link Double#POSITIVE_INFINITY} for the {@code +Inf} bucket.
     *                    An upper bound must not be {@link Double#NaN}.
     *                    The upperBounds array does not need to be sorted.
     * @param counts      must have the same length as {@code upperBounds}.
     *                    The entry at index {@code i} is the count for the {@code upperBound} at index {@code i}.
     *                    Counts are <i>not</i> cumulative. Counts must not be negative.
     */
    public static ClassicHistogramBuckets of(double[] upperBounds, long[] counts) {
        double[] upperBoundsCopy = Arrays.copyOf(upperBounds, upperBounds.length);
        long[] countsCopy = Arrays.copyOf(counts, counts.length);
        sortAndValidate(upperBoundsCopy, countsCopy);
        return new ClassicHistogramBuckets(upperBoundsCopy, countsCopy);
    }

    private static void sortAndValidate(double[] upperBounds, long[] counts) {
        if (upperBounds.length != counts.length) {
            throw new IllegalArgumentException("upperBounds.length == " + upperBounds.length + " but counts.length == " + counts.length + ". Expected the same length.");
        }
        sort(upperBounds, counts);
        validate(upperBounds, counts);
    }

    private static void sort(double[] upperBounds, long[] counts) {
        // Bubblesort. Should be efficient here as in most cases upperBounds is already sorted.
        int n = upperBounds.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (upperBounds[j] > upperBounds[j + 1]) {
                    swap(j, j + 1, upperBounds, counts);
                }
            }
        }
    }

    private static void swap(int i, int j, double[] upperBounds, long[] counts) {
        double tmpDouble = upperBounds[j];
        upperBounds[j] = upperBounds[i];
        upperBounds[i] = tmpDouble;
        long tmpLong = counts[j];
        counts[j] = counts[i];
        counts[i] = tmpLong;
    }

    private static void validate(double[] upperBounds, long[] counts) {
        // Preconditions:
        // * upperBounds sorted
        // * upperBounds and counts have the same length
        if (upperBounds.length == 0) {
            throw new IllegalArgumentException(ClassicHistogramBuckets.class.getSimpleName() + " cannot be empty. They must contain at least the +Inf bucket.");
        }
        if (upperBounds[upperBounds.length - 1] != Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException(ClassicHistogramBuckets.class.getSimpleName() + " must contain the +Inf bucket.");
        }
        for (int i = 0; i < upperBounds.length; i++) {
            if (Double.isNaN(upperBounds[i])) {
                throw new IllegalArgumentException("Cannot use NaN as an upper bound in " + ClassicHistogramBuckets.class.getSimpleName());
            }
            if (counts[i] < 0) {
                throw new IllegalArgumentException("Counts in " + ClassicHistogramBuckets.class.getSimpleName() + " cannot be negative.");
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

    /**
     * The count is <i>not</i> cumulative.
     */
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
     * To create new {@link ClassicHistogramBuckets}, you can either use one of the static {@code of(...)} methods,
     * or use {@code builder()}.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final List<Double> upperBounds = new ArrayList<>();
        private final List<Long> counts = new ArrayList<>();

        private Builder() {}

        /**
         * Must be called at least once for the {@link Double#POSITIVE_INFINITY} bucket.
         */
        public Builder bucket(double upperBound, long count) {
            upperBounds.add(upperBound);
            counts.add(count);
            return this;
        }

        /**
         * Will throw an {@link IllegalArgumentException} if the {@link Double#POSITIVE_INFINITY} bucket is missing.
         */
        public ClassicHistogramBuckets build() {
            return ClassicHistogramBuckets.of(upperBounds, counts);
        }
    }
}
