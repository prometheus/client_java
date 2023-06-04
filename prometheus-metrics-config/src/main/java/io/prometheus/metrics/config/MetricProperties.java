package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.metrics
 */
public class MetricProperties {

    private static final String EXEMPLARS_ENABLED = "exemplarsEnabled";
    private static final String HISTOGRAM_NATIVE_ONLY = "histogramNativeOnly";
    private static final String HISTOGRAM_CLASSIC_ONLY = "histogramClassicOnly";
    private static final String HISTOGRAM_CLASSIC_UPPER_BOUNDS = "histogramClassicUpperBounds";
    private static final String HISTOGRAM_NATIVE_INITIAL_SCHEMA = "histogramNativeInitialSchema";
    private static final String HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD = "histogramNativeMinZeroThreshold";
    private static final String HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD = "histogramNativeMaxZeroThreshold";
    private static final String HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS = "histogramNativeMaxNumberOfBuckets"; // 0 means unlimited number of buckets
    private static final String HISTOGRAM_NATIVE_RESET_DURATION_SECONDS = "histogramNativeResetDurationSeconds"; // 0 means no reset
    private static final String SUMMARY_QUANTILES = "summaryQuantiles";
    private static final String SUMMARY_QUANTILE_ERRORS = "summaryQuantileErrors";
    private static final String SUMMARY_MAX_AGE_SECONDS = "summaryMaxAgeSeconds";
    private static final String SUMMARY_NUMBER_OF_AGE_BUCKETS = "summaryNumberOfAgeBuckets";

    private final Boolean exemplarsEnabled;
    private final Boolean histogramNativeOnly;
    private final Boolean histogramClassicOnly;
    private final double[] histogramClassicUpperBounds;
    private final Integer histogramNativeInitialSchema;
    private final Double histogramNativeMinZeroThreshold;
    private final Double histogramNativeMaxZeroThreshold;
    private final Integer histogramNativeMaxNumberOfBuckets;
    private final Long histogramNativeResetDurationSeconds;
    private final double[] summaryQuantiles;
    private final double[] summaryQuantileErrors;
    private final Long summaryMaxAgeSeconds;
    private final Integer summaryNumberOfAgeBuckets;

    public MetricProperties(
            Boolean exemplarsEnabled,
            Boolean histogramNativeOnly,
            Boolean histogramClassicOnly,
            double[] histogramClassicUpperBounds,
            Integer histogramNativeInitialSchema,
            Double histogramNativeMinZeroThreshold,
            Double histogramNativeMaxZeroThreshold,
            Integer histogramNativeMaxNumberOfBuckets,
            Long histogramNativeResetDurationSeconds,
            double[] summaryQuantiles,
            double[] summaryQuantileErrors,
            Long summaryMaxAgeSeconds,
            Integer summaryNumberOfAgeBuckets) {
        this(exemplarsEnabled,
                histogramNativeOnly,
                histogramClassicOnly,
                histogramClassicUpperBounds,
                histogramNativeInitialSchema,
                histogramNativeMinZeroThreshold,
                histogramNativeMaxZeroThreshold,
                histogramNativeMaxNumberOfBuckets,
                histogramNativeResetDurationSeconds,
                summaryQuantiles,
                summaryQuantileErrors,
                summaryMaxAgeSeconds,
                summaryNumberOfAgeBuckets,
                "");
    }

    private MetricProperties(
            Boolean exemplarsEnabled,
            Boolean histogramNativeOnly,
            Boolean histogramClassicOnly,
            double[] histogramClassicUpperBounds,
            Integer histogramNativeInitialSchema,
            Double histogramNativeMinZeroThreshold,
            Double histogramNativeMaxZeroThreshold,
            Integer histogramNativeMaxNumberOfBuckets,
            Long histogramNativeResetDurationSeconds,
            double[] summaryQuantiles,
            double[] summaryQuantileErrors,
            Long summaryMaxAgeSeconds,
            Integer summaryNumberOfAgeBuckets,
            String configPropertyPrefix) {
        this.exemplarsEnabled = exemplarsEnabled;
        this.histogramNativeOnly = isHistogramNativeOnly(histogramClassicOnly, histogramNativeOnly);
        this.histogramClassicOnly = isHistogramClassicOnly(histogramClassicOnly, histogramNativeOnly);
        this.histogramClassicUpperBounds = histogramClassicUpperBounds;
        this.histogramNativeInitialSchema = histogramNativeInitialSchema;
        this.histogramNativeMinZeroThreshold = histogramNativeMinZeroThreshold;
        this.histogramNativeMaxZeroThreshold = histogramNativeMaxZeroThreshold;
        this.histogramNativeMaxNumberOfBuckets = histogramNativeMaxNumberOfBuckets;
        this.histogramNativeResetDurationSeconds = histogramNativeResetDurationSeconds;
        this.summaryQuantiles = summaryQuantiles;
        this.summaryQuantileErrors = summaryQuantileErrors;
        this.summaryMaxAgeSeconds = summaryMaxAgeSeconds;
        this.summaryNumberOfAgeBuckets = summaryNumberOfAgeBuckets;
        validate(configPropertyPrefix);
    }


    private Boolean isHistogramClassicOnly(Boolean histogramClassicOnly, Boolean histogramNativeOnly) {
        if (histogramClassicOnly == null && histogramNativeOnly == null) {
            return null;
        }
        if (histogramClassicOnly != null) {
            return histogramClassicOnly;
        }
        return !histogramNativeOnly;
    }

    private Boolean isHistogramNativeOnly(Boolean histogramClassicOnly, Boolean histogramNativeOnly) {
        if (histogramClassicOnly == null && histogramNativeOnly == null) {
            return null;
        }
        if (histogramNativeOnly != null) {
            return histogramNativeOnly;
        }
        return !histogramClassicOnly;
    }

    private void validate(String prefix) throws PrometheusPropertiesException {
        Util.assertValue(histogramNativeInitialSchema, s -> s >= -4 && s <= 8, "Expecting number between -4 and +8.", prefix, HISTOGRAM_NATIVE_INITIAL_SCHEMA);
        Util.assertValue(histogramNativeMinZeroThreshold, t -> t >= 0, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD);
        Util.assertValue(histogramNativeMaxZeroThreshold, t -> t >= 0, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD);
        Util.assertValue(histogramNativeMaxNumberOfBuckets, n -> n >= 0, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS);
        Util.assertValue(histogramNativeResetDurationSeconds, t -> t >= 0, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_RESET_DURATION_SECONDS);
        Util.assertValue(summaryMaxAgeSeconds, t -> t > 0, "Expecting value > 0", prefix, SUMMARY_MAX_AGE_SECONDS);
        Util.assertValue(summaryNumberOfAgeBuckets, t -> t > 0, "Expecting value > 0", prefix, SUMMARY_NUMBER_OF_AGE_BUCKETS);

        if (Boolean.TRUE.equals(histogramNativeOnly) && Boolean.TRUE.equals(histogramClassicOnly)) {
            throw new PrometheusPropertiesException(prefix + "." + HISTOGRAM_NATIVE_ONLY + " and " + prefix + "." + HISTOGRAM_CLASSIC_ONLY + " cannot both be true");
        }

        if (histogramNativeMinZeroThreshold != null && histogramNativeMaxZeroThreshold != null) {
            if (histogramNativeMinZeroThreshold > histogramNativeMaxZeroThreshold) {
                throw new PrometheusPropertiesException(prefix + "." + HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD + " cannot be greater than " + prefix + "." + HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD);
            }
        }

        if (summaryQuantiles != null) {
            for (double quantile : summaryQuantiles) {
                if (quantile < 0 || quantile > 1) {
                    throw new PrometheusPropertiesException(prefix + "." + SUMMARY_QUANTILES + ": Expecting 0.0 <= quantile <= 1.0");
                }
            }
        }

        if (summaryQuantileErrors != null) {
            if (summaryQuantiles != null) {
                throw new PrometheusPropertiesException(prefix + "." + SUMMARY_QUANTILE_ERRORS + ": Can't configure " + SUMMARY_QUANTILE_ERRORS + " without configuring " + SUMMARY_QUANTILES);
            }
            if (summaryQuantileErrors.length != summaryQuantiles.length) {
                throw new PrometheusPropertiesException(prefix + "." + SUMMARY_QUANTILE_ERRORS + ": must have the same length as " + SUMMARY_QUANTILES);
            }
            for (double error : summaryQuantileErrors) {
                if (error < 0 || error > 1) {
                    throw new PrometheusPropertiesException(prefix + "." + SUMMARY_QUANTILE_ERRORS + ": Expecting 0.0 <= error <= 1.0");
                }
            }
        }
    }

    public Boolean getExemplarsEnabled() {
        return exemplarsEnabled;
    }

    public Boolean getHistogramNativeOnly() {
        return histogramNativeOnly;
    }

    public Boolean getHistogramClassicOnly() {
        return histogramClassicOnly;
    }

    public double[] getHistogramClassicUpperBounds() {
        return histogramClassicUpperBounds;
    }

    public Integer getHistogramNativeInitialSchema() {
        return histogramNativeInitialSchema;
    }

    public Double getHistogramNativeMinZeroThreshold() {
        return histogramNativeMinZeroThreshold;
    }

    public Double getHistogramNativeMaxZeroThreshold() {
        return histogramNativeMaxZeroThreshold;
    }

    public Integer getHistogramNativeMaxNumberOfBuckets() {
        return histogramNativeMaxNumberOfBuckets;
    }

    public Long getHistogramNativeResetDurationSeconds() {
        return histogramNativeResetDurationSeconds;
    }

    public double[] getSummaryQuantiles() {
        return summaryQuantiles;
    }

    public double[] getSummaryQuantileErrors() {
        if (summaryQuantileErrors != null) {
            return summaryQuantileErrors;
        } else {
            double[] result = new double[summaryQuantiles.length];
            for (int i = 0; i < result.length; i++) {
                if (summaryQuantiles[i] <= 0.01 || summaryQuantiles[i] >= 0.99) {
                    result[i] = 0.001;
                } else if (summaryQuantiles[i] <= 0.02 || summaryQuantiles[i] >= 0.98) {
                    result[i] = 0.005;
                } else {
                    result[i] = 0.01;
                }
            }
            return result;
        }
    }

    public Long getSummaryMaxAgeSeconds() {
        return summaryMaxAgeSeconds;
    }

    public Integer getSummaryNumberOfAgeBuckets() {
        return summaryNumberOfAgeBuckets;
    }

    /**
     * Properties that are loaded will be removed from the map. The remaining properties are unused.
     */
    static MetricProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        return new MetricProperties(
                Util.loadBoolean(prefix + "." + EXEMPLARS_ENABLED, properties),
                Util.loadBoolean(prefix + "." + HISTOGRAM_NATIVE_ONLY, properties),
                Util.loadBoolean(prefix + "." + HISTOGRAM_CLASSIC_ONLY, properties),
                Util.loadDoubleArray(prefix + "." + HISTOGRAM_CLASSIC_UPPER_BOUNDS, properties),
                Util.loadInteger(prefix + "." + HISTOGRAM_NATIVE_INITIAL_SCHEMA, properties),
                Util.loadDouble(prefix + "." + HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD, properties),
                Util.loadDouble(prefix + "." + HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD, properties),
                Util.loadInteger(prefix + "." + HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS, properties),
                Util.loadLong(prefix + "." + HISTOGRAM_NATIVE_RESET_DURATION_SECONDS, properties),
                Util.loadDoubleArray(prefix + "." + SUMMARY_QUANTILES, properties),
                Util.loadDoubleArray(prefix + "." + SUMMARY_QUANTILE_ERRORS, properties),
                Util.loadLong(prefix + "." + SUMMARY_MAX_AGE_SECONDS, properties),
                Util.loadInteger(prefix + "." + SUMMARY_NUMBER_OF_AGE_BUCKETS, properties),
                prefix);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean exemplarsEnabled;
        private Boolean histogramNativeOnly;
        private Boolean histogramClassicOnly;
        private double[] histogramClassicUpperBounds;
        private Integer histogramNativeInitialSchema;
        private Double histogramNativeMinZeroThreshold;
        private Double histogramNativeMaxZeroThreshold;
        private Integer histogramNativeMaxNumberOfBuckets;
        private Long histogramNativeResetDurationSeconds;
        private double[] summaryQuantiles;
        private double[] summaryQuantileErrors;
        private Long summaryMaxAgeSeconds;
        private Integer summaryNumberOfAgeBuckets;

        private Builder() {
        }

        public MetricProperties build() {
            return new MetricProperties(exemplarsEnabled,
                    histogramNativeOnly,
                    histogramClassicOnly,
                    histogramClassicUpperBounds,
                    histogramNativeInitialSchema,
                    histogramNativeMinZeroThreshold,
                    histogramNativeMaxZeroThreshold,
                    histogramNativeMaxNumberOfBuckets,
                    histogramNativeResetDurationSeconds,
                    summaryQuantiles,
                    summaryQuantileErrors,
                    summaryMaxAgeSeconds,
                    summaryNumberOfAgeBuckets);
        }

        public Builder withExemplarsEnabled(Boolean exemplarsEnabled) {
            this.exemplarsEnabled = exemplarsEnabled;
            return this;
        }

        public Builder withHistogramNativeOnly(Boolean histogramNativeOnly) {
            this.histogramNativeOnly = histogramNativeOnly;
            return this;
        }

        public Builder withHistogramClassicOnly(Boolean histogramClassicOnly) {
            this.histogramClassicOnly = histogramClassicOnly;
            return this;
        }

        public Builder withHistogramClassicUpperBounds(double... histogramClassicUpperBounds) {
            this.histogramClassicUpperBounds = histogramClassicUpperBounds;
            return this;
        }

        public Builder withHistogramNativeInitialSchema(Integer histogramNativeInitialSchema) {
            this.histogramNativeInitialSchema = histogramNativeInitialSchema;
            return this;
        }

        public Builder withHistogramNativeMinZeroThreshold(Double histogramNativeMinZeroThreshold) {
            this.histogramNativeMinZeroThreshold = histogramNativeMinZeroThreshold;
            return this;
        }

        public Builder withHistogramNativeMaxZeroThreshold(Double histogramNativeMaxZeroThreshold) {
            this.histogramNativeMaxZeroThreshold = histogramNativeMaxZeroThreshold;
            return this;
        }

        public Builder withHistogramNativeMaxNumberOfBuckets(Integer histogramNativeMaxNumberOfBuckets) {
            this.histogramNativeMaxNumberOfBuckets = histogramNativeMaxNumberOfBuckets;
            return this;
        }

        public Builder withHistogramNativeResetDurationSeconds(Long histogramNativeResetDurationSeconds) {
            this.histogramNativeResetDurationSeconds = histogramNativeResetDurationSeconds;
            return this;
        }

        public Builder withSummaryQuantiles(double... summaryQuantiles) {
            this.summaryQuantiles = summaryQuantiles;
            return this;
        }

        public Builder withSummaryQuantileErrors(double... summaryQuantileErrors) {
            this.summaryQuantileErrors = summaryQuantileErrors;
            return this;
        }

        public Builder withSummaryMaxAgeSeconds(Long summaryMaxAgeSeconds) {
            this.summaryMaxAgeSeconds = summaryMaxAgeSeconds;
            return this;
        }

        public Builder withSummaryNumberOfAgeBuckets(Integer summaryNumberOfAgeBuckets) {
            this.summaryNumberOfAgeBuckets = summaryNumberOfAgeBuckets;
            return this;
        }
    }
}
