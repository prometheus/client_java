package io.prometheus.metrics.model;

import java.util.*;

public final class ExplicitBucketsHistogramSnapshot extends MetricSnapshot {
    private final Collection<ExplicitBucketsHistogramData> data;

    public ExplicitBucketsHistogramSnapshot(MetricMetadata metadata, Collection<ExplicitBucketsHistogramData> data) {
        super(metadata);
        this.data = data;
        for (ExplicitBucketsHistogramData d : data) {
            for (Label label : d.getLabels()) {
                if (label.getName().equals("le")) {
                    throw new IllegalArgumentException("le is a reserved label name for histograms");
                }
            }
        }
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
            // TODO: validation
            // TODO: buckets must not have duplicates, must be sorted, counts must be cumulative, buckets must include a +Inf bucket.
            // TODO: maybe implement a dedicated Buckets class similar to Labels?
            validate();
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
        protected void validate() {
            for (Label label : getLabels()) {
                if (label.getName().equals("le")) {
                    throw new IllegalArgumentException("le is a reserved label name for histograms");
                }
            }
        }
    }
}
