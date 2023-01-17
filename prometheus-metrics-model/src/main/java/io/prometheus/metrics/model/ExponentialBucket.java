package io.prometheus.metrics.model;

public class ExponentialBucket {

    private final int bucketIndex;
    private final long cumulativeCount;

    public ExponentialBucket(int bucketIndex, long cumulativeCount) {
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
