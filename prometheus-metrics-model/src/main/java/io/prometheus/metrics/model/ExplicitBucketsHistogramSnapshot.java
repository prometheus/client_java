package io.prometheus.metrics.model;

import java.util.*;

public final class ExplicitBucketsHistogramSnapshot extends Snapshot {

    private final long count;
    private final double sum;
    private final List<ExplicitBucket> buckets;
    private final long createdTimeMillis;
    private final Labels labels;

    public ExplicitBucketsHistogramSnapshot(long count, double sum, ExplicitBucket[] buckets, long createdTimeMillis, Labels labels) {
        this.count = count;
        this.sum = sum;
        this.buckets = Collections.unmodifiableList(Arrays.asList(Arrays.copyOf(buckets, buckets.length)));
        this.createdTimeMillis = createdTimeMillis;
        this.labels = labels;
    }

    public long getCount() {
        return count;
    }
    public double getSum() {
        return sum;
    }
    public Collection<ExplicitBucket> getBuckets() {
        return buckets;
    }
    public long getCreatedTimeMillis() {
        return createdTimeMillis;
    }
    @Override
    public Labels getLabels() {
        return labels;
    }
}
