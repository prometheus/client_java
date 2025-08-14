package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.config.EscapingScheme;

import javax.annotation.Nullable;

/** Immutable container for metric metadata: name, help, unit. */
public final class MetricMetadata {

  /**
   * Name without suffix.
   *
   * <p>For example, the name for a counter "http_requests_total" is "http_requests". The name of an
   * info called "jvm_info" is "jvm".
   *
   * <p>We allow dots in label names. Dots are automatically replaced with underscores in Prometheus
   * exposition formats. However, if metrics from this library are exposed in OpenTelemetry format
   * dots are retained.
   *
   * <p>See {@link #MetricMetadata(String, String, Unit)} for more info on naming conventions.
   */
  private final String name;

  /**
   * Same as name that all invalid char (without Unicode support) are replaced by _
   *
   * <p>Multiple metrics with the same prometheusName are not allowed, because they would end up in
   * the same time series in Prometheus if {@link EscapingScheme#UNDERSCORE_ESCAPING} or {@link
   * EscapingScheme#DOTS_ESCAPING} is used.
   */
  private final String prometheusName;

  @Nullable private final String help;
  @Nullable private final Unit unit;

  /** See {@link #MetricMetadata(String, String, Unit)} */
  public MetricMetadata(String name) {
    this(name, null, null);
  }

  /** See {@link #MetricMetadata(String, String, Unit)} */
  public MetricMetadata(String name, String help) {
    this(name, help, null);
  }

  /**
   * Constructor.
   *
   * @param name must not be {@code null}. {@link PrometheusNaming#isValidMetricName(String)
   *     isValidMetricName(name)} must be {@code true}. Use {@link
   *     PrometheusNaming#sanitizeMetricName(String)} to convert arbitrary strings into valid names.
   * @param help optional. May be {@code null}.
   * @param unit optional. May be {@code null}.
   */
  public MetricMetadata(String name, @Nullable String help, @Nullable Unit unit) {
    this.name = name;
    this.help = help;
    this.unit = unit;
    validate();
    this.prometheusName = PrometheusNaming.prometheusName(name);
  }

  /**
   * The name does not include the {@code _total} suffix for counter metrics or the {@code _info}
   * suffix for Info metrics.
   *
   * <p>The name may contain any Unicode chars. Use {@link #getPrometheusName()} to get the name in
   * legacy Prometheus format, i.e. with all dots and all invalid chars replaced by underscores.
   */
  public String getName() {
    return name;
  }

  /**
   * Same as {@link #getName()} but with all invalid characters and dots replaced by underscores.
   *
   * <p>This is used by Prometheus exposition formats.
   */
  public String getPrometheusName() {
    return prometheusName;
  }

  @Nullable
  public String getHelp() {
    return help;
  }

  public boolean hasUnit() {
    return unit != null;
  }

  @Nullable
  public Unit getUnit() {
    return unit;
  }

  private void validate() {
    if (name == null) {
      throw new IllegalArgumentException("Missing required field: name is null");
    }
    String error = PrometheusNaming.validateMetricName(name);
    if (error != null) {
      throw new IllegalArgumentException(
          "'"
              + name
              + "': Illegal metric name. "
              + error
              + " Call "
              + PrometheusNaming.class.getSimpleName()
              + ".sanitizeMetricName(name) to avoid this error.");
    }
    if (hasUnit()) {
      if (!name.endsWith("_" + unit) && !name.endsWith("." + unit)) {
        throw new IllegalArgumentException(
            "'"
                + name
                + "': Illegal metric name. If the unit is non-null, "
                + "the name must end with the unit: _"
                + unit
                + "."
                + " Call "
                + PrometheusNaming.class.getSimpleName()
                + ".sanitizeMetricName(name, unit) to avoid this error.");
      }
    }
  }

  MetricMetadata escape(EscapingScheme escapingScheme) {
    return new MetricMetadata(PrometheusNaming.escapeName(name, escapingScheme), help, unit);
  }
}
