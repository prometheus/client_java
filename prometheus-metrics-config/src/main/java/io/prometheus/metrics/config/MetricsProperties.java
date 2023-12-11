package io.prometheus.metrics.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;

/**
 * Properties starting with io.prometheus.metrics
 */
public class MetricsProperties {

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
    private final List<Double> histogramClassicUpperBounds;
    private final Integer histogramNativeInitialSchema;
    private final Double histogramNativeMinZeroThreshold;
    private final Double histogramNativeMaxZeroThreshold;
    private final Integer histogramNativeMaxNumberOfBuckets;
    private final Long histogramNativeResetDurationSeconds;
    private final List<Double> summaryQuantiles;
    private final List<Double> summaryQuantileErrors;
    private final Long summaryMaxAgeSeconds;
    private final Integer summaryNumberOfAgeBuckets;

    public MetricsProperties(
            Boolean exemplarsEnabled,
            Boolean histogramNativeOnly,
            Boolean histogramClassicOnly,
            List<Double> histogramClassicUpperBounds,
            Integer histogramNativeInitialSchema,
            Double histogramNativeMinZeroThreshold,
            Double histogramNativeMaxZeroThreshold,
            Integer histogramNativeMaxNumberOfBuckets,
            Long histogramNativeResetDurationSeconds,
            List<Double> summaryQuantiles,
            List<Double> summaryQuantileErrors,
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

    private MetricsProperties(
            Boolean exemplarsEnabled,
            Boolean histogramNativeOnly,
            Boolean histogramClassicOnly,
            List<Double> histogramClassicUpperBounds,
            Integer histogramNativeInitialSchema,
            Double histogramNativeMinZeroThreshold,
            Double histogramNativeMaxZeroThreshold,
            Integer histogramNativeMaxNumberOfBuckets,
            Long histogramNativeResetDurationSeconds,
            List<Double> summaryQuantiles,
            List<Double> summaryQuantileErrors,
            Long summaryMaxAgeSeconds,
            Integer summaryNumberOfAgeBuckets,
            String configPropertyPrefix) {
        this.exemplarsEnabled = exemplarsEnabled;
        this.histogramNativeOnly = isHistogramNativeOnly(histogramClassicOnly, histogramNativeOnly);
        this.histogramClassicOnly = isHistogramClassicOnly(histogramClassicOnly, histogramNativeOnly);
        this.histogramClassicUpperBounds = histogramClassicUpperBounds == null ? null : unmodifiableList(new ArrayList<>(histogramClassicUpperBounds));
        this.histogramNativeInitialSchema = histogramNativeInitialSchema;
        this.histogramNativeMinZeroThreshold = histogramNativeMinZeroThreshold;
        this.histogramNativeMaxZeroThreshold = histogramNativeMaxZeroThreshold;
        this.histogramNativeMaxNumberOfBuckets = histogramNativeMaxNumberOfBuckets;
        this.histogramNativeResetDurationSeconds = histogramNativeResetDurationSeconds;
        this.summaryQuantiles = summaryQuantiles == null ? null : unmodifiableList(new ArrayList<>(summaryQuantiles));
        this.summaryQuantileErrors = summaryQuantileErrors == null ? null : unmodifiableList(new ArrayList<>(summaryQuantileErrors));
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
            if (summaryQuantiles == null) {
                throw new PrometheusPropertiesException(prefix + "." + SUMMARY_QUANTILE_ERRORS + ": Can't configure " + SUMMARY_QUANTILE_ERRORS + " without configuring " + SUMMARY_QUANTILES);
            }
            if (summaryQuantileErrors.size() != summaryQuantiles.size()) {
                throw new PrometheusPropertiesException(prefix + "." + SUMMARY_QUANTILE_ERRORS + ": must have the same length as " + SUMMARY_QUANTILES);
            }
            for (double error : summaryQuantileErrors) {
                if (error < 0 || error > 1) {
                    throw new PrometheusPropertiesException(prefix + "." + SUMMARY_QUANTILE_ERRORS + ": Expecting 0.0 <= error <= 1.0");
                }
            }
        }
    }

    /**
     * This is the only configuration property that can be applied to all metric types.
     * You can use it to turn Exemplar support off. Default is {@code true}.
     */
    public Boolean getExemplarsEnabled() {
        return exemplarsEnabled;
    }

    /**
     * See {@code Histogram.Builder.nativeOnly()}
     */
    public Boolean getHistogramNativeOnly() {
        return histogramNativeOnly;
    }

    /**
     * See {@code Histogram.Builder.classicOnly()}
     */
    public Boolean getHistogramClassicOnly() {
        return histogramClassicOnly;
    }

    /**
     * See {@code Histogram.Builder.classicBuckets()}
     */
    public List<Double> getHistogramClassicUpperBounds() {
        return histogramClassicUpperBounds;
    }

    /**
     * See {@code Histogram.Builder.nativeInitialSchema()}
     */
    public Integer getHistogramNativeInitialSchema() {
        return histogramNativeInitialSchema;
    }

    /**
     * See {@code Histogram.Builder.nativeMinZeroThreshold()}
     */
    public Double getHistogramNativeMinZeroThreshold() {
        return histogramNativeMinZeroThreshold;
    }

    /**
     * See {@code Histogram.Builder.nativeMaxZeroThreshold()}
     */
    public Double getHistogramNativeMaxZeroThreshold() {
        return histogramNativeMaxZeroThreshold;
    }

    /**
     * See {@code Histogram.Builder.nativeMaxNumberOfBuckets()}
     */
    public Integer getHistogramNativeMaxNumberOfBuckets() {
        return histogramNativeMaxNumberOfBuckets;
    }

    /**
     * See {@code Histogram.Builder.nativeResetDuration()}
     */
    public Long getHistogramNativeResetDurationSeconds() {
        return histogramNativeResetDurationSeconds;
    }

    /**
     * See {@code Summary.Builder.quantile()}
     */
    public List<Double> getSummaryQuantiles() {
        return summaryQuantiles;
    }

    /**
     * See {@code Summary.Builder.quantile()}
     * <p>
     * Returns {@code null} only if {@link #getSummaryQuantiles()} is also {@code null}.
     * Returns an empty list if {@link #getSummaryQuantiles()} are specified without specifying errors.
     * If the list is not empty, it has the same size as {@link #getSummaryQuantiles()}.
     */
    public List<Double> getSummaryQuantileErrors() {
        if (summaryQuantiles != null) {
            if (summaryQuantileErrors == null) {
                return Collections.emptyList();
            }
        }
        return summaryQuantileErrors;
    }

    /**
     * See {@code Summary.Builder.maxAgeSeconds()}
     */
    public Long getSummaryMaxAgeSeconds() {
        return summaryMaxAgeSeconds;
    }

    /**
     * See {@code Summary.Builder.numberOfAgeBuckets()}
     */
    public Integer getSummaryNumberOfAgeBuckets() {
        return summaryNumberOfAgeBuckets;
    }

    /**
     * Note that this will remove entries from {@code properties}.
     * This is because we want to know if there are unused properties remaining after all properties have been loaded.
     */
    static MetricsProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        return new MetricsProperties(
                Util.loadBoolean(prefix + "." + EXEMPLARS_ENABLED, properties),
                Util.loadBoolean(prefix + "." + HISTOGRAM_NATIVE_ONLY, properties),
                Util.loadBoolean(prefix + "." + HISTOGRAM_CLASSIC_ONLY, properties),
                Util.loadDoubleList(prefix + "." + HISTOGRAM_CLASSIC_UPPER_BOUNDS, properties),
                Util.loadInteger(prefix + "." + HISTOGRAM_NATIVE_INITIAL_SCHEMA, properties),
                Util.loadDouble(prefix + "." + HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD, properties),
                Util.loadDouble(prefix + "." + HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD, properties),
                Util.loadInteger(prefix + "." + HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS, properties),
                Util.loadLong(prefix + "." + HISTOGRAM_NATIVE_RESET_DURATION_SECONDS, properties),
                Util.loadDoubleList(prefix + "." + SUMMARY_QUANTILES, properties),
                Util.loadDoubleList(prefix + "." + SUMMARY_QUANTILE_ERRORS, properties),
                Util.loadLong(prefix + "." + SUMMARY_MAX_AGE_SECONDS, properties),
                Util.loadInteger(prefix + "." + SUMMARY_NUMBER_OF_AGE_BUCKETS, properties),
                prefix);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean exemplarsEnabled;
        private Boolean histogramNativeOnly;
        private Boolean histogramClassicOnly;
        private List<Double> histogramClassicUpperBounds;
        private Integer histogramNativeInitialSchema;
        private Double histogramNativeMinZeroThreshold;
        private Double histogramNativeMaxZeroThreshold;
        private Integer histogramNativeMaxNumberOfBuckets;
        private Long histogramNativeResetDurationSeconds;
        private List<Double> summaryQuantiles;
        private List<Double> summaryQuantileErrors;
        private Long summaryMaxAgeSeconds;
        private Integer summaryNumberOfAgeBuckets;

        private Builder() {
        }

        public MetricsProperties build() {
            return new MetricsProperties(exemplarsEnabled,
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

        /**
         * See {@link MetricsProperties#getExemplarsEnabled()}
         */
        public Builder exemplarsEnabled(Boolean exemplarsEnabled) {
            this.exemplarsEnabled = exemplarsEnabled;
            return this;
        }

        /**
         * See {@link MetricsProperties#getHistogramNativeOnly()}
         */
        public Builder histogramNativeOnly(Boolean histogramNativeOnly) {
            this.histogramNativeOnly = histogramNativeOnly;
            return this;
        }

        /**
         * See {@link MetricsProperties#getHistogramClassicOnly()}
         */
        public Builder histogramClassicOnly(Boolean histogramClassicOnly) {
            this.histogramClassicOnly = histogramClassicOnly;
            return this;
        }

        /**
         * See {@link MetricsProperties#getHistogramClassicUpperBounds()}
         */
        public Builder histogramClassicUpperBounds(double... histogramClassicUpperBounds) {
            this.histogramClassicUpperBounds = Util.toList(histogramClassicUpperBounds);
            return this;
        }

        /**
         * See {@link MetricsProperties#getHistogramNativeInitialSchema()}
         */
        public Builder histogramNativeInitialSchema(Integer histogramNativeInitialSchema) {
            this.histogramNativeInitialSchema = histogramNativeInitialSchema;
            return this;
        }

        /**
         * See {@link MetricsProperties#getHistogramNativeMinZeroThreshold()}
         */
        public Builder histogramNativeMinZeroThreshold(Double histogramNativeMinZeroThreshold) {
            this.histogramNativeMinZeroThreshold = histogramNativeMinZeroThreshold;
            return this;
        }

        /**
         * See {@link MetricsProperties#getHistogramNativeMaxZeroThreshold()}
         */
        public Builder histogramNativeMaxZeroThreshold(Double histogramNativeMaxZeroThreshold) {
            this.histogramNativeMaxZeroThreshold = histogramNativeMaxZeroThreshold;
            return this;
        }

        /**
         * See {@link MetricsProperties#getHistogramNativeMaxNumberOfBuckets()}
         */
        public Builder histogramNativeMaxNumberOfBuckets(Integer histogramNativeMaxNumberOfBuckets) {
            this.histogramNativeMaxNumberOfBuckets = histogramNativeMaxNumberOfBuckets;
            return this;
        }

        /**
         * See {@link MetricsProperties#getHistogramNativeResetDurationSeconds()}
         */
        public Builder histogramNativeResetDurationSeconds(Long histogramNativeResetDurationSeconds) {
            this.histogramNativeResetDurationSeconds = histogramNativeResetDurationSeconds;
            return this;
        }

        /**
         * See {@link MetricsProperties#getSummaryQuantiles()}
         */
        public Builder summaryQuantiles(double... summaryQuantiles) {
            this.summaryQuantiles = Util.toList(summaryQuantiles);
            return this;
        }

        /**
         * See {@link MetricsProperties#getSummaryQuantileErrors()}
         */
        public Builder summaryQuantileErrors(double... summaryQuantileErrors) {
            this.summaryQuantileErrors = Util.toList(summaryQuantileErrors);
            return this;
        }

        /**
         * See {@link MetricsProperties#getSummaryMaxAgeSeconds()}
         */
        public Builder summaryMaxAgeSeconds(Long summaryMaxAgeSeconds) {
            this.summaryMaxAgeSeconds = summaryMaxAgeSeconds;
            return this;
        }

        /**
         * See {@link MetricsProperties#getSummaryNumberOfAgeBuckets()}
         */
        public Builder summaryNumberOfAgeBuckets(Integer summaryNumberOfAgeBuckets) {
            this.summaryNumberOfAgeBuckets = summaryNumberOfAgeBuckets;
            return this;
        }
    }
}
