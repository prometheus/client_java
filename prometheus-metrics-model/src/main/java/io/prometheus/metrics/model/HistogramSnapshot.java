package io.prometheus.metrics.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class HistogramSnapshot extends MetricSnapshot {

    private final boolean gaugeHistogram;
    public static final int CLASSIC_HISTOGRAM = Integer.MIN_VALUE;

    public HistogramSnapshot(MetricMetadata metadata, Collection<HistogramData> data) {
        this(false, metadata, data);
    }

    public HistogramSnapshot(boolean isGaugeHistogram, MetricMetadata metadata, Collection<HistogramData> data) {
        super(metadata, data);
        this.gaugeHistogram = isGaugeHistogram;
    }

    public boolean isGaugeHistogram() {
        return gaugeHistogram;
    }

    @Override
    public List<HistogramData> getData() {
        return (List<HistogramData>) data;
    }

    public static final class HistogramData extends DistributionData {

        private final ClassicHistogramBuckets classicBuckets;
        private final int nativeSchema;
        private final long nativeZeroCount;
        private final double nativeZeroThreshold;
        private final NativeHistogramBuckets nativeBucketsForPositiveValues;
        private final NativeHistogramBuckets nativeBucketsForNegativeValues;

        /**
         * Constructor for classic histograms.
         */
        public HistogramData(
                ClassicHistogramBuckets classicBuckets,
                double sum,
                Labels labels,
                Exemplars exemplars,
                long createdTimestampMillis) {
            this(classicBuckets, CLASSIC_HISTOGRAM, 0, 0, NativeHistogramBuckets.EMPTY, NativeHistogramBuckets.EMPTY, sum, labels, exemplars, createdTimestampMillis, 0L);
        }

        /**
         * Constructor for native histograms.
         */
        public HistogramData(
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
         */
        public HistogramData(
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
         * Constructor with an additional scrape timestamp parameter. In most cases you should not need this,
         * as the scrape timestamp is set by the Prometheus server during scraping.
         * Exceptions include mirroring metrics with given timestamps from other metric sources.
         */
        public HistogramData(
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
            for (int i=0; i<classicBuckets.size(); i++) {
                count += classicBuckets.getCount(i);
            }
            return count;
        }

        private static long calculateNativeCount(long nativeZeroCount, NativeHistogramBuckets nativeBucketsForPositiveValues, NativeHistogramBuckets nativeBucketsForNegativeValues) {
            long count = nativeZeroCount;
            for (int i=0; i<nativeBucketsForNegativeValues.size(); i++) {
                count += nativeBucketsForNegativeValues.getCount(i);
            }
            for (int i=0; i<nativeBucketsForPositiveValues.size(); i++) {
                count += nativeBucketsForPositiveValues.getCount(i);
            }
            return count;
        }

        public ClassicHistogramBuckets getClassicBuckets() {
            return classicBuckets;
        }

        public int getNativeSchema() {
            return nativeSchema;
        }

        public long getNativeZeroCount() {
            return nativeZeroCount;
        }

        public double getNativeZeroThreshold() {
            return nativeZeroThreshold;
        }

        public NativeHistogramBuckets getNativeBucketsForPositiveValues() {
            return nativeBucketsForPositiveValues;
        }

        public NativeHistogramBuckets getNativeBucketsForNegativeValues() {
            return nativeBucketsForNegativeValues;
        }

        @Override
        protected void validate() {
            for (Label label : getLabels()) {
                if (label.getName().equals("le")) {
                    throw new IllegalArgumentException("le is a reserved label name for histograms");
                }
            }
            if (nativeSchema == CLASSIC_HISTOGRAM) {
                // validate classic histogram
                if (classicBuckets.isEmpty()) {
                    throw new IllegalArgumentException("A classic histogram must not have empty classic histogram buckets.");
                }
            } else {
                // validate native histogram
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

        public static class Builder extends DistributionData.Builder<Builder> {

            private ClassicHistogramBuckets classicHistogramBuckets = ClassicHistogramBuckets.EMPTY;
            private int nativeSchema = CLASSIC_HISTOGRAM;
            private long nativeZeroCount = 0;
            private double nativeZeroThreshold = 0;
            private NativeHistogramBuckets nativeBucketsForPositiveValues = NativeHistogramBuckets.EMPTY;
            private NativeHistogramBuckets nativeBucketsForNegativeValues = NativeHistogramBuckets.EMPTY;

            private Builder() {}

            @Override
            protected Builder self() {
                return this;
            }

            public Builder withClassicHistogramBuckets(ClassicHistogramBuckets classicBuckets) {
                this.classicHistogramBuckets = classicBuckets;
                return this;
            }

            public Builder withNativeSchema(int nativeSchema) {
                this.nativeSchema = nativeSchema;
                return this;
            }

            public Builder withNativeZeroCount(long zeroCount) {
                this.nativeZeroCount = zeroCount;
                return this;
            }

            public Builder withNativeZeroThreshold(double zeroThreshold) {
                this.nativeZeroThreshold = zeroThreshold;
                return this;
            }

            public Builder withNativeBucketsForPositiveValues(NativeHistogramBuckets bucketsForPositiveValues) {
                this.nativeBucketsForPositiveValues = bucketsForPositiveValues;
                return this;
            }

            public Builder withNativeBucketsForNegativeValues(NativeHistogramBuckets bucketsForNegativeValues) {
                this.nativeBucketsForNegativeValues = bucketsForNegativeValues;
                return this;
            }

            public HistogramData build() {
                if (nativeSchema == CLASSIC_HISTOGRAM && classicHistogramBuckets.isEmpty()) {
                    throw new IllegalArgumentException("One of nativeSchema and classicHistogramBuckets is required.");
                }
                return new HistogramData(classicHistogramBuckets, nativeSchema, nativeZeroCount, nativeZeroThreshold, nativeBucketsForPositiveValues, nativeBucketsForNegativeValues, sum, labels, exemplars, createdTimestampMillis, scrapeTimestampMillis);
            }
        }

        public static Builder newBuilder() {
            return new Builder();
        }
    }

    public static class Builder extends MetricSnapshot.Builder<Builder> {

        private final List<HistogramData> histogramData = new ArrayList<>();
        private boolean isGaugeHistogram = false;

        private Builder() {
        }

        public Builder addData(HistogramData data) {
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

        public HistogramSnapshot build() {
            return new HistogramSnapshot(isGaugeHistogram, buildMetadata(), histogramData);
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
