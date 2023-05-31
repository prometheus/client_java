package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.metrics
 */
public class MetricsConfig {

    private static final String EXEMPLARS_ENABLED = "exemplarsEnabled";
    private static final String NATIVE_HISTOGRAM_ONLY = "nativeHistogramOnly";
    private static final String CLASSIC_HISTOGRAM_ONLY = "classicHistogramOnly";
    private static final String CLASSIC_HISTOGRAM_UPPER_BOUNDS = "classicHistogramUpperBounds";
    private static final String NATIVE_HISTOGRAM_INITIAL_SCHEMA = "nativeHistogramInitialSchema";
    private static final String NATIVE_HISTOGRAM_MIN_ZERO_THRESHOLD = "nativeHistogramMinZeroThreshold";
    private static final String NATIVE_HISTOGRAM_MAX_ZERO_THRESHOLD = "nativeHistogramMaxZeroThreshold";
    private static final String NATIVE_HISTOGRAM_MAX_NUMBER_OF_BUCKETS = "nativeHistogramMaxNumberOfBuckets"; // 0 means unlimited number of buckets
    private static final String NATIVE_HISTOGRAM_RESET_DURATION_SECONDS = "nativeHistogramResetDurationSeconds"; // 0 means no reset
    private static final String SUMMARY_QUANTILES = "summaryQuantiles";
    private static final String SUMMARY_MAX_AGE_SECONDS = "summaryMaxAgeSeconds";
    private static final String SUMMARY_NUMBER_OF_AGE_BUCKETS = "summaryNumberOfAgeBuckets";

    private final Boolean exemplarsEnabled;
    private final Boolean nativeHistogramOnly;
    private final Boolean classicHistogramOnly;
    private final double[] classicHistogramUpperBounds;
    private final Integer nativeHistogramInitialSchema;
    private final Double nativeHistogramMinZeroThreshold;
    private final Double nativeHistogramMaxZeroThreshold;
    private final Integer nativeHistogramMaxNumberOfBuckets;
    private final Long nativeHistogramResetDurationSeconds;
    private final double[] summaryQuantiles;
    private final Long summaryMaxAgeSeconds;
    private final Integer summaryNumberOfAgeBuckets;

    public MetricsConfig(
            Boolean exemplarsEnabled,
            Boolean nativeHistogramOnly,
            Boolean classicHistogramOnly,
            double[] classicHistogramUpperBounds,
            Integer nativeHistogramInitialSchema,
            Double nativeHistogramMinZeroThreshold,
            Double nativeHistogramMaxZeroThreshold,
            Integer nativeHistogramMaxNumberOfBuckets,
            Long nativeHistogramResetDurationSeconds,
            double[] summaryQuantiles,
            Long summaryMaxAgeSeconds,
            Integer summaryNumberOfAgeBuckets) {
        this(exemplarsEnabled,
                nativeHistogramOnly,
                classicHistogramOnly,
                classicHistogramUpperBounds,
                nativeHistogramInitialSchema,
                nativeHistogramMinZeroThreshold,
                nativeHistogramMaxZeroThreshold,
                nativeHistogramMaxNumberOfBuckets,
                nativeHistogramResetDurationSeconds,
                summaryQuantiles,
                summaryMaxAgeSeconds,
                summaryNumberOfAgeBuckets,
                null);
    }

    private MetricsConfig(
            Boolean exemplarsEnabled,
            Boolean nativeHistogramOnly,
            Boolean classicHistogramOnly,
            double[] classicHistogramUpperBounds,
            Integer nativeHistogramInitialSchema,
            Double nativeHistogramMinZeroThreshold,
            Double nativeHistogramMaxZeroThreshold,
            Integer nativeHistogramMaxNumberOfBuckets,
            Long nativeHistogramResetDurationSeconds,
            double[] summaryQuantiles,
            Long summaryMaxAgeSeconds,
            Integer summaryNumberOfAgeBuckets,
            String configPropertyPrefix) {
        this.exemplarsEnabled = exemplarsEnabled;
        this.nativeHistogramOnly = isNativeHistogramOnly(classicHistogramOnly, nativeHistogramOnly);
        this.classicHistogramOnly = isClassicHistogramOnly(classicHistogramOnly, nativeHistogramOnly);
        this.classicHistogramUpperBounds = classicHistogramUpperBounds;
        this.nativeHistogramInitialSchema = nativeHistogramInitialSchema;
        this.nativeHistogramMinZeroThreshold = nativeHistogramMinZeroThreshold;
        this.nativeHistogramMaxZeroThreshold = nativeHistogramMaxZeroThreshold;
        this.nativeHistogramMaxNumberOfBuckets = nativeHistogramMaxNumberOfBuckets;
        this.nativeHistogramResetDurationSeconds = nativeHistogramResetDurationSeconds;
        this.summaryQuantiles = summaryQuantiles;
        this.summaryMaxAgeSeconds = summaryMaxAgeSeconds;
        this.summaryNumberOfAgeBuckets = summaryNumberOfAgeBuckets;
        validate(configPropertyPrefix);
    }

    private Boolean isClassicHistogramOnly(Boolean classicHistogramOnly, Boolean nativeHistogramOnly) {
        if (classicHistogramOnly == null && nativeHistogramOnly == null) {
            return null;
        }
        if (classicHistogramOnly != null) {
            return classicHistogramOnly;
        }
        return !nativeHistogramOnly;
    }

    private Boolean isNativeHistogramOnly(Boolean classicHistogramOnly, Boolean nativeHistogramOnly) {
        if (classicHistogramOnly == null && nativeHistogramOnly == null) {
            return null;
        }
        if (nativeHistogramOnly != null) {
            return nativeHistogramOnly;
        }
        return !classicHistogramOnly;
    }

    private void validate(String prefix) throws PrometheusConfigException {
        Util.assertValue(nativeHistogramInitialSchema, s -> s >= -4 && s <= 8, "Expecting number between -4 and +8.", prefix, NATIVE_HISTOGRAM_INITIAL_SCHEMA);
        Util.assertValue(nativeHistogramMinZeroThreshold, t -> t >= 0, "Expecting value >= 0.", prefix, NATIVE_HISTOGRAM_MIN_ZERO_THRESHOLD);
        Util.assertValue(nativeHistogramMaxZeroThreshold, t -> t >= 0, "Expecting value >= 0.", prefix, NATIVE_HISTOGRAM_MAX_ZERO_THRESHOLD);
        Util.assertValue(nativeHistogramMaxNumberOfBuckets, n -> n >= 0, "Expecting value >= 0.", prefix, NATIVE_HISTOGRAM_MAX_NUMBER_OF_BUCKETS);
        Util.assertValue(nativeHistogramResetDurationSeconds, t -> t >= 0, "Expecting value >= 0.", prefix, NATIVE_HISTOGRAM_RESET_DURATION_SECONDS);
        Util.assertValue(summaryMaxAgeSeconds, t -> t > 0, "Expecting value > 0", prefix, SUMMARY_MAX_AGE_SECONDS);
        Util.assertValue(summaryNumberOfAgeBuckets, t -> t > 0, "Expecting value > 0", prefix, SUMMARY_NUMBER_OF_AGE_BUCKETS);

        if (Boolean.TRUE.equals(nativeHistogramOnly) && Boolean.TRUE.equals(classicHistogramOnly)) {
            throw new PrometheusConfigException(prefix + "." + NATIVE_HISTOGRAM_ONLY + " and " + prefix + "." + CLASSIC_HISTOGRAM_ONLY + " cannot both be true");
        }

        if (nativeHistogramMinZeroThreshold != null && nativeHistogramMaxZeroThreshold != null) {
            if (nativeHistogramMinZeroThreshold > nativeHistogramMaxZeroThreshold) {
                throw new PrometheusConfigException(prefix + "." + NATIVE_HISTOGRAM_MIN_ZERO_THRESHOLD + " cannot be greater than " + prefix + "." + NATIVE_HISTOGRAM_MAX_ZERO_THRESHOLD);
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

    public Boolean getNativeHistogramOnly() {
        return nativeHistogramOnly;
    }

    public Boolean getClassicHistogramOnly() {
        return classicHistogramOnly;
    }

    public double[] getClassicHistogramUpperBounds() {
        return classicHistogramUpperBounds;
    }

    public Integer getNativeHistogramInitialSchema() {
        return nativeHistogramInitialSchema;
    }

    public Double getNativeHistogramMinZeroThreshold() {
        return nativeHistogramMinZeroThreshold;
    }

    public Double getNativeHistogramMaxZeroThreshold() {
        return nativeHistogramMaxZeroThreshold;
    }

    public Integer getNativeHistogramMaxNumberOfBuckets() {
        return nativeHistogramMaxNumberOfBuckets;
    }

    public Long getNativeHistogramResetDurationSeconds() {
        return nativeHistogramResetDurationSeconds;
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
                Util.loadBoolean(prefix + "." + NATIVE_HISTOGRAM_ONLY, properties),
                Util.loadBoolean(prefix + "." + CLASSIC_HISTOGRAM_ONLY, properties),
                Util.loadDoubleArray(prefix + "." + CLASSIC_HISTOGRAM_UPPER_BOUNDS, properties),
                Util.loadInteger(prefix + "." + NATIVE_HISTOGRAM_INITIAL_SCHEMA, properties),
                Util.loadDouble(prefix + "." + NATIVE_HISTOGRAM_MIN_ZERO_THRESHOLD, properties),
                Util.loadDouble(prefix + "." + NATIVE_HISTOGRAM_MAX_ZERO_THRESHOLD, properties),
                Util.loadInteger(prefix + "." + NATIVE_HISTOGRAM_MAX_NUMBER_OF_BUCKETS, properties),
                Util.loadLong(prefix + "." + NATIVE_HISTOGRAM_RESET_DURATION_SECONDS, properties),
                Util.loadDoubleArray(prefix + "." + SUMMARY_QUANTILES, properties),
                Util.loadLong(prefix + "." + SUMMARY_MAX_AGE_SECONDS, properties),
                Util.loadInteger(prefix + "." + SUMMARY_NUMBER_OF_AGE_BUCKETS, properties),
                prefix);
    }
}
