package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Immutable representation of native histogram buckets.
 * <p>
 * The bucket index defines the boundaries of the bucket,
 * depending on the histogram's {@link HistogramSnapshot.HistogramDataPointSnapshot#getNativeSchema() schema}.
 * <pre>
 *     base = 2^(2^-schema)
 *     lower bound = base^(index - 1)
 *     upper bound = base^index
 * </pre>
 */
public class NativeHistogramBuckets implements Iterable<NativeHistogramBucket> {

    public static final NativeHistogramBuckets EMPTY = new NativeHistogramBuckets(new int[]{}, new long[]{});
    private final int[] bucketIndexes; // sorted
    private final long[] counts;

    private NativeHistogramBuckets(int[] bucketIndexes, long[] counts) {
        this.bucketIndexes = bucketIndexes;
        this.counts = counts;
    }

    /**
     * To create a new {@link NativeHistogramBuckets} instance, you can either use one of the static {@code of(...)}
     * methods, or use {@link NativeHistogramBuckets#builder()}.
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
     * To create a new {@link NativeHistogramBuckets} instance, you can either use one of the static {@code of(...)}
     * methods, or use {@link NativeHistogramBuckets#builder()}.
     * @param bucketIndexes see class javadoc of {@link NativeHistogramBuckets}. May be empty.
     * @param counts must have the same size as bucketIndexes
     */
    public static NativeHistogramBuckets of(List<Integer> bucketIndexes, List<Long> counts) {
        int[] bucketIndexesCopy = new int[bucketIndexes.size()];
        for (int i=0; i<bucketIndexes.size(); i++) {
            bucketIndexesCopy[i] = bucketIndexes.get(i);
        }
        long[] countsCopy = new long[counts.size()];
        for (int i=0; i<counts.size(); i++) {
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
        for (int i=0; i<bucketIndexes.length; i++) {
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
            throw new IllegalArgumentException("bucketIndexes.length == " + bucketIndexes.length + " but counts.length == " + counts.length + ". Expected the same length.");
        }
        sort(bucketIndexes, counts);
        validate(bucketIndexes, counts);
    }

    private static void sort(int[] bucketIndexes, long[] counts) {
        // Bubblesort. Should be efficient here as in most cases bucketIndexes is already sorted.
        int n = bucketIndexes.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (bucketIndexes[j] > bucketIndexes[j + 1]) {
                    swap(j, j+1, bucketIndexes, counts);
                }
            }
        }
    }

    private static void swap(int i, int j, int[] bucketIndexes, long[] counts) {
        int tmpInt = bucketIndexes[j];
        bucketIndexes[j] = bucketIndexes[i];
        bucketIndexes[i] = tmpInt;
        long tmpLong = counts[j];
        counts[j] = counts[i];
        counts[i] = tmpLong;
    }

    private static void validate(int[] bucketIndexes, long[] counts) {
        // Preconditions:
        // * bucketIndexes sorted
        // * bucketIndexes and counts have the same length
        for (int i=0; i<bucketIndexes.length; i++) {
            if (counts[i] < 0) {
                throw new IllegalArgumentException("Bucket counts cannot be negative.");
            }
            if (i > 0) {
                if (bucketIndexes[i-1] == bucketIndexes[i]) {
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

        /**
         * Add a native histogram bucket. Call multiple times to add multiple buckets.
         */
        public Builder bucket(int bucketIndex, long count) {
            bucketIndexes.add(bucketIndex);
            counts.add(count);
            return this;
        }

        public NativeHistogramBuckets build() {
            return NativeHistogramBuckets.of(bucketIndexes, counts);
        }
    }
}
