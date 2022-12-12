package io.prometheus.metrics.model;

import java.util.*;

public final class FixedBucketsHistogramSnapshot extends MetricSnapshot {
    private final Collection<FixedBucketsHistogramData> data;

    public FixedBucketsHistogramSnapshot(MetricMetadata metadata, Collection<FixedBucketsHistogramData> data) {
        super(metadata);
        this.data = data;
        for (FixedBucketsHistogramData d : data) {
            for (Label label : d.getLabels()) {
                if (label.getName().equals("le")) {
                    throw new IllegalArgumentException("le is a reserved label name for histograms");
                }
            }
        }
    }

    public Collection<FixedBucketsHistogramData> getData() {
        return data;
    }

    public static final class FixedBucketsHistogramData extends MetricData {
        private final long count;
        private final double sum;
        private final List<FixedBucket> buckets;
        private final long createdTimeMillis;

        public FixedBucketsHistogramData(long count, double sum, FixedBucket[] buckets, Labels labels, long createdTimeMillis) {
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

        public Collection<FixedBucket> getBuckets() {
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
