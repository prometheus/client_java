package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class NativeHistogramBuckets implements Iterable<NativeHistogramBucket> {

    public static final NativeHistogramBuckets EMPTY = new NativeHistogramBuckets(new int[]{}, new long[]{});
    private final int[] bucketIndexes;
    private final long[] cumulativeCounts;

    private NativeHistogramBuckets(int[] bucketIndexes, long[] cumulativeCounts) {
        this.bucketIndexes = bucketIndexes;
        this.cumulativeCounts = cumulativeCounts;
    }

    public static NativeHistogramBuckets of(int[] bucketIndexes, long[] cumulativeCounts) {
        int[] bucketIndexesCopy = Arrays.copyOf(bucketIndexes, bucketIndexes.length);
        long[] cumulativeCountsCopy = Arrays.copyOf(cumulativeCounts, cumulativeCounts.length);
        sortAndValidate(bucketIndexesCopy, cumulativeCountsCopy);
        return new NativeHistogramBuckets(bucketIndexesCopy, cumulativeCountsCopy);
    }

    public static NativeHistogramBuckets of(List<Integer> bucketIndexes, List<Long> cumulativeCounts) {
        int[] bucketIndexesCopy = new int[bucketIndexes.size()];
        for (int i=0; i<bucketIndexes.size(); i++) {
            bucketIndexesCopy[i] = bucketIndexes.get(i);
        }
        long[] cumulativeCountsCopy = new long[cumulativeCounts.size()];
        for (int i=0; i<cumulativeCounts.size(); i++) {
            cumulativeCountsCopy[i] = cumulativeCounts.get(i);
        }
        sortAndValidate(bucketIndexesCopy, cumulativeCountsCopy);
        return new NativeHistogramBuckets(bucketIndexesCopy, cumulativeCountsCopy);
    }

    public int size() {
        return bucketIndexes.length;
    }

    private List<NativeHistogramBucket> asList() {
        List<NativeHistogramBucket> result = new ArrayList<>(size());
        for (int i=0; i<bucketIndexes.length; i++) {
            result.add(new NativeHistogramBucket(bucketIndexes[i], cumulativeCounts[i]));
        }
        return result;
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

    public long getCumulativeCount(int i) {
        return cumulativeCounts[i];
    }

    private static void sortAndValidate(int[] bucketIndexes, long[] cumulativeCounts) {
        if (bucketIndexes.length != cumulativeCounts.length) {
            throw new IllegalArgumentException("bucketIndexes.length == " + bucketIndexes.length + " but cumulativeCounts.length == " + cumulativeCounts.length + ". Expected the same length.");
        }
        sort(bucketIndexes, cumulativeCounts);
        validate(bucketIndexes, cumulativeCounts);
    }

    private static void sort(int[] bucketIndexes, long[] cumulativeCounts) {
        // Bubblesort. Should be efficient here as in most cases bucketIndexes is already sorted.
        int n = bucketIndexes.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (bucketIndexes[j] > bucketIndexes[j + 1]) {
                    swap(j, j+1, bucketIndexes, cumulativeCounts);
                }
            }
        }
    }

    private static void swap(int i, int j, int[] bucketIndexes, long[] cumulativeCounts) {
        int tmpInt = bucketIndexes[j];
        bucketIndexes[j] = bucketIndexes[i];
        bucketIndexes[i] = tmpInt;
        long tmpLong = cumulativeCounts[j];
        cumulativeCounts[j] = cumulativeCounts[i];
        cumulativeCounts[i] = tmpLong;
    }

    private static void validate(int[] bucketIndexes, long[] cumulativeCounts) {
        // Preconditions:
        // * bucketIndexes sorted
        // * bucketIndexes and cumulativeCounts have the same length
        for (int i=0; i<bucketIndexes.length; i++) {
            if (cumulativeCounts[i] < 0) {
                throw new IllegalArgumentException("Bucket counts cannot be negative.");
            }
            if (i > 0) {
                if (bucketIndexes[i-1] == bucketIndexes[i]) {
                    throw new IllegalArgumentException("Duplicate bucket index " + bucketIndexes[i]);
                }
                if (cumulativeCounts[i-1] > cumulativeCounts[i]) {
                    throw new IllegalArgumentException("Bucket counts must be cumulative.");
                }
            }
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private final List<Integer> bucketIndexes = new ArrayList<>();
        private final List<Long> cumulativeCounts = new ArrayList<>();

        private Builder() {}

        public Builder addBucket(int bucketIndex, long cumulativeCount) {
            bucketIndexes.add(bucketIndex);
            cumulativeCounts.add(cumulativeCount);
            return this;
        }

        public NativeHistogramBuckets build() {
            return NativeHistogramBuckets.of(bucketIndexes, cumulativeCounts);
        }
    }
}
