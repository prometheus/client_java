package io.prometheus.metrics.model.snapshots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Immutable snapshot of a Histogram.
 */
public final class HistogramSnapshot extends MetricSnapshot {

    private final boolean gaugeHistogram;
    public static final int CLASSIC_HISTOGRAM = Integer.MIN_VALUE;

    /**
     * To create a new {@link HistogramSnapshot}, you can either call the constructor directly or use
     * the builder with {@link HistogramSnapshot#builder()}.
     *
     * @param metadata see {@link MetricMetadata} for naming conventions.
     * @param data     the constructor will create a sorted copy of the collection.
     */
    public HistogramSnapshot(MetricMetadata metadata, Collection<HistogramDataPointSnapshot> data) {
        this(false, metadata, data);
    }

    /**
     * Use this with the first parameter {@code true} to create a snapshot of a Gauge Histogram.
     * The data model for Gauge Histograms is the same as for regular histograms, except that bucket values
     * are semantically gauges and not counters.
     * See <a href="https://openmetrics.io">openmetrics.io</a> for more info on Gauge Histograms.
     */
    public HistogramSnapshot(boolean isGaugeHistogram, MetricMetadata metadata, Collection<HistogramDataPointSnapshot> data) {
        super(metadata, data);
        this.gaugeHistogram = isGaugeHistogram;
    }

    public boolean isGaugeHistogram() {
        return gaugeHistogram;
    }

    @Override
    public List<HistogramDataPointSnapshot> getDataPoints() {
        return (List<HistogramDataPointSnapshot>) dataPoints;
    }

    public static final class HistogramDataPointSnapshot extends DistributionDataPointSnapshot {

        // There are two types of histograms: Classic histograms and native histograms.
        // Classic histograms have a fixed set of buckets.
        // Native histograms have "infinitely many" buckets with exponentially growing boundaries.
        // The OpenTelemetry terminology for native histogram is "exponential histogram".
        // ---
        // A histogram can be a classic histogram (indicated by nativeSchema == CLASSIC_HISTOGRAM),
        // or a native histogram (indicated by classicBuckets == ClassicHistogramBuckets.EMPTY),
        // or both.
        // ---
        // A histogram that is both classic and native is great for migrating from classic histograms
        // to native histograms: Old Prometheus servers can still scrape the classic histogram, while
        // new Prometheus servers can scrape the native histogram.

        private final ClassicHistogramBuckets classicBuckets; // May be ClassicHistogramBuckets.EMPTY for native histograms.
        private final int nativeSchema; // Number in [-4, 8]. May be CLASSIC_HISTOGRAM for classic histograms.
        private final long nativeZeroCount; // only used if nativeSchema != CLASSIC_HISTOGRAM
        private final double nativeZeroThreshold; // only used if nativeSchema != CLASSIC_HISTOGRAM
        private final NativeHistogramBuckets nativeBucketsForPositiveValues; // only used if nativeSchema != CLASSIC_HISTOGRAM
        private final NativeHistogramBuckets nativeBucketsForNegativeValues; // only used if nativeSchema != CLASSIC_HISTOGRAM

        /**
         * Constructor for classic histograms (as opposed to native histograms).
         * <p>
         * To create a new {@link HistogramDataPointSnapshot}, you can either call the constructor directly or use the
         * Builder with {@link HistogramSnapshot#builder()}.
         *
         * @param classicBuckets         required. Must not be empty. Must at least contain the +Inf bucket.
         * @param sum                    sum of all observed values. Optional, pass {@link Double#NaN} if not available.
         * @param labels                 must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplars              must not be null. Use {@link Exemplars#EMPTY} if there are no Exemplars.
         * @param createdTimestampMillis timestamp (as in {@link System#currentTimeMillis()}) when the time series
         *                               (this specific set of labels) was created (or reset to zero).
         *                               It's optional. Use {@code 0L} if there is no created timestamp.
         */
        public HistogramDataPointSnapshot(
                ClassicHistogramBuckets classicBuckets,
                double sum,
                Labels labels,
                Exemplars exemplars,
                long createdTimestampMillis) {
            this(classicBuckets, CLASSIC_HISTOGRAM, 0, 0, NativeHistogramBuckets.EMPTY, NativeHistogramBuckets.EMPTY, sum, labels, exemplars, createdTimestampMillis, 0L);
        }

        /**
         * Constructor for native histograms (as opposed to classic histograms).
         * <p>
         * To create a new {@link HistogramDataPointSnapshot}, you can either call the constructor directly or use the
         * Builder with {@link HistogramSnapshot#builder()}.
         *
         * @param nativeSchema                   number in [-4, 8]. See <a href="https://github.com/prometheus/client_model/blob/7f720d22828060526c55ac83bceff08f43d4cdbc/io/prometheus/client/metrics.proto#L76-L80">Prometheus client_model metrics.proto</a>.
         * @param nativeZeroCount                number of observed zero values (zero is special because there is no
         *                                       histogram bucket for zero values).
         * @param nativeZeroThreshold            observations in [-zeroThreshold, +zeroThreshold] are treated as zero.
         *                                       This is to avoid creating a large number of buckets if observations fluctuate around zero.
         * @param nativeBucketsForPositiveValues must not be {@code null}. Use {@link NativeHistogramBuckets#EMPTY} if empty.
         * @param nativeBucketsForNegativeValues must not be {@code null}. Use {@link NativeHistogramBuckets#EMPTY} if empty.
         * @param sum                            sum of all observed values. Optional, use {@link Double#NaN} if not available.
         * @param labels                         must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplars                      must not be null. Use {@link Exemplars#EMPTY} if there are no Exemplars.
         * @param createdTimestampMillis         timestamp (as in {@link System#currentTimeMillis()}) when the time series
         *                                       (this specific set of labels) was created (or reset to zero).
         *                                       It's optional. Use {@code 0L} if there is no created timestamp.
         */
        public HistogramDataPointSnapshot(
                int nativeSchema,
                long nativeZeroCount,
                double nativeZeroThreshold,
                NativeHistogramBuckets nativeBucketsForPositiveValues,
                NativeHistogramBuckets nativeBucketsForNegativeValues,
                double sum,
                Labels labels,
                Exemplars exemplars,
                long createdTimestampMillis) {
            this(ClassicHistogramBuckets.EMPTY, nativeSchema, nativeZeroCount, nativeZeroThreshold, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues, sum, labels, exemplars, createdTimestampMillis, 0L);
        }

        /**
         * Constructor for a histogram with both, classic and native data.
         * <p>
         * To create a new {@link HistogramDataPointSnapshot}, you can either call the constructor directly or use the
         * Builder with {@link HistogramSnapshot#builder()}.
         *
         * @param classicBuckets                 required. Must not be empty. Must at least contain the +Inf bucket.
         * @param nativeSchema                   number in [-4, 8]. See <a href="https://github.com/prometheus/client_model/blob/7f720d22828060526c55ac83bceff08f43d4cdbc/io/prometheus/client/metrics.proto#L76-L80">Prometheus client_model metrics.proto</a>.
         * @param nativeZeroCount                number of observed zero values (zero is special because there is no
         *                                       histogram bucket for zero values).
         * @param nativeZeroThreshold            observations in [-zeroThreshold, +zeroThreshold] are treated as zero.
         *                                       This is to avoid creating a large number of buckets if observations fluctuate around zero.
         * @param nativeBucketsForPositiveValues must not be {@code null}. Use {@link NativeHistogramBuckets#EMPTY} if empty.
         * @param nativeBucketsForNegativeValues must not be {@code null}. Use {@link NativeHistogramBuckets#EMPTY} if empty.
         * @param sum                            sum of all observed values. Optional, use {@link Double#NaN} if not available.
         * @param labels                         must not be null. Use {@link Labels#EMPTY} if there are no labels.
         * @param exemplars                      must not be null. Use {@link Exemplars#EMPTY} if there are no Exemplars.
         * @param createdTimestampMillis         timestamp (as in {@link System#currentTimeMillis()}) when the time series
         *                                       (this specific set of labels) was created (or reset to zero).
         *                                       It's optional. Use {@code 0L} if there is no created timestamp.
         */
        public HistogramDataPointSnapshot(
                ClassicHistogramBuckets classicBuckets,
                int nativeSchema,
                long nativeZeroCount,
                double nativeZeroThreshold,
                NativeHistogramBuckets nativeBucketsForPositiveValues,
                NativeHistogramBuckets nativeBucketsForNegativeValues,
                double sum,
                Labels labels,
                Exemplars exemplars,
                long createdTimestampMillis) {
            this(classicBuckets, nativeSchema, nativeZeroCount, nativeZeroThreshold, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues, sum, labels, exemplars, createdTimestampMillis, 0L);
        }

        /**
         * Constructor with an additional scrape timestamp.
         * This is only useful in rare cases as the scrape timestamp is usually set by the Prometheus server
         * during scraping. Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public HistogramDataPointSnapshot(
                ClassicHistogramBuckets classicBuckets,
                int nativeSchema,
                long nativeZeroCount,
                double nativeZeroThreshold,
                NativeHistogramBuckets nativeBucketsForPositiveValues,
                NativeHistogramBuckets nativeBucketsForNegativeValues,
                double sum,
                Labels labels,
                Exemplars exemplars,
                long createdTimestampMillis,
                long scrapeTimestampMillis) {
            super(calculateCount(classicBuckets, nativeSchema, nativeZeroCount, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues), sum, exemplars, labels, createdTimestampMillis, scrapeTimestampMillis);
            this.classicBuckets = classicBuckets;
            this.nativeSchema = nativeSchema;
            this.nativeZeroCount = nativeSchema == CLASSIC_HISTOGRAM ? 0 : nativeZeroCount;
            this.nativeZeroThreshold = nativeSchema == CLASSIC_HISTOGRAM ? 0 : nativeZeroThreshold;
            this.nativeBucketsForPositiveValues = nativeSchema == CLASSIC_HISTOGRAM ? NativeHistogramBuckets.EMPTY : nativeBucketsForPositiveValues;
            this.nativeBucketsForNegativeValues = nativeSchema == CLASSIC_HISTOGRAM ? NativeHistogramBuckets.EMPTY : nativeBucketsForNegativeValues;
            validate();
        }

        private static long calculateCount(ClassicHistogramBuckets classicBuckets, int nativeSchema, long nativeZeroCount, NativeHistogramBuckets nativeBucketsForPositiveValues, NativeHistogramBuckets nativeBucketsForNegativeValues) {
            if (classicBuckets.isEmpty()) {
                // This is a native histogram
                return calculateNativeCount(nativeZeroCount, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues);
            } else if (nativeSchema == CLASSIC_HISTOGRAM) {
                // This is a classic histogram
                return calculateClassicCount(classicBuckets);
            } else {
                // This is both, a native and a classic histogram. Count should be the same for both.
                long classicCount = calculateClassicCount(classicBuckets);
                long nativeCount = calculateNativeCount(nativeZeroCount, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues);
                if (classicCount != nativeCount) {
                    throw new IllegalArgumentException("Inconsistent observation count: If a histogram has both classic and native data the observation count must be the same. Classic count is " + classicCount + " but native count is " + nativeCount + ".");
                }
                return classicCount;
            }
        }

        private static long calculateClassicCount(ClassicHistogramBuckets classicBuckets) {
            long count = 0;
            for (int i = 0; i < classicBuckets.size(); i++) {
                count += classicBuckets.getCount(i);
            }
            return count;
        }

        private static long calculateNativeCount(long nativeZeroCount, NativeHistogramBuckets nativeBucketsForPositiveValues, NativeHistogramBuckets nativeBucketsForNegativeValues) {
            long count = nativeZeroCount;
            for (int i = 0; i < nativeBucketsForNegativeValues.size(); i++) {
                count += nativeBucketsForNegativeValues.getCount(i);
            }
            for (int i = 0; i < nativeBucketsForPositiveValues.size(); i++) {
                count += nativeBucketsForPositiveValues.getCount(i);
            }
            return count;
        }

        public boolean hasClassicHistogramData() {
            return !classicBuckets.isEmpty();
        }

        public boolean hasNativeHistogramData() {
            return nativeSchema != CLASSIC_HISTOGRAM;
        }

        /**
         * Will return garbage if {@link #hasClassicHistogramData()} is {@code false}.
         */
        public ClassicHistogramBuckets getClassicBuckets() {
            return classicBuckets;
        }

        /**
         * The schema defines the scale of the native histogram, i.g. the granularity of the buckets.
         * Current supported values are -4 &lt;= schema &lt;= 8.
         * See {@link NativeHistogramBuckets} for more info.
         * This will return garbage if {@link #hasNativeHistogramData()} is {@code false}.
         */
        public int getNativeSchema() {
            return nativeSchema;
        }

        /**
         * Number of observed zero values.
         * Will return garbage if {@link #hasNativeHistogramData()} is {@code false}.
         */
        public long getNativeZeroCount() {
            return nativeZeroCount;
        }

        /**
         * All observations in [-nativeZeroThreshold; +nativeZeroThreshold] are treated as zero.
         * This is useful to avoid creation of a large number of buckets if observations fluctuate around zero.
         * Will return garbage if {@link #hasNativeHistogramData()} is {@code false}.
         */
        public double getNativeZeroThreshold() {
            return nativeZeroThreshold;
        }

        /**
         * Will return garbage if {@link #hasNativeHistogramData()} is {@code false}.
         */
        public NativeHistogramBuckets getNativeBucketsForPositiveValues() {
            return nativeBucketsForPositiveValues;
        }

        /**
         * Will return garbage if {@link #hasNativeHistogramData()} is {@code false}.
         */
        public NativeHistogramBuckets getNativeBucketsForNegativeValues() {
            return nativeBucketsForNegativeValues;
        }

        private void validate() {
            for (Label label : getLabels()) {
                if (label.getName().equals("le")) {
                    throw new IllegalArgumentException("le is a reserved label name for histograms");
                }
            }
            if (nativeSchema == CLASSIC_HISTOGRAM && classicBuckets.isEmpty()) {
                throw new IllegalArgumentException("Histogram buckets cannot be empty, must at least have the +Inf bucket.");
            }
            if (nativeSchema != CLASSIC_HISTOGRAM) {
                if (nativeSchema < -4 || nativeSchema > 8) {
                    throw new IllegalArgumentException(nativeSchema + ": illegal schema. Expecting number in [-4, 8].");
                }
                if (nativeZeroCount < 0) {
                    throw new IllegalArgumentException(nativeZeroCount + ": nativeZeroCount cannot be negative");
                }
                if (Double.isNaN(nativeZeroThreshold) || nativeZeroThreshold < 0) {
                    throw new IllegalArgumentException(nativeZeroThreshold + ": illegal nativeZeroThreshold. Must be >= 0.");
                }
            }
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder extends DistributionDataPointSnapshot.Builder<Builder> {

            private ClassicHistogramBuckets classicHistogramBuckets = ClassicHistogramBuckets.EMPTY;
            private int nativeSchema = CLASSIC_HISTOGRAM;
            private long nativeZeroCount = 0;
            private double nativeZeroThreshold = 0;
            private NativeHistogramBuckets nativeBucketsForPositiveValues = NativeHistogramBuckets.EMPTY;
            private NativeHistogramBuckets nativeBucketsForNegativeValues = NativeHistogramBuckets.EMPTY;

            private Builder() {
            }

            @Override
            protected Builder self() {
                return this;
            }

            public Builder classicHistogramBuckets(ClassicHistogramBuckets classicBuckets) {
                this.classicHistogramBuckets = classicBuckets;
                return this;
            }

            public Builder nativeSchema(int nativeSchema) {
                this.nativeSchema = nativeSchema;
                return this;
            }

            public Builder nativeZeroCount(long zeroCount) {
                this.nativeZeroCount = zeroCount;
                return this;
            }

            public Builder nativeZeroThreshold(double zeroThreshold) {
                this.nativeZeroThreshold = zeroThreshold;
                return this;
            }

            public Builder nativeBucketsForPositiveValues(NativeHistogramBuckets bucketsForPositiveValues) {
                this.nativeBucketsForPositiveValues = bucketsForPositiveValues;
                return this;
            }

            public Builder nativeBucketsForNegativeValues(NativeHistogramBuckets bucketsForNegativeValues) {
                this.nativeBucketsForNegativeValues = bucketsForNegativeValues;
                return this;
            }

            public HistogramDataPointSnapshot build() {
                if (nativeSchema == CLASSIC_HISTOGRAM && classicHistogramBuckets.isEmpty()) {
                    throw new IllegalArgumentException("One of nativeSchema and classicHistogramBuckets is required.");
                }
                return new HistogramDataPointSnapshot(classicHistogramBuckets, nativeSchema, nativeZeroCount, nativeZeroThreshold, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues, sum, labels, exemplars, createdTimestampMillis, scrapeTimestampMillis);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends MetricSnapshot.Builder<Builder> {

        private final List<HistogramDataPointSnapshot> dataPoints = new ArrayList<>();
        private boolean isGaugeHistogram = false;

        private Builder() {
        }

        /**
         * Add a data point. Call multiple times to add multiple data points.
         */
        public Builder dataPoint(HistogramDataPointSnapshot dataPoint) {
            dataPoints.add(dataPoint);
            return this;
        }

        /**
         * {@code true} indicates that this histogram is a gauge histogram.
         * The data model for gauge histograms is the same as for regular histograms,
         * except that bucket values are semantically gauges and not counters.
         * See <a href="https://openmetrics.io">openmetrics.io</a> for more info on gauge histograms.
         */
        public Builder gaugeHistogram(boolean isGaugeHistogram) {
            this.isGaugeHistogram = isGaugeHistogram;
            return this;
        }

        public HistogramSnapshot build() {
            return new HistogramSnapshot(isGaugeHistogram, buildMetadata(), dataPoints);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
