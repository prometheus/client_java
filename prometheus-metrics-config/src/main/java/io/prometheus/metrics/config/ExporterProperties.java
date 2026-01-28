package io.prometheus.metrics.config;

import javax.annotation.Nullable;

/** Properties starting with io.prometheus.exporter */
public class ExporterProperties {

  private static final String INCLUDE_CREATED_TIMESTAMPS = "include_created_timestamps";
  // milliseconds is the default - we only provide a boolean flag to avoid a breaking change
  private static final String PROMETHEUS_TIMESTAMPS_IN_MS = "prometheus_timestamps_in_ms";
  private static final String EXEMPLARS_ON_ALL_METRIC_TYPES = "exemplars_on_all_metric_types";
  private static final String PREFIX = "io.prometheus.exporter";

  @Nullable private final Boolean includeCreatedTimestamps;
  @Nullable private final Boolean prometheusTimestampsInMs;
  @Nullable private final Boolean exemplarsOnAllMetricTypes;

  private ExporterProperties(
      @Nullable Boolean includeCreatedTimestamps,
      @Nullable Boolean prometheusTimestampsInMs,
      @Nullable Boolean exemplarsOnAllMetricTypes) {
    this.includeCreatedTimestamps = includeCreatedTimestamps;
    this.prometheusTimestampsInMs = prometheusTimestampsInMs;
    this.exemplarsOnAllMetricTypes = exemplarsOnAllMetricTypes;
  }

  /** Include the {@code _created} timestamps in text format? Default is {@code false}. */
  public boolean getIncludeCreatedTimestamps() {
    return includeCreatedTimestamps != null && includeCreatedTimestamps;
  }

  /** Use milliseconds for timestamps in prometheus text format? Default is {@code false}. */
  public boolean getPrometheusTimestampsInMs() {
    return prometheusTimestampsInMs != null && prometheusTimestampsInMs;
  }

  /**
   * Allow Exemplars on all metric types in OpenMetrics format? Default is {@code false}, which
   * means Exemplars will only be added for Counters and Histogram buckets.
   */
  public boolean getExemplarsOnAllMetricTypes() {
    return exemplarsOnAllMetricTypes != null && exemplarsOnAllMetricTypes;
  }

  /**
   * Note that this will remove entries from {@code propertySource}. This is because we want to know
   * if there are unused properties remaining after all properties have been loaded.
   */
  static ExporterProperties load(PropertySource propertySource)
      throws PrometheusPropertiesException {
    Boolean includeCreatedTimestamps =
        Util.loadBoolean(PREFIX, INCLUDE_CREATED_TIMESTAMPS, propertySource);
    Boolean timestampsInMs = Util.loadBoolean(PREFIX, PROMETHEUS_TIMESTAMPS_IN_MS, propertySource);
    Boolean exemplarsOnAllMetricTypes =
        Util.loadBoolean(PREFIX, EXEMPLARS_ON_ALL_METRIC_TYPES, propertySource);
    return new ExporterProperties(
        includeCreatedTimestamps, timestampsInMs, exemplarsOnAllMetricTypes);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Boolean includeCreatedTimestamps;
    @Nullable private Boolean exemplarsOnAllMetricTypes;
    boolean prometheusTimestampsInMs;

    private Builder() {}

    /** See {@link #getIncludeCreatedTimestamps()} */
    public Builder includeCreatedTimestamps(boolean includeCreatedTimestamps) {
      this.includeCreatedTimestamps = includeCreatedTimestamps;
      return this;
    }

    /** See {@link #getExemplarsOnAllMetricTypes()}. */
    public Builder exemplarsOnAllMetricTypes(boolean exemplarsOnAllMetricTypes) {
      this.exemplarsOnAllMetricTypes = exemplarsOnAllMetricTypes;
      return this;
    }

    /** See {@link #getPrometheusTimestampsInMs()}. */
    public Builder prometheusTimestampsInMs(boolean prometheusTimestampsInMs) {
      this.prometheusTimestampsInMs = prometheusTimestampsInMs;
      return this;
    }

    public ExporterProperties build() {
      return new ExporterProperties(
          includeCreatedTimestamps, prometheusTimestampsInMs, exemplarsOnAllMetricTypes);
    }
  }
}
