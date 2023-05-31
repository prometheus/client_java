package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.metrics
 */
public class MetricsConfig {

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
    private final Long summaryMaxAgeSeconds;
    private final Integer summaryNumberOfAgeBuckets;

    public MetricsConfig(
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
                summaryMaxAgeSeconds,
                summaryNumberOfAgeBuckets,
                null);
    }

    private MetricsConfig(
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

    private void validate(String prefix) throws PrometheusConfigException {
        Util.assertValue(histogramNativeInitialSchema, s -> s >= -4 && s <= 8, "Expecting number between -4 and +8.", prefix, HISTOGRAM_NATIVE_INITIAL_SCHEMA);
        Util.assertValue(histogramNativeMinZeroThreshold, t -> t >= 0, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD);
        Util.assertValue(histogramNativeMaxZeroThreshold, t -> t >= 0, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD);
        Util.assertValue(histogramNativeMaxNumberOfBuckets, n -> n >= 0, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS);
        Util.assertValue(histogramNativeResetDurationSeconds, t -> t >= 0, "Expecting value >= 0.", prefix, HISTOGRAM_NATIVE_RESET_DURATION_SECONDS);
        Util.assertValue(summaryMaxAgeSeconds, t -> t > 0, "Expecting value > 0", prefix, SUMMARY_MAX_AGE_SECONDS);
        Util.assertValue(summaryNumberOfAgeBuckets, t -> t > 0, "Expecting value > 0", prefix, SUMMARY_NUMBER_OF_AGE_BUCKETS);

        if (Boolean.TRUE.equals(histogramNativeOnly) && Boolean.TRUE.equals(histogramClassicOnly)) {
            throw new PrometheusConfigException(prefix + "." + HISTOGRAM_NATIVE_ONLY + " and " + prefix + "." + HISTOGRAM_CLASSIC_ONLY + " cannot both be true");
        }

        if (histogramNativeMinZeroThreshold != null && histogramNativeMaxZeroThreshold != null) {
            if (histogramNativeMinZeroThreshold > histogramNativeMaxZeroThreshold) {
                throw new PrometheusConfigException(prefix + "." + HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD + " cannot be greater than " + prefix + "." + HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD);
            }
        }

        if (summaryQuantiles != null) {
            for (double quantile : summaryQuantiles) {
                if (quantile < 0 || quantile > 1) {
                    throw new PrometheusConfigException(prefix + "." + SUMMARY_QUANTILES + ": Expecting 0.0 <= quantile <= 1.0");
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

    public Long getSummaryMaxAgeSeconds() {
        return summaryMaxAgeSeconds;
    }

    public Integer getSummaryNumberOfAgeBuckets() {
        return summaryNumberOfAgeBuckets;
    }

    static MetricsConfig load(String prefix, Map<Object, Object> properties) throws PrometheusConfigException {
        return new MetricsConfig(
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
                Util.loadLong(prefix + "." + SUMMARY_MAX_AGE_SECONDS, properties),
                Util.loadInteger(prefix + "." + SUMMARY_NUMBER_OF_AGE_BUCKETS, properties),
                prefix);
    }
}
