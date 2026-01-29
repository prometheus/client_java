package io.prometheus.metrics.config;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/** Properties starting with io.prometheus.metrics */
public class MetricsProperties {

  private static final String EXEMPLARS_ENABLED = "exemplars_enabled";
  private static final String HISTOGRAM_NATIVE_ONLY = "histogram_native_only";
  private static final String HISTOGRAM_CLASSIC_ONLY = "histogram_classic_only";
  private static final String HISTOGRAM_CLASSIC_UPPER_BOUNDS = "histogram_classic_upper_bounds";
  private static final String HISTOGRAM_NATIVE_INITIAL_SCHEMA = "histogram_native_initial_schema";
  private static final String HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD =
      "histogram_native_min_zero_threshold";
  private static final String HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD =
      "histogram_native_max_zero_threshold";
  private static final String HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS =
      "histogram_native_max_number_of_buckets"; // 0 means unlimited number of buckets
  private static final String HISTOGRAM_NATIVE_RESET_DURATION_SECONDS =
      "histogram_native_reset_duration_seconds"; // 0 means no reset
  private static final String SUMMARY_QUANTILES = "summary_quantiles";
  private static final String SUMMARY_QUANTILE_ERRORS = "summary_quantile_errors";
  private static final String SUMMARY_MAX_AGE_SECONDS = "summary_max_age_seconds";
  private static final String SUMMARY_NUMBER_OF_AGE_BUCKETS = "summary_number_of_age_buckets";

  /**
   * All known property suffixes that can be configured for metrics.
   *
   * <p>This list is used to parse metric-specific configuration keys from environment variables.
   */
  static final String[] PROPERTY_SUFFIXES = {
    EXEMPLARS_ENABLED,
    HISTOGRAM_NATIVE_ONLY,
    HISTOGRAM_CLASSIC_ONLY,
    HISTOGRAM_CLASSIC_UPPER_BOUNDS,
    HISTOGRAM_NATIVE_INITIAL_SCHEMA,
    HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD,
    HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD,
    HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS,
    HISTOGRAM_NATIVE_RESET_DURATION_SECONDS,
    SUMMARY_QUANTILES,
    SUMMARY_QUANTILE_ERRORS,
    SUMMARY_MAX_AGE_SECONDS,
    SUMMARY_NUMBER_OF_AGE_BUCKETS
  };
  private static final String USE_OTEL_METRICS = "useOtelMetrics";

  @Nullable private final Boolean exemplarsEnabled;
  @Nullable private final Boolean histogramNativeOnly;
  @Nullable private final Boolean histogramClassicOnly;
  @Nullable private final List<Double> histogramClassicUpperBounds;
  @Nullable private final Integer histogramNativeInitialSchema;
  @Nullable private final Double histogramNativeMinZeroThreshold;
  @Nullable private final Double histogramNativeMaxZeroThreshold;
  @Nullable private final Integer histogramNativeMaxNumberOfBuckets;
  @Nullable private final Long histogramNativeResetDurationSeconds;
  @Nullable private final List<Double> summaryQuantiles;
  @Nullable private final List<Double> summaryQuantileErrors;
  @Nullable private final Long summaryMaxAgeSeconds;
  @Nullable private final Integer summaryNumberOfAgeBuckets;
  @Nullable private final Boolean useOtelMetrics;

  public MetricsProperties(
      @Nullable Boolean exemplarsEnabled,
      @Nullable Boolean histogramNativeOnly,
      @Nullable Boolean histogramClassicOnly,
      @Nullable List<Double> histogramClassicUpperBounds,
      @Nullable Integer histogramNativeInitialSchema,
      @Nullable Double histogramNativeMinZeroThreshold,
      @Nullable Double histogramNativeMaxZeroThreshold,
      @Nullable Integer histogramNativeMaxNumberOfBuckets,
      @Nullable Long histogramNativeResetDurationSeconds,
      @Nullable List<Double> summaryQuantiles,
      @Nullable List<Double> summaryQuantileErrors,
      @Nullable Long summaryMaxAgeSeconds,
      @Nullable Integer summaryNumberOfAgeBuckets,
      @Nullable Boolean useOtelMetrics) {
    this(
        exemplarsEnabled,
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
        useOtelMetrics,
        "");
  }

  private MetricsProperties(
      @Nullable Boolean exemplarsEnabled,
      @Nullable Boolean histogramNativeOnly,
      @Nullable Boolean histogramClassicOnly,
      @Nullable List<Double> histogramClassicUpperBounds,
      @Nullable Integer histogramNativeInitialSchema,
      @Nullable Double histogramNativeMinZeroThreshold,
      @Nullable Double histogramNativeMaxZeroThreshold,
      @Nullable Integer histogramNativeMaxNumberOfBuckets,
      @Nullable Long histogramNativeResetDurationSeconds,
      @Nullable List<Double> summaryQuantiles,
      @Nullable List<Double> summaryQuantileErrors,
      @Nullable Long summaryMaxAgeSeconds,
      @Nullable Integer summaryNumberOfAgeBuckets,
      @Nullable Boolean useOtelMetrics,
      String configPropertyPrefix) {
    this.exemplarsEnabled = exemplarsEnabled;
    this.histogramNativeOnly = isHistogramNativeOnly(histogramClassicOnly, histogramNativeOnly);
    this.histogramClassicOnly = isHistogramClassicOnly(histogramClassicOnly, histogramNativeOnly);
    this.histogramClassicUpperBounds =
        histogramClassicUpperBounds == null
            ? null
            : unmodifiableList(new ArrayList<>(histogramClassicUpperBounds));
    this.histogramNativeInitialSchema = histogramNativeInitialSchema;
    this.histogramNativeMinZeroThreshold = histogramNativeMinZeroThreshold;
    this.histogramNativeMaxZeroThreshold = histogramNativeMaxZeroThreshold;
    this.histogramNativeMaxNumberOfBuckets = histogramNativeMaxNumberOfBuckets;
    this.histogramNativeResetDurationSeconds = histogramNativeResetDurationSeconds;
    this.summaryQuantiles =
        summaryQuantiles == null ? null : unmodifiableList(new ArrayList<>(summaryQuantiles));
    this.summaryQuantileErrors =
        summaryQuantileErrors == null
            ? null
            : unmodifiableList(new ArrayList<>(summaryQuantileErrors));
    this.summaryMaxAgeSeconds = summaryMaxAgeSeconds;
    this.summaryNumberOfAgeBuckets = summaryNumberOfAgeBuckets;
    this.useOtelMetrics = useOtelMetrics;
    validate(configPropertyPrefix);
  }

  @Nullable
  private Boolean isHistogramClassicOnly(
      @Nullable Boolean histogramClassicOnly, @Nullable Boolean histogramNativeOnly) {
    if (histogramClassicOnly != null) {
      return histogramClassicOnly;
    }
    if (histogramNativeOnly != null) {
      return !histogramNativeOnly;
    }
    return null;
  }

  @Nullable
  private Boolean isHistogramNativeOnly(
      @Nullable Boolean histogramClassicOnly, @Nullable Boolean histogramNativeOnly) {
    if (histogramNativeOnly != null) {
      return histogramNativeOnly;
    }
    if (histogramClassicOnly != null) {
      return !histogramClassicOnly;
    }
    return null;
  }

  private void validate(String prefix) throws PrometheusPropertiesException {
    Util.assertValue(
        histogramNativeInitialSchema,
        s -> s >= -4 && s <= 8,
        "Expecting number between -4 and +8.",
        prefix,
        HISTOGRAM_NATIVE_INITIAL_SCHEMA);
    Util.assertValue(
        histogramNativeMinZeroThreshold,
        t -> t >= 0,
        "Expecting value >= 0.",
        prefix,
        HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD);
    Util.assertValue(
        histogramNativeMaxZeroThreshold,
        t -> t >= 0,
        "Expecting value >= 0.",
        prefix,
        HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD);
    Util.assertValue(
        histogramNativeMaxNumberOfBuckets,
        n -> n >= 0,
        "Expecting value >= 0.",
        prefix,
        HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS);
    Util.assertValue(
        histogramNativeResetDurationSeconds,
        t -> t >= 0,
        "Expecting value >= 0.",
        prefix,
        HISTOGRAM_NATIVE_RESET_DURATION_SECONDS);
    Util.assertValue(
        summaryMaxAgeSeconds, t -> t > 0, "Expecting value > 0.", prefix, SUMMARY_MAX_AGE_SECONDS);
    Util.assertValue(
        summaryNumberOfAgeBuckets,
        t -> t > 0,
        "Expecting value > 0.",
        prefix,
        SUMMARY_NUMBER_OF_AGE_BUCKETS);

    if (Boolean.TRUE.equals(histogramNativeOnly) && Boolean.TRUE.equals(histogramClassicOnly)) {
      throw new PrometheusPropertiesException(
          prefix
              + "."
              + HISTOGRAM_NATIVE_ONLY
              + " and "
              + prefix
              + "."
              + HISTOGRAM_CLASSIC_ONLY
              + " cannot both be true");
    }

    if (histogramNativeMinZeroThreshold != null && histogramNativeMaxZeroThreshold != null) {
      if (histogramNativeMinZeroThreshold > histogramNativeMaxZeroThreshold) {
        throw new PrometheusPropertiesException(
            prefix
                + "."
                + HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD
                + " cannot be greater than "
                + prefix
                + "."
                + HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD);
      }
    }

    if (summaryQuantiles != null) {
      for (double quantile : summaryQuantiles) {
        if (quantile < 0 || quantile > 1) {
          throw new PrometheusPropertiesException(
              prefix
                  + "."
                  + SUMMARY_QUANTILES
                  + ": Expecting 0.0 <= quantile <= 1.0. Found: "
                  + quantile);
        }
      }
    }

    if (summaryQuantileErrors != null) {
      if (summaryQuantiles == null) {
        throw new PrometheusPropertiesException(
            prefix
                + "."
                + SUMMARY_QUANTILE_ERRORS
                + ": Can't configure "
                + SUMMARY_QUANTILE_ERRORS
                + " without configuring "
                + SUMMARY_QUANTILES);
      }
      if (summaryQuantileErrors.size() != summaryQuantiles.size()) {
        String fullKey =
            prefix.isEmpty() ? SUMMARY_QUANTILE_ERRORS : prefix + "." + SUMMARY_QUANTILE_ERRORS;
        throw new PrometheusPropertiesException(
            fullKey + ": must have the same length as " + SUMMARY_QUANTILES);
      }
      for (double error : summaryQuantileErrors) {
        if (error < 0 || error > 1) {
          String fullKey =
              prefix.isEmpty() ? SUMMARY_QUANTILE_ERRORS : prefix + "." + SUMMARY_QUANTILE_ERRORS;
          throw new PrometheusPropertiesException(fullKey + ": Expecting 0.0 <= error <= 1.0");
        }
      }
    }
  }

  /**
   * This is the only configuration property that can be applied to all metric types. You can use it
   * to turn Exemplar support off. Default is {@code true}.
   */
  @Nullable
  public Boolean getExemplarsEnabled() {
    return exemplarsEnabled;
  }

  /** See {@code Histogram.Builder.nativeOnly()} */
  @Nullable
  public Boolean getHistogramNativeOnly() {
    return histogramNativeOnly;
  }

  /** See {@code Histogram.Builder.classicOnly()} */
  @Nullable
  public Boolean getHistogramClassicOnly() {
    return histogramClassicOnly;
  }

  /** See {@code Histogram.Builder.classicUpperBounds()} */
  @Nullable
  public List<Double> getHistogramClassicUpperBounds() {
    return histogramClassicUpperBounds;
  }

  /** See {@code Histogram.Builder.nativeInitialSchema()} */
  @Nullable
  public Integer getHistogramNativeInitialSchema() {
    return histogramNativeInitialSchema;
  }

  /** See {@code Histogram.Builder.nativeMinZeroThreshold()} */
  @Nullable
  public Double getHistogramNativeMinZeroThreshold() {
    return histogramNativeMinZeroThreshold;
  }

  /** See {@code Histogram.Builder.nativeMaxZeroThreshold()} */
  @Nullable
  public Double getHistogramNativeMaxZeroThreshold() {
    return histogramNativeMaxZeroThreshold;
  }

  /** See {@code Histogram.Builder.nativeMaxNumberOfBuckets()} */
  @Nullable
  public Integer getHistogramNativeMaxNumberOfBuckets() {
    return histogramNativeMaxNumberOfBuckets;
  }

  /** See {@code Histogram.Builder.nativeResetDuration()} */
  @Nullable
  public Long getHistogramNativeResetDurationSeconds() {
    return histogramNativeResetDurationSeconds;
  }

  /** See {@code Summary.Builder.quantile()} */
  @Nullable
  public List<Double> getSummaryQuantiles() {
    return summaryQuantiles;
  }

  /**
   * See {@code Summary.Builder.quantile()}
   *
   * <p>Returns {@code null} only if {@link #getSummaryQuantiles()} is also {@code null}. Returns an
   * empty list if {@link #getSummaryQuantiles()} are specified without specifying errors. If the
   * list is not empty, it has the same size as {@link #getSummaryQuantiles()}.
   */
  @Nullable
  public List<Double> getSummaryQuantileErrors() {
    if (summaryQuantiles != null) {
      if (summaryQuantileErrors == null) {
        return Collections.emptyList();
      }
    }
    return summaryQuantileErrors;
  }

  /** See {@code Summary.Builder.maxAgeSeconds()} */
  @Nullable
  public Long getSummaryMaxAgeSeconds() {
    return summaryMaxAgeSeconds;
  }

  /** See {@code Summary.Builder.numberOfAgeBuckets()} */
  @Nullable
  public Integer getSummaryNumberOfAgeBuckets() {
    return summaryNumberOfAgeBuckets;
  }

  /** See {@code Summary.Builder.useOtelMetrics()} */
  @Nullable
  public Boolean useOtelMetrics() {
    return useOtelMetrics;
  }

  /**
   * Note that this will remove entries from {@code propertySource}. This is because we want to know
   * if there are unused properties remaining after all properties have been loaded.
   */
  static MetricsProperties load(String prefix, PropertySource propertySource)
      throws PrometheusPropertiesException {
    return new MetricsProperties(
        Util.loadBoolean(prefix, EXEMPLARS_ENABLED, propertySource),
        Util.loadBoolean(prefix, HISTOGRAM_NATIVE_ONLY, propertySource),
        Util.loadBoolean(prefix, HISTOGRAM_CLASSIC_ONLY, propertySource),
        Util.loadDoubleList(prefix, HISTOGRAM_CLASSIC_UPPER_BOUNDS, propertySource),
        Util.loadInteger(prefix, HISTOGRAM_NATIVE_INITIAL_SCHEMA, propertySource),
        Util.loadDouble(prefix, HISTOGRAM_NATIVE_MIN_ZERO_THRESHOLD, propertySource),
        Util.loadDouble(prefix, HISTOGRAM_NATIVE_MAX_ZERO_THRESHOLD, propertySource),
        Util.loadInteger(prefix, HISTOGRAM_NATIVE_MAX_NUMBER_OF_BUCKETS, propertySource),
        Util.loadLong(prefix, HISTOGRAM_NATIVE_RESET_DURATION_SECONDS, propertySource),
        Util.loadDoubleList(prefix, SUMMARY_QUANTILES, propertySource),
        Util.loadDoubleList(prefix, SUMMARY_QUANTILE_ERRORS, propertySource),
        Util.loadLong(prefix, SUMMARY_MAX_AGE_SECONDS, propertySource),
        Util.loadInteger(prefix, SUMMARY_NUMBER_OF_AGE_BUCKETS, propertySource),
        Util.loadBoolean(prefix + "." + USE_OTEL_METRICS, properties),
        prefix);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    @Nullable private Boolean exemplarsEnabled;
    @Nullable private Boolean histogramNativeOnly;
    @Nullable private Boolean histogramClassicOnly;
    @Nullable private List<Double> histogramClassicUpperBounds;
    @Nullable private Integer histogramNativeInitialSchema;
    @Nullable private Double histogramNativeMinZeroThreshold;
    @Nullable private Double histogramNativeMaxZeroThreshold;
    @Nullable private Integer histogramNativeMaxNumberOfBuckets;
    @Nullable private Long histogramNativeResetDurationSeconds;
    @Nullable private List<Double> summaryQuantiles;
    @Nullable private List<Double> summaryQuantileErrors;
    @Nullable private Long summaryMaxAgeSeconds;
    @Nullable private Integer summaryNumberOfAgeBuckets;
    @Nullable private Boolean useOtelMetrics;

    private Builder() {}

    public MetricsProperties build() {
      return new MetricsProperties(
          exemplarsEnabled,
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
          useOtelMetrics);
    }

    /** See {@link MetricsProperties#getExemplarsEnabled()} */
    public Builder exemplarsEnabled(@Nullable Boolean exemplarsEnabled) {
      this.exemplarsEnabled = exemplarsEnabled;
      return this;
    }

    /** See {@link MetricsProperties#getHistogramNativeOnly()} */
    public Builder histogramNativeOnly(@Nullable Boolean histogramNativeOnly) {
      this.histogramNativeOnly = histogramNativeOnly;
      return this;
    }

    /** See {@link MetricsProperties#getHistogramClassicOnly()} */
    public Builder histogramClassicOnly(@Nullable Boolean histogramClassicOnly) {
      this.histogramClassicOnly = histogramClassicOnly;
      return this;
    }

    /** See {@link MetricsProperties#getHistogramClassicUpperBounds()} */
    public Builder histogramClassicUpperBounds(double... histogramClassicUpperBounds) {
      this.histogramClassicUpperBounds = Util.toList(histogramClassicUpperBounds);
      return this;
    }

    /** See {@link MetricsProperties#getHistogramNativeInitialSchema()} */
    public Builder histogramNativeInitialSchema(@Nullable Integer histogramNativeInitialSchema) {
      this.histogramNativeInitialSchema = histogramNativeInitialSchema;
      return this;
    }

    /** See {@link MetricsProperties#getHistogramNativeMinZeroThreshold()} */
    public Builder histogramNativeMinZeroThreshold(
        @Nullable Double histogramNativeMinZeroThreshold) {
      this.histogramNativeMinZeroThreshold = histogramNativeMinZeroThreshold;
      return this;
    }

    /** See {@link MetricsProperties#getHistogramNativeMaxZeroThreshold()} */
    public Builder histogramNativeMaxZeroThreshold(
        @Nullable Double histogramNativeMaxZeroThreshold) {
      this.histogramNativeMaxZeroThreshold = histogramNativeMaxZeroThreshold;
      return this;
    }

    /** See {@link MetricsProperties#getHistogramNativeMaxNumberOfBuckets()} */
    public Builder histogramNativeMaxNumberOfBuckets(
        @Nullable Integer histogramNativeMaxNumberOfBuckets) {
      this.histogramNativeMaxNumberOfBuckets = histogramNativeMaxNumberOfBuckets;
      return this;
    }

    /** See {@link MetricsProperties#getHistogramNativeResetDurationSeconds()} */
    public Builder histogramNativeResetDurationSeconds(
        @Nullable Long histogramNativeResetDurationSeconds) {
      this.histogramNativeResetDurationSeconds = histogramNativeResetDurationSeconds;
      return this;
    }

    /** See {@link MetricsProperties#getSummaryQuantiles()} */
    public Builder summaryQuantiles(double... summaryQuantiles) {
      this.summaryQuantiles = Util.toList(summaryQuantiles);
      return this;
    }

    /** See {@link MetricsProperties#getSummaryQuantileErrors()} */
    public Builder summaryQuantileErrors(double... summaryQuantileErrors) {
      this.summaryQuantileErrors = Util.toList(summaryQuantileErrors);
      return this;
    }

    /** See {@link MetricsProperties#getSummaryMaxAgeSeconds()} */
    public Builder summaryMaxAgeSeconds(@Nullable Long summaryMaxAgeSeconds) {
      this.summaryMaxAgeSeconds = summaryMaxAgeSeconds;
      return this;
    }

    /** See {@link MetricsProperties#getSummaryNumberOfAgeBuckets()} */
    public Builder summaryNumberOfAgeBuckets(@Nullable Integer summaryNumberOfAgeBuckets) {
      this.summaryNumberOfAgeBuckets = summaryNumberOfAgeBuckets;
      return this;
    }

    /** See {@link MetricsProperties#useOtelMetrics()} */
    public Builder useOtelMetrics(@Nullable Boolean useOtelMetrics) {
      this.useOtelMetrics = useOtelMetrics;
      return this;
    }
  }
}
