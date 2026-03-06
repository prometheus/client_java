package io.prometheus.metrics.config;

import javax.annotation.Nullable;

/** Properties starting with io.prometheus.open_metrics2 */
public class OpenMetrics2Properties {

  private static final String PREFIX = "io.prometheus.open_metrics2";
  private static final String CONTENT_NEGOTIATION = "content_negotiation";
  private static final String DISABLE_SUFFIX_APPENDING = "disable_suffix_appending";
  private static final String COMPOSITE_VALUES = "composite_values";
  private static final String EXEMPLAR_COMPLIANCE = "exemplar_compliance";
  private static final String NATIVE_HISTOGRAMS = "native_histograms";

  @Nullable private final Boolean contentNegotiation;
  @Nullable private final Boolean disableSuffixAppending;
  @Nullable private final Boolean compositeValues;
  @Nullable private final Boolean exemplarCompliance;
  @Nullable private final Boolean nativeHistograms;

  private OpenMetrics2Properties(
      @Nullable Boolean contentNegotiation,
      @Nullable Boolean disableSuffixAppending,
      @Nullable Boolean compositeValues,
      @Nullable Boolean exemplarCompliance,
      @Nullable Boolean nativeHistograms) {
    this.contentNegotiation = contentNegotiation;
    this.disableSuffixAppending = disableSuffixAppending;
    this.compositeValues = compositeValues;
    this.exemplarCompliance = exemplarCompliance;
    this.nativeHistograms = nativeHistograms;
  }

  /** Gate OM2 features behind content negotiation. Default is {@code false}. */
  public boolean getContentNegotiation() {
    return contentNegotiation != null && contentNegotiation;
  }

  /** Suppress {@code _total} and unit suffixes. Default is {@code false}. */
  public boolean getDisableSuffixAppending() {
    return disableSuffixAppending != null && disableSuffixAppending;
  }

  /** Single-line histogram/summary with {@code st@}. Default is {@code false}. */
  public boolean getCompositeValues() {
    return compositeValues != null && compositeValues;
  }

  /** Mandatory timestamps, no 128-char limit for exemplars. Default is {@code false}. */
  public boolean getExemplarCompliance() {
    return exemplarCompliance != null && exemplarCompliance;
  }

  /** Exponential buckets support for native histograms. Default is {@code false}. */
  public boolean getNativeHistograms() {
    return nativeHistograms != null && nativeHistograms;
  }

  /**
   * Note that this will remove entries from {@code propertySource}. This is because we want to know
   * if there are unused properties remaining after all properties have been loaded.
   */
  static OpenMetrics2Properties load(PropertySource propertySource)
      throws PrometheusPropertiesException {
    Boolean contentNegotiation = Util.loadBoolean(PREFIX, CONTENT_NEGOTIATION, propertySource);
    Boolean disableSuffixAppending =
        Util.loadBoolean(PREFIX, DISABLE_SUFFIX_APPENDING, propertySource);
    Boolean compositeValues = Util.loadBoolean(PREFIX, COMPOSITE_VALUES, propertySource);
    Boolean exemplarCompliance = Util.loadBoolean(PREFIX, EXEMPLAR_COMPLIANCE, propertySource);
    Boolean nativeHistograms = Util.loadBoolean(PREFIX, NATIVE_HISTOGRAMS, propertySource);
    return new OpenMetrics2Properties(
        contentNegotiation,
        disableSuffixAppending,
        compositeValues,
        exemplarCompliance,
        nativeHistograms);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private Boolean contentNegotiation;
    @Nullable private Boolean disableSuffixAppending;
    @Nullable private Boolean compositeValues;
    @Nullable private Boolean exemplarCompliance;
    @Nullable private Boolean nativeHistograms;

    private Builder() {}

    /** See {@link #getContentNegotiation()} */
    public Builder contentNegotiation(boolean contentNegotiation) {
      this.contentNegotiation = contentNegotiation;
      return this;
    }

    /** See {@link #getDisableSuffixAppending()} */
    public Builder disableSuffixAppending(boolean disableSuffixAppending) {
      this.disableSuffixAppending = disableSuffixAppending;
      return this;
    }

    /** See {@link #getCompositeValues()} */
    public Builder compositeValues(boolean compositeValues) {
      this.compositeValues = compositeValues;
      return this;
    }

    /** See {@link #getExemplarCompliance()} */
    public Builder exemplarCompliance(boolean exemplarCompliance) {
      this.exemplarCompliance = exemplarCompliance;
      return this;
    }

    /** See {@link #getNativeHistograms()} */
    public Builder nativeHistograms(boolean nativeHistograms) {
      this.nativeHistograms = nativeHistograms;
      return this;
    }

    /** Enable all OpenMetrics 2.0 features */
    public Builder enableAll() {
      this.contentNegotiation = true;
      this.disableSuffixAppending = true;
      this.compositeValues = true;
      this.exemplarCompliance = true;
      this.nativeHistograms = true;
      return this;
    }

    public OpenMetrics2Properties build() {
      return new OpenMetrics2Properties(
          contentNegotiation,
          disableSuffixAppending,
          compositeValues,
          exemplarCompliance,
          nativeHistograms);
    }
  }
}
