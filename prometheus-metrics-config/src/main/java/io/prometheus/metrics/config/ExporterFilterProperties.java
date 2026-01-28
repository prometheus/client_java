package io.prometheus.metrics.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Properties starting with io.prometheus.exporter.filter */
public class ExporterFilterProperties {

  public static final String METRIC_NAME_MUST_BE_EQUAL_TO = "metric_name_must_be_equal_to";
  public static final String METRIC_NAME_MUST_NOT_BE_EQUAL_TO = "metric_name_must_not_be_equal_to";
  public static final String METRIC_NAME_MUST_START_WITH = "metric_name_must_start_with";
  public static final String METRIC_NAME_MUST_NOT_START_WITH = "metric_name_must_not_start_with";
  private static final String PREFIX = "io.prometheus.exporter.filter";

  @Nullable private final List<String> allowedNames;
  @Nullable private final List<String> excludedNames;
  @Nullable private final List<String> allowedPrefixes;
  @Nullable private final List<String> excludedPrefixes;

  private ExporterFilterProperties(
      @Nullable List<String> allowedNames,
      @Nullable List<String> excludedNames,
      @Nullable List<String> allowedPrefixes,
      @Nullable List<String> excludedPrefixes) {
    this.allowedNames =
        allowedNames == null ? null : Collections.unmodifiableList(new ArrayList<>(allowedNames));
    this.excludedNames =
        excludedNames == null ? null : Collections.unmodifiableList(new ArrayList<>(excludedNames));
    this.allowedPrefixes =
        allowedPrefixes == null
            ? null
            : Collections.unmodifiableList(new ArrayList<>(allowedPrefixes));
    this.excludedPrefixes =
        excludedPrefixes == null
            ? null
            : Collections.unmodifiableList(new ArrayList<>(excludedPrefixes));
  }

  @Nullable
  public List<String> getAllowedMetricNames() {
    return allowedNames;
  }

  @Nullable
  public List<String> getExcludedMetricNames() {
    return excludedNames;
  }

  @Nullable
  public List<String> getAllowedMetricNamePrefixes() {
    return allowedPrefixes;
  }

  @Nullable
  public List<String> getExcludedMetricNamePrefixes() {
    return excludedPrefixes;
  }

  /**
   * Note that this will remove entries from {@code properties}. This is because we want to know if
   * there are unused properties remaining after all properties have been loaded.
   */
  static ExporterFilterProperties load(Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    List<String> allowedNames =
        Util.loadStringList(PREFIX + "." + METRIC_NAME_MUST_BE_EQUAL_TO, properties);
    List<String> excludedNames =
        Util.loadStringList(PREFIX + "." + METRIC_NAME_MUST_NOT_BE_EQUAL_TO, properties);
    List<String> allowedPrefixes =
        Util.loadStringList(PREFIX + "." + METRIC_NAME_MUST_START_WITH, properties);
    List<String> excludedPrefixes =
        Util.loadStringList(PREFIX + "." + METRIC_NAME_MUST_NOT_START_WITH, properties);
    return new ExporterFilterProperties(
        allowedNames, excludedNames, allowedPrefixes, excludedPrefixes);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private List<String> allowedNames;
    @Nullable private List<String> excludedNames;
    @Nullable private List<String> allowedPrefixes;
    @Nullable private List<String> excludedPrefixes;

    private Builder() {}

    /** Only allowed metric names will be exposed. */
    public Builder allowedNames(String... allowedNames) {
      this.allowedNames = Arrays.asList(allowedNames);
      return this;
    }

    /** Excluded metric names will not be exposed. */
    public Builder excludedNames(String... excludedNames) {
      this.excludedNames = Arrays.asList(excludedNames);
      return this;
    }

    /** Only metrics with a name starting with an allowed prefix will be exposed. */
    public Builder allowedPrefixes(String... allowedPrefixes) {
      this.allowedPrefixes = Arrays.asList(allowedPrefixes);
      return this;
    }

    /** Metrics with a name starting with an excluded prefix will not be exposed. */
    public Builder excludedPrefixes(String... excludedPrefixes) {
      this.excludedPrefixes = Arrays.asList(excludedPrefixes);
      return this;
    }

    public ExporterFilterProperties build() {
      return new ExporterFilterProperties(
          allowedNames, excludedNames, allowedPrefixes, excludedPrefixes);
    }
  }
}
