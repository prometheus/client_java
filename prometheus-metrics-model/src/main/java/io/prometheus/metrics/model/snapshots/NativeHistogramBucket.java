package io.prometheus.metrics.model.snapshots;

/**
 * For iterating over {@link NativeHistogramBuckets}.
 */
public class NativeHistogramBucket {

    private final int bucketIndex;
    private final long count;

    public NativeHistogramBucket(int bucketIndex, long count) {
        this.bucketIndex = bucketIndex;
        this.count = count;
    }

    /**
     * See {@link NativeHistogramBuckets} for info on native bucket indexes.
     */
    public int getBucketIndex() {
        return bucketIndex;
    }

    public long getCount() {
        return count;
    }
}
