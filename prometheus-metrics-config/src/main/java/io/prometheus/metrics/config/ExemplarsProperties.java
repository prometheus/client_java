package io.prometheus.metrics.config;

import java.util.Map;
import javax.annotation.Nullable;

/** Properties starting with io.prometheus.exemplars */
public class ExemplarsProperties {

  private static final String PREFIX = "io.prometheus.exemplars";
  private static final String MIN_RETENTION_PERIOD_SECONDS = "minRetentionPeriodSeconds";
  private static final String MAX_RETENTION_PERIOD_SECONDS = "maxRetentionPeriodSeconds";
  private static final String SAMPLE_INTERVAL_MILLISECONDS = "sampleIntervalMilliseconds";

  @Nullable private final Integer minRetentionPeriodSeconds;
  @Nullable private final Integer maxRetentionPeriodSeconds;
  @Nullable private final Integer sampleIntervalMilliseconds;

  private ExemplarsProperties(
      @Nullable Integer minRetentionPeriodSeconds,
      @Nullable Integer maxRetentionPeriodSeconds,
      @Nullable Integer sampleIntervalMilliseconds) {
    this.minRetentionPeriodSeconds = minRetentionPeriodSeconds;
    this.maxRetentionPeriodSeconds = maxRetentionPeriodSeconds;
    this.sampleIntervalMilliseconds = sampleIntervalMilliseconds;
  }

  /**
   * Minimum time how long Exemplars are kept before they may be replaced by new Exemplars.
   *
   * <p>Default see {@code ExemplarSamplerConfig.DEFAULT_MIN_RETENTION_PERIOD_SECONDS}
   */
  @Nullable
  public Integer getMinRetentionPeriodSeconds() {
    return minRetentionPeriodSeconds;
  }

  /**
   * Maximum time how long Exemplars are kept before they are evicted.
   *
   * <p>Default see {@code ExemplarSamplerConfig.DEFAULT_MAX_RETENTION_PERIOD_SECONDS}
   */
  @Nullable
  public Integer getMaxRetentionPeriodSeconds() {
    return maxRetentionPeriodSeconds;
  }

  /**
   * Time between attempts to sample new Exemplars. This is a performance improvement for
   * high-frequency applications, because with the sample interval we make sure that the exemplar
   * sampler is not called for every single request.
   *
   * <p>Default see {@code ExemplarSamplerConfig.DEFAULT_SAMPLE_INTERVAL_MILLISECONDS}
   */
  @Nullable
  public Integer getSampleIntervalMilliseconds() {
    return sampleIntervalMilliseconds;
  }

  /**
   * Note that this will remove entries from {@code properties}. This is because we want to know if
   * there are unused properties remaining after all properties have been loaded.
   */
  static ExemplarsProperties load(Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    Integer minRetentionPeriodSeconds =
        Util.loadInteger(PREFIX + "." + MIN_RETENTION_PERIOD_SECONDS, properties);
    Integer maxRetentionPeriodSeconds =
        Util.loadInteger(PREFIX + "." + MAX_RETENTION_PERIOD_SECONDS, properties);
    Integer sampleIntervalMilliseconds =
        Util.loadInteger(PREFIX + "." + SAMPLE_INTERVAL_MILLISECONDS, properties);

    Util.assertValue(
        minRetentionPeriodSeconds,
        t -> t > 0,
        "Expecting value > 0.",
        PREFIX,
        MIN_RETENTION_PERIOD_SECONDS);
    Util.assertValue(
        maxRetentionPeriodSeconds,
        t -> t > 0,
        "Expecting value > 0.",
        PREFIX,
        MAX_RETENTION_PERIOD_SECONDS);
    Util.assertValue(
        sampleIntervalMilliseconds,
        t -> t > 0,
        "Expecting value > 0.",
        PREFIX,
        SAMPLE_INTERVAL_MILLISECONDS);

    if (minRetentionPeriodSeconds != null && maxRetentionPeriodSeconds != null) {
      if (minRetentionPeriodSeconds > maxRetentionPeriodSeconds) {
        throw new PrometheusPropertiesException(
            PREFIX
                + "."
                + MIN_RETENTION_PERIOD_SECONDS
                + " must not be greater than "
                + PREFIX
                + "."
                + MAX_RETENTION_PERIOD_SECONDS
                + ".");
      }
    }

    return new ExemplarsProperties(
        minRetentionPeriodSeconds, maxRetentionPeriodSeconds, sampleIntervalMilliseconds);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Integer minRetentionPeriodSeconds;
    @Nullable private Integer maxRetentionPeriodSeconds;
    @Nullable private Integer sampleIntervalMilliseconds;

    private Builder() {}

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
      return new ExemplarsProperties(
          minRetentionPeriodSeconds, maxRetentionPeriodSeconds, sampleIntervalMilliseconds);
    }
  }
}
