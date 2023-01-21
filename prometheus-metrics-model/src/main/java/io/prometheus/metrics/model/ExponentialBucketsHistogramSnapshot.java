package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ExponentialBucketsHistogramSnapshot extends MetricSnapshot {

    private final boolean gaugeHistogram;

    /**
     * To create a new {@link ExponentialBucketsHistogramSnapshot}, you can either call the constructor directly or use
     * the builder with {@link ExponentialBucketsHistogramSnapshot#newBuilder()}.
     *
     * @param metadata see {@link MetricMetadata} for more naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public ExponentialBucketsHistogramSnapshot(MetricMetadata metadata, Collection<ExponentialBucketsHistogramData> data) {
        this(false, metadata, data);
    }

    /**
     * Use this with the first parameter {@code true} to create a Gauge Histogram.
     * The data model for Gauge Histograms is the same as for regular histograms, except that bucket values
     * are semantically gauges and not counters. See <a href="https://openmetrics.io">openmetrics.io</a> for more
     * info on Gauge Histograms.
     */
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
        private final ExponentialBuckets bucketsForPositiveValues;
        private final ExponentialBuckets bucketsForNegativeValues;

        /**
         * To create a new {@link ExponentialBucketsHistogramData}, you can either call the constructor directly
         * or use the Builder with {@link ExponentialBucketsHistogramData#newBuilder()}.
         *
         * @param count                    total number of observations.
         *                                 If present, the count must be the same as the value of the highest bucket.
         *                                 Count is optional, pass -1 if no count is available.
         * @param sum                      sum of all observed values.
         *                                 Semantically the sum is a counter, so it should only be present if all
         *                                 observed values are positive (example: latencies are always positive).
         *                                 The sum is optional, pass {@link Double#NaN} if no sum is available.
         * @param schema                   Number between -4 and 8. Required. See Prometheus data model.
         * @param zeroCount                Total number of observed zero values (zero values do not fit into
         *                                 a histogram bucket, that's why there's an extra counter for them).
         * @param zeroThreshold            Observations smaller than this threshold are considered equal to zero.
         *                                 Required. Can be 0.
         * @param bucketsForPositiveValues buckets representing positive values. Can be
         *                                 {@link ExponentialBuckets#EMPTY}, but must not be {@code null}.
         * @param bucketsForNegativeValues buckets representing positive values. Can be empty,
         *                                 {@link ExponentialBuckets#EMPTY}, but must not be {@code null}.
         * @param labels                   must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplars                must not be null. Use {@link Exemplars#EMPTY} if there are no exemplars.
         * @param createdTimestampMillis   timestamp (as in {@link System#currentTimeMillis()}) when this histogram
         *                                 data (this specific set of labels) was created or reset to zero.
         *                                 Note that this refers to the creation of the timeseries,
         *                                 not the creation of the snapshot.
         *                                 It's optional. Use {@code 0L} if there is no created timestamp.
         */
        public ExponentialBucketsHistogramData(long count, double sum, int schema, long zeroCount, double zeroThreshold, ExponentialBuckets bucketsForPositiveValues, ExponentialBuckets bucketsForNegativeValues, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(count, sum, schema, zeroCount, zeroThreshold, bucketsForPositiveValues, bucketsForNegativeValues, labels, exemplars, createdTimestampMillis, 0L);
        }

        /**
         * Constructor with an additional metric timestamp parameter. In most cases you should not need this,
         * as the timestamp of a Prometheus metric is set by the Prometheus server during scraping.
         * Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public ExponentialBucketsHistogramData(long count, double sum, int schema, long zeroCount, double zeroThreshold, ExponentialBuckets bucketsForPositiveValues, ExponentialBuckets bucketsForNegativeValues, Labels labels, Exemplars exemplars, long createdTimestampMillis, long timestampMillis) {
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

        public ExponentialBuckets getBucketsForPositiveValues() {
            return bucketsForPositiveValues;
        }

        public ExponentialBuckets getBucketsForNegativeValues() {
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

        public static class Builder extends DistributionMetricData.Builder<Builder> {
            private Integer schema;
            private long zeroCount = 0;
            private double zeroThreshold = 0;
            private ExponentialBuckets bucketsForPositiveValues = ExponentialBuckets.EMPTY;
            private ExponentialBuckets bucketsForNegativeValues = ExponentialBuckets.EMPTY;

            private Builder() {}

            @Override
            protected Builder self() {
                return this;
            }

            public Builder withSchema(int schema) {
                this.schema = schema;
                return this;
            }

            public Builder withZeroCount(long zeroCount) {
                this.zeroCount = zeroCount;
                return this;
            }

            public Builder withZeroThreshold(double zeroThreshold) {
                this.zeroThreshold = zeroThreshold;
                return this;
            }

            public Builder withBucketsForPositiveValues(ExponentialBuckets bucketsForPositiveValues) {
                this.bucketsForPositiveValues = bucketsForPositiveValues;
                return this;
            }

            public Builder withBucketsForNegativeValues(ExponentialBuckets bucketsForNegativeValues) {
                this.bucketsForNegativeValues = bucketsForNegativeValues;
                return this;
            }

            public ExponentialBucketsHistogramData build() {
                if (schema == null) {
                    throw new IllegalArgumentException("schema is required");
                }
                return new ExponentialBucketsHistogramData(count, sum, schema, zeroCount, zeroThreshold, bucketsForPositiveValues, bucketsForNegativeValues, labels, exemplars, createdTimestampMillis, timestampMillis);
            }
        }

        public static Builder newBuilder() {
            return new Builder();
        }
    }


    public static class Builder extends MetricSnapshot.Builder<Builder> {

        private final List<ExponentialBucketsHistogramData> histogramData = new ArrayList<>();
        private boolean isGaugeHistogram = false;

        private Builder() {
        }

        public Builder addData(ExponentialBucketsHistogramData data) {
            histogramData.add(data);
            return this;
        }

        /**
         * Create a Gauge Histogram. The data model for Gauge Histograms is the same as for regular histograms,
         * except that bucket values are semantically gauges and not counters.
         * See <a href="https://openmetrics.io">openmetrics.io</a> for more info on Gauge Histograms.
         */
        public Builder asGaugeHistogram() {
            isGaugeHistogram = true;
            return this;
        }

        public ExponentialBucketsHistogramSnapshot build() {
            return new ExponentialBucketsHistogramSnapshot(isGaugeHistogram, buildMetadata(), histogramData);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }
}
