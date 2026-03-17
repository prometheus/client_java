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

  /**
   * The base name for exposition, with unit suffix ensured and type suffix preserved. For example,
   * for {@code Counter.builder().name("events_total").unit(BYTES)}, this is "events_total_bytes".
   * Used by format writers for smart-append logic (e.g. deciding whether to append _total).
   */
  private final String expositionBaseName;

  private final String expositionBasePrometheusName;

  /**
   * The original name as provided by the user, before any modification (no suffix stripping, no
   * unit appending). For example, for {@code Counter.builder().name("req").unit(BYTES)}, this is
   * "req". Used by the OTel exporter with {@code preserve_names=true}.
   */
  private final String originalName;

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
    this(name, name, help, unit);
  }

  /**
   * Constructor with exposition base name.
   *
   * @param name the base name (with type suffixes stripped, e.g. "events" for a counter named
   *     "events_total")
   * @param expositionBaseName the name with unit suffix ensured and type suffix preserved, used by
   *     format writers for smart-append logic
   * @param help optional. May be {@code null}.
   * @param unit optional. May be {@code null}.
   */
  public MetricMetadata(
      String name, String expositionBaseName, @Nullable String help, @Nullable Unit unit) {
    this(name, expositionBaseName, expositionBaseName, help, unit);
  }

  /**
   * Constructor with exposition base name and original name.
   *
   * @param name the base name (with type suffixes stripped, e.g. "events" for a counter named
   *     "events_total")
   * @param expositionBaseName the name with unit suffix ensured and type suffix preserved
   * @param originalName the raw name as provided by the user, before any modification
   * @param help optional. May be {@code null}.
   * @param unit optional. May be {@code null}.
   */
  public MetricMetadata(
      String name,
      String expositionBaseName,
      String originalName,
      @Nullable String help,
      @Nullable Unit unit) {
    this.name = name;
    this.expositionBaseName = expositionBaseName;
    this.originalName = originalName;
    this.help = help;
    this.unit = unit;
    validate();
    this.prometheusName = PrometheusNaming.prometheusName(name);
    this.expositionBasePrometheusName = PrometheusNaming.prometheusName(expositionBaseName);
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

  /**
   * The original name as provided by the user, before any modification. For example, if the user
   * called {@code Counter.builder().name("req").unit(BYTES)}, this returns "req" while {@link
   * #getName()} returns "req_bytes" and {@link #getExpositionBaseName()} returns "req_bytes".
   */
  public String getOriginalName() {
    return originalName;
  }

  /**
   * The base name for exposition, with unit suffix ensured and type suffix preserved. For example,
   * if the user called {@code Counter.builder().name("events_total")}, this returns "events_total"
   * while {@link #getName()} returns "events".
   */
  public String getExpositionBaseName() {
    return expositionBaseName;
  }

  /**
   * Same as {@link #getExpositionBaseName()} but with all invalid characters and dots replaced by
   * underscores.
   */
  public String getExpositionBasePrometheusName() {
    return expositionBasePrometheusName;
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
    return new MetricMetadata(
        PrometheusNaming.escapeName(name, escapingScheme),
        PrometheusNaming.escapeName(expositionBaseName, escapingScheme),
        PrometheusNaming.escapeName(originalName, escapingScheme),
        help,
        unit);
  }
}
