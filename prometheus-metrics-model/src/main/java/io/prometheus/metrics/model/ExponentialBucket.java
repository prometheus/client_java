package io.prometheus.metrics.model;

public class ExponentialBucket {
    private final long cumulativeCount;
    private final int bucketIndex;
    private final Exemplar exemplar;

    public ExponentialBucket(long cumulativeCount, int bucketIndex, Exemplar exemplar) {
        this.cumulativeCount = cumulativeCount;
        this.bucketIndex = bucketIndex;
        this.exemplar = exemplar;
    }

    public long getCumulativeCount() {
        return cumulativeCount;
    }

    public int getBucketIndex() {
        return bucketIndex;
    }

    public Exemplar getExemplar() {
        return exemplar;
    }
}
