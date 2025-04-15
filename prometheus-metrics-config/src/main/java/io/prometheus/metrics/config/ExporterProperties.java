package io.prometheus.metrics.config;

import java.util.Map;

/** Properties starting with io.prometheus.exporter */
public class ExporterProperties {

  private static final String INCLUDE_CREATED_TIMESTAMPS = "includeCreatedTimestamps";
  // milliseconds is the default - we only provide a boolean flag to avoid a breaking change
  private static final String PROMETHEUS_TIMESTAMPS_IN_MS = "prometheusTimestampsInMs";
  private static final String EXEMPLARS_ON_ALL_METRIC_TYPES = "exemplarsOnAllMetricTypes";
  private static final String PREFIX = "io.prometheus.exporter";

  private final Boolean includeCreatedTimestamps;
  private final Boolean prometheusTimestampsInMs;
  private final Boolean exemplarsOnAllMetricTypes;

  private ExporterProperties(
      Boolean includeCreatedTimestamps,
      Boolean prometheusTimestampsInMs,
      Boolean exemplarsOnAllMetricTypes) {
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
   * Note that this will remove entries from {@code properties}. This is because we want to know if
   * there are unused properties remaining after all properties have been loaded.
   */
  static ExporterProperties load(Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    Boolean includeCreatedTimestamps =
        Util.loadBoolean(PREFIX + "." + INCLUDE_CREATED_TIMESTAMPS, properties);
    Boolean timestampsInMs = Util.loadBoolean(PREFIX + "." + PROMETHEUS_TIMESTAMPS_IN_MS, properties);
    Boolean exemplarsOnAllMetricTypes =
        Util.loadBoolean(PREFIX + "." + EXEMPLARS_ON_ALL_METRIC_TYPES, properties);
    return new ExporterProperties(
        includeCreatedTimestamps, timestampsInMs, exemplarsOnAllMetricTypes);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Boolean includeCreatedTimestamps;
    private Boolean exemplarsOnAllMetricTypes;
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
