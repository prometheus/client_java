package io.prometheus.metrics.model;

import java.util.*;

public final class ExplicitBucketsHistogramSnapshot extends MetricSnapshot {
    private final Collection<ExplicitBucketsHistogramData> data;

    public ExplicitBucketsHistogramSnapshot(MetricMetadata metadata, Collection<ExplicitBucketsHistogramData> data) {
        super(metadata);
        this.data = data;
    }

    public Collection<ExplicitBucketsHistogramData> getData() {
        return data;
    }

    public static final class ExplicitBucketsHistogramData extends MetricData {
        private final long count;
        private final double sum;
        private final List<ExplicitBucket> buckets;
        private final long createdTimeMillis;

        public ExplicitBucketsHistogramData(long count, double sum, ExplicitBucket[] buckets, Labels labels, long createdTimeMillis) {
            super(labels);
            this.count = count;
            this.sum = sum;
            this.buckets = Collections.unmodifiableList(Arrays.asList(Arrays.copyOf(buckets, buckets.length)));
            this.createdTimeMillis = createdTimeMillis;
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
    }
}
