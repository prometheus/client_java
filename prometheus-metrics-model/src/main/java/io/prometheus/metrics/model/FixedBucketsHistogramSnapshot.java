package io.prometheus.metrics.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class FixedBucketsHistogramSnapshot extends MetricSnapshot {

    private final boolean gaugeHistogram;

    public FixedBucketsHistogramSnapshot(String name, FixedBucketsHistogramData... data) {
        this(new MetricMetadata(name), data);
    }

    public FixedBucketsHistogramSnapshot(String name, String help, FixedBucketsHistogramData... data) {
        this(new MetricMetadata(name, help), data);
    }

    public FixedBucketsHistogramSnapshot(String name, String help, Unit unit, FixedBucketsHistogramData... data) {
        this(new MetricMetadata(name, help, unit), data);
    }

    public FixedBucketsHistogramSnapshot(MetricMetadata metadata, FixedBucketsHistogramData... data) {
        this(metadata, Arrays.asList(data));
    }

    public FixedBucketsHistogramSnapshot(MetricMetadata metadata, Collection<FixedBucketsHistogramData> data) {
        this(false, metadata, data);
    }

    public FixedBucketsHistogramSnapshot(boolean isGaugeHistogram, MetricMetadata metadata, Collection<FixedBucketsHistogramData> data) {
        super(metadata, data);
        this.gaugeHistogram = isGaugeHistogram;
    }

    public boolean isGaugeHistogram() {
        return gaugeHistogram;
    }

    @Override
    public List<FixedBucketsHistogramData> getData() {
        return (List<FixedBucketsHistogramData>) data;
    }

    public static final class FixedBucketsHistogramData extends DistributionMetricData {

        private final FixedBuckets buckets;

        /**
         * @param count total number of observations. Optional, pass {@code -1} if not available.
         *              If the count is present, it must have the same value as the +Inf bucket.
         * @param sum sum of all observed values. Optional, {@code pass Double.NaN} if not present.
         * @param buckets required, there must be at least the +Inf bucket.
         * @param labels optional, pass Labels.EMPTY if not present.
         * @param exemplars optional, pass Exemplars.EMPTY if not present.
         * @param createdTimestampMillis optional, pass 0 if not present.
         */
        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, buckets, labels, exemplars, createdTimestampMillis, 0);
        }

        public FixedBucketsHistogramData(long count, double sum, FixedBuckets buckets, Labels labels, Exemplars exemplars, long createdTimestampMillis, long timestampMillis) {
            super(count, sum, exemplars, labels, createdTimestampMillis, timestampMillis);
            this.buckets = buckets;
            validate();
        }

        public FixedBuckets getBuckets() {
            return buckets;
        }

        @Override
        protected void validate() {
            for (Label label : getLabels()) {
                if (label.getName().equals("le")) {
                    throw new IllegalArgumentException("le is a reserved label name for histograms");
                }
            }
            if (buckets.getCumulativeCount(buckets.size()-1) != getCount()) {
                throw new IllegalArgumentException("The +Inf bucket must have the same value as the count.");
            }
        }
    }
}
