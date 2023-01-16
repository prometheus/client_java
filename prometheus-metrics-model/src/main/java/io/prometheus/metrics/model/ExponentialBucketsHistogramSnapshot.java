package io.prometheus.metrics.model;

import java.util.Collection;
import java.util.List;

public final class ExponentialBucketsHistogramSnapshot extends MetricSnapshot {

    private final boolean gaugeHistogram;

    public ExponentialBucketsHistogramSnapshot(MetricMetadata metadata, Collection<ExponentialBucketsHistogramData> data) {
        this(false, metadata, data);
    }

    public ExponentialBucketsHistogramSnapshot(boolean isGaugeHistogram, MetricMetadata metadata, Collection<ExponentialBucketsHistogramData> data) {
        super(metadata, data);
        this.gaugeHistogram = isGaugeHistogram;
    }

    @Override
    public List<ExponentialBucketsHistogramData> getData() {
        return (List<ExponentialBucketsHistogramData>) data;
    }

    public static final class ExponentialBucketsHistogramData extends DistributionMetricData {

        private final int schema;
        private final long zeroCount;
        private final double zeroThreshold;
        private final List<ExponentialBucket> bucketsForPositiveValues;
        private final List<ExponentialBucket> bucketsForNegativeValues;

        public ExponentialBucketsHistogramData(long count, double sum, int schema, long zeroCount, double zeroThreshold, List<ExponentialBucket> bucketsForPositiveValues, List<ExponentialBucket> bucketsForNegativeValues, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, schema, zeroCount, zeroThreshold, bucketsForPositiveValues, bucketsForNegativeValues, labels, exemplars, createdTimestampMillis, 0L);
        }

        public ExponentialBucketsHistogramData(long count, double sum, int schema, long zeroCount, double zeroThreshold, List<ExponentialBucket> bucketsForPositiveValues, List<ExponentialBucket> bucketsForNegativeValues, Labels labels, Exemplars exemplars, long createdTimestampMillis, long timestampMillis) {
            super(count, sum, exemplars, labels, createdTimestampMillis, timestampMillis);
            this.schema = schema;
            this.zeroCount = zeroCount;
            this.zeroThreshold = zeroThreshold;
            this.bucketsForPositiveValues = bucketsForPositiveValues;
            this.bucketsForNegativeValues = bucketsForNegativeValues;
            validate();
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
