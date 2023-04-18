package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class NativeHistogramSnapshot extends MetricSnapshot {

    private final boolean gaugeHistogram;

    /**
     * To create a new {@link NativeHistogramSnapshot}, you can either call the constructor directly or use
     * the builder with {@link NativeHistogramSnapshot#newBuilder()}.
     *
     * @param metadata see {@link MetricMetadata} for more naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public NativeHistogramSnapshot(MetricMetadata metadata, Collection<NativeHistogramData> data) {
        this(false, metadata, data);
    }

    /**
     * Use this with the first parameter {@code true} to create a Gauge Histogram.
     * The data model for Gauge Histograms is the same as for regular histograms, except that bucket values
     * are semantically gauges and not counters. See <a href="https://openmetrics.io">openmetrics.io</a> for more
     * info on Gauge Histograms.
     */
    public NativeHistogramSnapshot(boolean isGaugeHistogram, MetricMetadata metadata, Collection<NativeHistogramData> data) {
        super(metadata, data);
        this.gaugeHistogram = isGaugeHistogram;
    }

    public boolean isGaugeHistogram() {
        return gaugeHistogram;
    }

    @Override
    public List<NativeHistogramData> getData() {
        return (List<NativeHistogramData>) data;
    }

    public static final class NativeHistogramData extends DistributionData {

        private final int schema;
        private final long zeroCount;
        private final double zeroThreshold;
        private final NativeHistogramBuckets bucketsForPositiveValues;
        private final NativeHistogramBuckets bucketsForNegativeValues;

        /**
         * To create a new {@link NativeHistogramData}, you can either call the constructor directly
         * or use the Builder with {@link NativeHistogramData#newBuilder()}.
         *
         * @param schema                   Number between -4 and 8. Required. See Prometheus data model.
         * @param zeroCount                Total number of observed zero values (zero values do not fit into
         *                                 a histogram bucket, that's why there's an extra counter for them).
         * @param zeroThreshold            Observations smaller than this threshold are considered equal to zero.
         *                                 Required. Can be 0.
         * @param bucketsForPositiveValues buckets representing positive values. Can be
         *                                 {@link NativeHistogramBuckets#EMPTY}, but must not be {@code null}.
         * @param bucketsForNegativeValues buckets representing positive values. Can be empty,
         *                                 {@link NativeHistogramBuckets#EMPTY}, but must not be {@code null}.
         * @param sum                      sum of all observed values.
         *                                 Semantically the sum is a counter, so it should only be present if all
         *                                 observed values are positive (example: latencies are always positive).
         *                                 The sum is optional, pass {@link Double#NaN} if no sum is available.
         * @param labels                   must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplars                must not be null. Use {@link Exemplars#EMPTY} if there are no exemplars.
         * @param createdTimestampMillis   timestamp (as in {@link System#currentTimeMillis()}) when this histogram
         *                                 data (this specific set of labels) was created or reset to zero.
         *                                 Note that this refers to the creation of the timeseries,
         *                                 not the creation of the snapshot.
         *                                 It's optional. Use {@code 0L} if there is no created timestamp.
         */
        public NativeHistogramData(int schema, long zeroCount, double zeroThreshold, NativeHistogramBuckets bucketsForPositiveValues, NativeHistogramBuckets bucketsForNegativeValues, double sum, Labels labels, Exemplars exemplars, long createdTimestampMillis) {
            this(schema, zeroCount, zeroThreshold, bucketsForPositiveValues, bucketsForNegativeValues, sum, labels, exemplars, createdTimestampMillis, 0L);
        }

        /**
         * Constructor with an additional metric timestamp parameter. In most cases you should not need this,
         * as the timestamp of a Prometheus metric is set by the Prometheus server during scraping.
         * Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public NativeHistogramData(int schema, long zeroCount, double zeroThreshold, NativeHistogramBuckets bucketsForPositiveValues, NativeHistogramBuckets bucketsForNegativeValues, double sum, Labels labels, Exemplars exemplars, long createdTimestampMillis, long timestampMillis) {
            super(calculateCount(zeroCount, bucketsForPositiveValues, bucketsForNegativeValues), sum, exemplars, labels, createdTimestampMillis, timestampMillis);
            this.schema = schema;
            this.zeroCount = zeroCount;
            this.zeroThreshold = zeroThreshold;
            this.bucketsForPositiveValues = bucketsForPositiveValues;
            this.bucketsForNegativeValues = bucketsForNegativeValues;
            validate();
        }

        private static long calculateCount(long zeroCount, NativeHistogramBuckets bucketsForPositiveValues, NativeHistogramBuckets bucketsForNegativeValues) {
            long count = zeroCount;
            for (int i=0; i<bucketsForNegativeValues.size(); i++) {
                count += bucketsForNegativeValues.getCount(i);
            }
            for (int i=0; i<bucketsForPositiveValues.size(); i++) {
                count += bucketsForPositiveValues.getCount(i);
            }
            return count;
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

        public NativeHistogramBuckets getBucketsForPositiveValues() {
            return bucketsForPositiveValues;
        }

        public NativeHistogramBuckets getBucketsForNegativeValues() {
            return bucketsForNegativeValues;
        }

        @Override
        protected void validate() {
            if (zeroCount < 0) {
                throw new IllegalArgumentException(zeroCount + ": zeroCount cannot be negative");
            }
            if (schema < -4 || schema > 8) {
                throw new IllegalArgumentException(schema + ": illegal schema. Expecting number in [-4, 8].");
            }
            if (Double.isNaN(zeroThreshold) || zeroThreshold < 0) {
                throw new IllegalArgumentException(zeroThreshold + ": illegal zeroThreshold. Must be >= 0.");
            }
            for (Label label : getLabels()) {
                if (label.getName().equals("le")) {
                    throw new IllegalArgumentException("le is a reserved label name for histograms");
                }
            }
        }

        public static class Builder extends DistributionData.Builder<Builder> {
            private Integer schema;
            private long zeroCount = 0;
            private double zeroThreshold = 0;
            private NativeHistogramBuckets bucketsForPositiveValues = NativeHistogramBuckets.EMPTY;
            private NativeHistogramBuckets bucketsForNegativeValues = NativeHistogramBuckets.EMPTY;

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

            public Builder withBucketsForPositiveValues(NativeHistogramBuckets bucketsForPositiveValues) {
                this.bucketsForPositiveValues = bucketsForPositiveValues;
                return this;
            }

            public Builder withBucketsForNegativeValues(NativeHistogramBuckets bucketsForNegativeValues) {
                this.bucketsForNegativeValues = bucketsForNegativeValues;
                return this;
            }

            public NativeHistogramData build() {
                if (schema == null) {
                    throw new IllegalArgumentException("schema is required");
                }
                return new NativeHistogramData(schema, zeroCount, zeroThreshold, bucketsForPositiveValues, bucketsForNegativeValues, sum, labels, exemplars, createdTimestampMillis, scrapeTimestampMillis);
            }
        }

        public static Builder newBuilder() {
            return new Builder();
        }
    }


    public static class Builder extends MetricSnapshot.Builder<Builder> {

        private final List<NativeHistogramData> histogramData = new ArrayList<>();
        private boolean isGaugeHistogram = false;

        private Builder() {
        }

        public Builder addData(NativeHistogramData data) {
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

        public NativeHistogramSnapshot build() {
            return new NativeHistogramSnapshot(isGaugeHistogram, buildMetadata(), histogramData);
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
