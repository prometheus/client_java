package io.prometheus.metrics.model;

public class ExponentialBucket {
    private final long cumulativeCount;
    private final int bucketIndex;

    public ExponentialBucket(long cumulativeCount, int bucketIndex) {
        this.cumulativeCount = cumulativeCount;
        this.bucketIndex = bucketIndex;
    }

    public long getCumulativeCount() {
        return cumulativeCount;
    }

    public int getBucketIndex() {
        return bucketIndex;
    }
}
