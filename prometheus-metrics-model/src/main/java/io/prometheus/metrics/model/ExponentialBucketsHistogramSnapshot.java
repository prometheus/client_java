package io.prometheus.metrics.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ExponentialBucketsHistogramSnapshot extends MetricSnapshot {

    public ExponentialBucketsHistogramSnapshot(MetricMetadata metadata, Collection<ExponentialBucketsHistogramData> data) {
        super(metadata, data);
    }

    @Override
    public List<ExponentialBucketsHistogramData> getData() {
        return (List<ExponentialBucketsHistogramData>) data;
    }

    public static final class ExponentialBucketsHistogramData extends MetricData {

        private final long count;
        private final double sum;
        private final int schema;
        private final long zeroCount;
        private final double zeroThreshold;
        private final List<ExponentialBucket> bucketsForPositiveValues;
        private final List<ExponentialBucket> bucketsForNegativeValues;
        private final Collection<Exemplar> exemplars;

        public ExponentialBucketsHistogramData(long count, double sum, int schema, long zeroCount, double zeroThreshold, List<ExponentialBucket> bucketsForPositiveValues, List<ExponentialBucket> bucketsForNegativeValues, Labels labels, Collection<Exemplar> exemplars, long createdTimestampMillis, long timestampMillis) {
            super(labels, createdTimestampMillis, timestampMillis);
            this.count = count;
            this.sum = sum;
            this.schema = schema;
            this.zeroCount = zeroCount;
            this.zeroThreshold = zeroThreshold;
            this.bucketsForPositiveValues = bucketsForPositiveValues;
            this.bucketsForNegativeValues = bucketsForNegativeValues;
            this.exemplars = exemplars == null ? Collections.emptyList() : exemplars;
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

        public Collection<Exemplar> getExemplars() {
            return exemplars;
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
