package io.prometheus.metrics.model;

public class NativeHistogramBucket {

    private final int bucketIndex;
    private final long cumulativeCount;

    public NativeHistogramBucket(int bucketIndex, long cumulativeCount) {
        this.bucketIndex = bucketIndex;
        this.cumulativeCount = cumulativeCount;
    }

    public int getBucketIndex() {
        return bucketIndex;
    }

    public long getCumulativeCount() {
        return cumulativeCount;
    }
}
