package io.prometheus.metrics.model;

public class NativeHistogramBucket {

    private final int bucketIndex;
    private final long count;

    public NativeHistogramBucket(int bucketIndex, long count) {
        this.bucketIndex = bucketIndex;
        this.count = count;
    }

    public int getBucketIndex() {
        return bucketIndex;
    }

    public long getCount() {
        return count;
    }
}
