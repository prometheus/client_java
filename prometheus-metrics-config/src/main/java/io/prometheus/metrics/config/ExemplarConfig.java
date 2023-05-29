package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.exemplars
 */
public class ExemplarConfig {

    private static final String MIN_RETENTION_PERIOD_SECONDS = "minRetentionPeriodSeconds";
    private static final String MAX_RETENTION_PERIOD_SECONDS = "maxRetentionPeriodSeconds";
    private static final String SAMPLE_INTERVAL_MILLISECONDS = "sampleIntervalMilliseconds";
    private static final String NUMBER_OF_EXEMPLARS = "numberOfExemplars";

    private final Integer minRetentionPeriodSeconds;
    private final Integer maxRetentionPeriodSeconds;
    private final Integer sampleIntervalMilliseconds;
    private final Integer numberOfExemplars;

    public ExemplarConfig(
            Integer minRetentionPeriodSeconds,
            Integer maxRetentionPeriodSeconds,
            Integer sampleIntervalMilliseconds,
            Integer numberOfExemplars) {
        this.minRetentionPeriodSeconds = minRetentionPeriodSeconds;
        this.maxRetentionPeriodSeconds = maxRetentionPeriodSeconds;
        this.sampleIntervalMilliseconds = sampleIntervalMilliseconds;
        this.numberOfExemplars = numberOfExemplars;
    }

    public Integer getMinRetentionPeriodSeconds() {
        return minRetentionPeriodSeconds;
    }

    public Integer getMaxRetentionPeriodSeconds() {
        return maxRetentionPeriodSeconds;
    }

    public Integer getSampleIntervalMilliseconds() {
        return sampleIntervalMilliseconds;
    }

    public Integer getNumberOfExemplars() {
        return numberOfExemplars;
    }

    static ExemplarConfig load(String prefix, Map<Object, Object> properties) throws PrometheusConfigException {
        Integer minRetentionPeriodSeconds = Util.loadInteger(prefix + "." + MIN_RETENTION_PERIOD_SECONDS, properties);
        Integer maxRetentionPeriodSeconds = Util.loadInteger(prefix + "." + MAX_RETENTION_PERIOD_SECONDS, properties);
        Integer sampleIntervalMilliseconds = Util.loadInteger(prefix + "." + SAMPLE_INTERVAL_MILLISECONDS, properties);
        Integer numberOfExemplars = Util.loadInteger(prefix + "." + NUMBER_OF_EXEMPLARS, properties);

        Util.assertValue(minRetentionPeriodSeconds, t -> t > 0, "Expecting value > 0.", prefix, MIN_RETENTION_PERIOD_SECONDS);
        Util.assertValue(minRetentionPeriodSeconds, t -> t > 0, "Expecting value > 0.", prefix, MAX_RETENTION_PERIOD_SECONDS);
        Util.assertValue(sampleIntervalMilliseconds, t -> t > 0, "Expecting value > 0.", prefix, SAMPLE_INTERVAL_MILLISECONDS);
        Util.assertValue(numberOfExemplars, n -> n > 0, "Expecting value > 0.", prefix, NUMBER_OF_EXEMPLARS);

        if (minRetentionPeriodSeconds != null && maxRetentionPeriodSeconds != null) {
            if (minRetentionPeriodSeconds > maxRetentionPeriodSeconds) {
                throw new PrometheusConfigException(prefix + "." + MIN_RETENTION_PERIOD_SECONDS + " must not be greater than " + prefix + "." + MAX_RETENTION_PERIOD_SECONDS + ".");
            }
        }

        return new ExemplarConfig(
                minRetentionPeriodSeconds,
                maxRetentionPeriodSeconds,
                sampleIntervalMilliseconds,
                numberOfExemplars
        );
    }
}
