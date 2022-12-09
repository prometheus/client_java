package io.prometheus.metrics.model;

import java.util.Collection;
import java.util.List;

public final class ExponentialBucketsHistogramSnapshot extends MetricSnapshot {
        private final Collection<ExponentialBucketsHistogramData> data;

        public ExponentialBucketsHistogramSnapshot(MetricMetadata metadata, Collection<ExponentialBucketsHistogramData> data) {
            super(metadata);
            this.data = data;
        }

    public Collection<ExponentialBucketsHistogramData> getData() {
            return data;
    }

    public static final class ExponentialBucketsHistogramData extends MetricData {

        private final long count;
        private final double sum;
            private final int schema;
            private final long zeroCount;
            private final double zeroThreshold;
            private final List<ExponentialBucket> bucketsForPositiveValues;
            private final List<ExponentialBucket> bucketsForNegativeValues;

            public ExponentialBucketsHistogramData(long count, double sum, int schema, long zeroCount, double zeroThreshold, List<ExponentialBucket> bucketsForPositiveValues, List<ExponentialBucket> bucketsForNegativeValues, Labels labels, long createdTimeMillis) {
                super(labels);
                this.count = count;
                this.sum = sum;
                this.schema = schema;
                this.zeroCount = zeroCount;
                this.zeroThreshold = zeroThreshold;
                this.bucketsForPositiveValues = bucketsForPositiveValues;
                this.bucketsForNegativeValues = bucketsForNegativeValues;
                validate();
            }

        public long getCount() {
            return count;
        }

        public double getSum() {
            return sum;
        }

        public int getSchema() {
            return schema;
        }

        public long getZeroCount() {
            return zeroCount;
        }

        public double getZeroThreshold() {
                return zeroThreshold;
            }

        public List<ExponentialBucket> getBucketsForPositiveValues() {
            return bucketsForPositiveValues;
        }

        public List<ExponentialBucket> getBucketsForNegativeValues() {
            return bucketsForNegativeValues;
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
