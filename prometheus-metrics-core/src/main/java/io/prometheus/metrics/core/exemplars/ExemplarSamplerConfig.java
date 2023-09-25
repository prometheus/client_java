package io.prometheus.metrics.core.exemplars;

import io.prometheus.metrics.config.ExemplarsProperties;
import io.prometheus.metrics.config.PrometheusProperties;

import java.util.concurrent.TimeUnit;

public class ExemplarSamplerConfig {

    /**
     * See {@link ExemplarsProperties#getMinRetentionPeriodSeconds()}
     */
    public static final int DEFAULT_MIN_RETENTION_PERIOD_SECONDS = 7;

    /**
     * See {@link ExemplarsProperties#getMaxRetentionPeriodSeconds()}
     */
    public static final int DEFAULT_MAX_RETENTION_PERIOD_SECONDS = 70;

    /**
     * See {@link ExemplarsProperties#getSampleIntervalMilliseconds()}
     */
    private static final int DEFAULT_SAMPLE_INTERVAL_MILLISECONDS = 90;

    private final long minRetentionPeriodMillis;
    private final long maxRetentionPeriodMillis;
    private final long sampleIntervalMillis;
    private final double[] histogramClassicUpperBounds; // null unless it's a classic histogram
    private final int numberOfExemplars; // if histogramClassicUpperBounds != null, then numberOfExemplars == histogramClassicUpperBounds.length

    /**
     * Constructor for all metric types except classic histograms.
     *
     * @param properties        See {@link PrometheusProperties#getExemplarProperties()}.
     * @param numberOfExemplars Counters have 1 Exemplar, native histograms and summaries have 4 Exemplars by default.
     *                          For classic histogram use {@link #ExemplarSamplerConfig(ExemplarsProperties, double[])}.
     */
    public ExemplarSamplerConfig(ExemplarsProperties properties, int numberOfExemplars) {
        this(properties, numberOfExemplars, null);
    }

    /**
     * Constructor for classic histogram metrics.
     *
     * @param properties                  See {@link PrometheusProperties#getExemplarProperties()}.
     * @param histogramClassicUpperBounds the ExemplarSampler will provide one Exemplar per histogram bucket.
     *                                    Must be sorted, and must include the +Inf bucket.
     */
    public ExemplarSamplerConfig(ExemplarsProperties properties, double[] histogramClassicUpperBounds) {
        this(properties, histogramClassicUpperBounds.length, histogramClassicUpperBounds);
    }

    private ExemplarSamplerConfig(ExemplarsProperties properties, int numberOfExemplars, double[] histogramClassicUpperBounds) {
        this(
                TimeUnit.SECONDS.toMillis(getOrDefault(properties.getMinRetentionPeriodSeconds(), DEFAULT_MIN_RETENTION_PERIOD_SECONDS)),
                TimeUnit.SECONDS.toMillis(getOrDefault(properties.getMaxRetentionPeriodSeconds(), DEFAULT_MAX_RETENTION_PERIOD_SECONDS)),
                getOrDefault(properties.getSampleIntervalMilliseconds(), DEFAULT_SAMPLE_INTERVAL_MILLISECONDS),
                numberOfExemplars,
                histogramClassicUpperBounds);
    }

    ExemplarSamplerConfig(long minRetentionPeriodMillis, long maxRetentionPeriodMillis, long sampleIntervalMillis, int numberOfExemplars, double[] histogramClassicUpperBounds) {
        this.minRetentionPeriodMillis = minRetentionPeriodMillis;
        this.maxRetentionPeriodMillis = maxRetentionPeriodMillis;
        this.sampleIntervalMillis = sampleIntervalMillis;
        this.numberOfExemplars = numberOfExemplars;
        this.histogramClassicUpperBounds = histogramClassicUpperBounds;
        validate();
    }

    private void validate() {
        if (minRetentionPeriodMillis <= 0) {
            throw new IllegalArgumentException(minRetentionPeriodMillis + ": minRetentionPeriod must be > 0.");
        }
        if (maxRetentionPeriodMillis <= 0) {
            throw new IllegalArgumentException(maxRetentionPeriodMillis + ": maxRetentionPeriod must be > 0.");
        }
        if (histogramClassicUpperBounds != null) {
            if (histogramClassicUpperBounds.length == 0 || histogramClassicUpperBounds[histogramClassicUpperBounds.length - 1] != Double.POSITIVE_INFINITY) {
                throw new IllegalArgumentException("histogramClassicUpperBounds must contain the +Inf bucket.");
            }
            if (histogramClassicUpperBounds.length != numberOfExemplars) {
                throw new IllegalArgumentException("histogramClassicUpperBounds.length must be equal to numberOfExemplars.");
            }
            double bound = histogramClassicUpperBounds[0];
            for (int i = 1; i < histogramClassicUpperBounds.length; i++) {
                if (bound >= histogramClassicUpperBounds[i]) {
                    throw new IllegalArgumentException("histogramClassicUpperBounds must be sorted and must not contain duplicates.");
                }
            }
        }
        if (numberOfExemplars <= 0) {
            throw new IllegalArgumentException(numberOfExemplars + ": numberOfExemplars must be > 0.");
        }
    }

    private static <T> T getOrDefault(T result, T defaultValue) {
        return result != null ? result : defaultValue;
    }

    /**
     * May be {@code null}.
     */
    public double[] getHistogramClassicUpperBounds() {
        return histogramClassicUpperBounds;
    }

    /**
     * See {@link ExemplarsProperties#getMinRetentionPeriodSeconds()}
     */
    public long getMinRetentionPeriodMillis() {
        return minRetentionPeriodMillis;
    }

    /**
     * See {@link ExemplarsProperties#getMaxRetentionPeriodSeconds()}
     */
    public long getMaxRetentionPeriodMillis() {
        return maxRetentionPeriodMillis;
    }

    /**
     * See {@link ExemplarsProperties#getSampleIntervalMilliseconds()}
     */
    public long getSampleIntervalMillis() {
        return sampleIntervalMillis;
    }

    /**
     * Defaults: Counters have one Exemplar, native histograms and summaries have 4 Exemplars, classic histograms have one Exemplar per bucket.
     */
    public int getNumberOfExemplars() {
        return numberOfExemplars;
    }
}
