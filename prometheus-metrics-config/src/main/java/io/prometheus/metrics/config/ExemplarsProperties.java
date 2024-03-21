package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.exemplars
 */
public class ExemplarsProperties {

    private static final String MIN_RETENTION_PERIOD_SECONDS = "minRetentionPeriodSeconds";
    private static final String MAX_RETENTION_PERIOD_SECONDS = "maxRetentionPeriodSeconds";
    private static final String SAMPLE_INTERVAL_MILLISECONDS = "sampleIntervalMilliseconds";

    private final Integer minRetentionPeriodSeconds;
    private final Integer maxRetentionPeriodSeconds;
    private final Integer sampleIntervalMilliseconds;

    private ExemplarsProperties(
            Integer minRetentionPeriodSeconds,
            Integer maxRetentionPeriodSeconds,
            Integer sampleIntervalMilliseconds) {
        this.minRetentionPeriodSeconds = minRetentionPeriodSeconds;
        this.maxRetentionPeriodSeconds = maxRetentionPeriodSeconds;
        this.sampleIntervalMilliseconds = sampleIntervalMilliseconds;
    }

    /**
     * Minimum time how long Exemplars are kept before they may be replaced by new Exemplars.
     * <p>
     * Default see {@code ExemplarSamplerConfig.DEFAULT_MIN_RETENTION_PERIOD_SECONDS}
     */
    public Integer getMinRetentionPeriodSeconds() {
        return minRetentionPeriodSeconds;
    }

    /**
     * Maximum time how long Exemplars are kept before they are evicted.
     * <p>
     * Default see {@code ExemplarSamplerConfig.DEFAULT_MAX_RETENTION_PERIOD_SECONDS}
     */
    public Integer getMaxRetentionPeriodSeconds() {
        return maxRetentionPeriodSeconds;
    }

    /**
     * Time between attempts to sample new Exemplars. This is a performance improvement for high-frequency
     * applications, because with the sample interval we make sure that the exemplar sampler is not called
     * for every single request.
     * <p>
     * Default see {@code ExemplarSamplerConfig.DEFAULT_SAMPLE_INTERVAL_MILLISECONDS}
     */
    public Integer getSampleIntervalMilliseconds() {
        return sampleIntervalMilliseconds;
    }

    /**
     * Note that this will remove entries from {@code properties}.
     * This is because we want to know if there are unused properties remaining after all properties have been loaded.
     */
    static ExemplarsProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        Integer minRetentionPeriodSeconds = Util.loadInteger(prefix + "." + MIN_RETENTION_PERIOD_SECONDS, properties);
        Integer maxRetentionPeriodSeconds = Util.loadInteger(prefix + "." + MAX_RETENTION_PERIOD_SECONDS, properties);
        Integer sampleIntervalMilliseconds = Util.loadInteger(prefix + "." + SAMPLE_INTERVAL_MILLISECONDS, properties);

        Util.assertValue(minRetentionPeriodSeconds, t -> t > 0, "Expecting value > 0.", prefix, MIN_RETENTION_PERIOD_SECONDS);
        Util.assertValue(minRetentionPeriodSeconds, t -> t > 0, "Expecting value > 0.", prefix, MAX_RETENTION_PERIOD_SECONDS);
        Util.assertValue(sampleIntervalMilliseconds, t -> t > 0, "Expecting value > 0.", prefix, SAMPLE_INTERVAL_MILLISECONDS);

        if (minRetentionPeriodSeconds != null && maxRetentionPeriodSeconds != null) {
            if (minRetentionPeriodSeconds > maxRetentionPeriodSeconds) {
                throw new PrometheusPropertiesException(prefix + "." + MIN_RETENTION_PERIOD_SECONDS + " must not be greater than " + prefix + "." + MAX_RETENTION_PERIOD_SECONDS + ".");
            }
        }

        return new ExemplarsProperties(
                minRetentionPeriodSeconds,
                maxRetentionPeriodSeconds,
                sampleIntervalMilliseconds
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Integer minRetentionPeriodSeconds;
        private Integer maxRetentionPeriodSeconds;
        private Integer sampleIntervalMilliseconds;

        private Builder() {
        }

        public Builder minRetentionPeriodSeconds(int minRetentionPeriodSeconds) {
            this.minRetentionPeriodSeconds = minRetentionPeriodSeconds;
            return this;
        }

        public Builder maxRetentionPeriodSeconds(int maxRetentionPeriodSeconds) {
            this.maxRetentionPeriodSeconds = maxRetentionPeriodSeconds;
            return this;
        }

        public Builder sampleIntervalMilliseconds(int sampleIntervalMilliseconds) {
            this.sampleIntervalMilliseconds = sampleIntervalMilliseconds;
            return this;
        }

        public ExemplarsProperties build() {
            return new ExemplarsProperties(minRetentionPeriodSeconds, maxRetentionPeriodSeconds, sampleIntervalMilliseconds);
        }
    }
}
