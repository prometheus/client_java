package io.prometheus.metrics.model.snapshots;

import javax.annotation.Nullable;

/**
 * Utility for Prometheus Metric and Label naming.
 *
 * <p>Note that this library allows dots in metric and label names. Dots will automatically be
 * replaced with underscores in Prometheus exposition formats. However, if metrics are exposed in
 * OpenTelemetry format the dots are retained.
 *
 * @deprecated use {@link PrometheusNames} instead.
 */
@Deprecated
@SuppressWarnings("InlineMeSuggester")
public class PrometheusNaming {

  /**
   * Test if a metric name is valid. Rules:
   *
   * <ul>
   *   <li>The name must match {@link PrometheusNames#METRIC_NAME_PATTERN}.
   *   <li>The name MUST NOT end with one of the {@link
   *       PrometheusNames#RESERVED_METRIC_NAME_SUFFIXES}.
   * </ul>
   *
   * If a metric has a {@link Unit}, the metric name SHOULD end with the unit as a suffix. Note that
   * <a href="https://openmetrics.io/">OpenMetrics</a> requires metric names to have their unit as
   * suffix, and we implement this in {@code prometheus-metrics-core}. However, {@code
   * prometheus-metrics-model} does not enforce Unit suffixes.
   *
   * <p>Example: If you create a Counter for a processing time with Unit {@link Unit#SECONDS
   * SECONDS}, the name should be {@code processing_time_seconds}. When exposed in OpenMetrics Text
   * format, this will be represented as two values: {@code processing_time_seconds_total} for the
   * counter value, and the optional {@code processing_time_seconds_created} timestamp.
   *
   * <p>Use {@link #sanitizeMetricName(String)} to convert arbitrary Strings to valid metric names.
   *
   * @deprecated use {@link PrometheusNames#isValidMetricName(String)} instead.
   */
  @Deprecated
  public static boolean isValidMetricName(String name) {
    return validateMetricName(name) == null;
  }

  /**
   * Same as {@link #isValidMetricName(String)}, but produces an error message.
   *
   * <p>The name is valid if the error message is {@code null}.
   *
   * @deprecated use {@link PrometheusNames#validateMetricName(String)} instead.
   */
  @Deprecated
  @Nullable
  public static String validateMetricName(String name) {
    String reservedSuffix = PrometheusNames.findReservedSuffix(name);
    if (reservedSuffix != null) {
      return reservedSuffix;
    }
    if (!PrometheusNames.isValidLegacyMetricName(name)) {
      return "The metric name contains unsupported characters";
    }
    return null;
  }

  /**
   * Test if a label name is valid. Rules:
   *
   * <ul>
   *   <li>The name must match {@link PrometheusNames#LEGACY_LABEL_NAME_PATTERN _PATTERN}.
   *   <li>The name MUST NOT start with {@code __}, {@code ._}, or {@code _.} or {@code ..}
   * </ul>
   *
   * @deprecated use {@link PrometheusNames#isValidLabelName(String)} instead.
   */
  @Deprecated
  public static boolean isValidLabelName(String name) {
    return PrometheusNames.isValidLegacyLabelName(name)
        && !PrometheusNames.hasInvalidLabelPrefix(name);
  }

  /**
   * Units may not have illegal characters, and they may not end with a reserved suffix like
   * 'total'.
   *
   * @deprecated use {@link PrometheusNames#isValidUnitName(String)} instead.
   */
  @Deprecated
  public static boolean isValidUnitName(String name) {
    // no Unicode support for unit names
    return PrometheusNames.isValidUnitName(name);
  }

  /**
   * Same as {@link #isValidUnitName(String)} but returns an error message.
   *
   * @deprecated use {@link PrometheusNames#validateUnitName(String)} instead.
   */
  @Deprecated
  @Nullable
  public static String validateUnitName(String name) {
    // no Unicode support for unit names
    return PrometheusNames.validateUnitName(name);
  }

  /**
   * Get the metric or label name that is used in Prometheus exposition format.
   *
   * @param name must be a valid metric or label name, i.e. {@link #isValidMetricName(String)
   *     isValidMetricName(name)} or {@link #isValidLabelName(String) isValidLabelName(name)} must
   *     be true.
   * @return the name with dots replaced by underscores.
   * @deprecated use {@link PrometheusNames#prometheusName(String)} instead.
   */
  @Deprecated
  public static String prometheusName(String name) {
    return name.replace(".", "_");
  }

  /**
   * Convert an arbitrary string to a name where {@link #isValidMetricName(String)
   * isValidMetricName(name)} is true.
   *
   * @deprecated use {@link PrometheusNames#sanitizeMetricName(String)} instead.
   */
  @Deprecated
  public static String sanitizeMetricName(String metricName) {
    if (metricName.isEmpty()) {
      throw new IllegalArgumentException("Cannot convert an empty string to a valid metric name.");
    }
    return PrometheusNames.sanitizeMetricName(replaceIllegalCharsInMetricName(metricName));
  }

  /**
   * Like {@link #sanitizeMetricName(String)}, but also makes sure that the unit is appended as a
   * suffix if the unit is not {@code null}.
   *
   * @deprecated use {@link PrometheusNames#sanitizeMetricName(String, Unit)} instead.
   */
  @Deprecated
  public static String sanitizeMetricName(String metricName, Unit unit) {
    return PrometheusNames.sanitizeMetricName(replaceIllegalCharsInMetricName(metricName), unit);
  }

  /**
   * Convert an arbitrary string to a name where {@link #isValidLabelName(String)
   * isValidLabelName(name)} is true.
   *
   * @deprecated use {@link PrometheusNames#sanitizeLabelName(String)} instead.
   */
  @Deprecated
  public static String sanitizeLabelName(String labelName) {
    if (labelName.isEmpty()) {
      throw new IllegalArgumentException("Cannot convert an empty string to a valid label name.");
    }
    return PrometheusNames.sanitizeLabelName(replaceIllegalCharsInLabelName(labelName));
  }

  /**
   * Convert an arbitrary string to a name where {@link #isValidUnitName(String)
   * isValidUnitName(name)} is true.
   *
   * @throws IllegalArgumentException if the {@code unitName} cannot be converted, for example if
   *     you call {@code sanitizeUnitName("total")} or {@code sanitizeUnitName("")}.
   * @throws NullPointerException if {@code unitName} is null.
   * @deprecated use {@link PrometheusNames#sanitizeUnitName(String)} instead.
   */
  @Deprecated
  public static String sanitizeUnitName(String unitName) {
    // no Unicode support for unit names
    return PrometheusNames.sanitizeUnitName(unitName);
  }

  /** Returns a string that matches {@link PrometheusNames#METRIC_NAME_PATTERN}. */
  private static String replaceIllegalCharsInMetricName(String name) {
    int length = name.length();
    char[] sanitized = new char[length];
    for (int i = 0; i < length; i++) {
      char ch = name.charAt(i);
      if (ch == '.'
          || (ch >= 'a' && ch <= 'z')
          || (ch >= 'A' && ch <= 'Z')
          || (i > 0 && ch >= '0' && ch <= '9')) {
        sanitized[i] = ch;
      } else {
        sanitized[i] = '_';
      }
    }
    return new String(sanitized);
  }

  /** Returns a string that matches {@link PrometheusNames#LEGACY_LABEL_NAME_PATTERN}. */
  private static String replaceIllegalCharsInLabelName(String name) {
    int length = name.length();
    char[] sanitized = new char[length];
    for (int i = 0; i < length; i++) {
      char ch = name.charAt(i);
      if (ch == '.'
          || (ch >= 'a' && ch <= 'z')
          || (ch >= 'A' && ch <= 'Z')
          || (i > 0 && ch >= '0' && ch <= '9')) {
        sanitized[i] = ch;
      } else {
        sanitized[i] = '_';
      }
    }
    return new String(sanitized);
  }
}
