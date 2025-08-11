package io.prometheus.metrics.model.snapshots;

import static java.lang.Character.MAX_CODE_POINT;
import static java.lang.Character.MAX_LOW_SURROGATE;
import static java.lang.Character.MIN_HIGH_SURROGATE;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.config.PrometheusPropertiesLoader;
import io.prometheus.metrics.config.ValidationScheme;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for Prometheus Metric and Label naming.
 *
 * <p>Note that this library allows dots in metric and label names. Dots will automatically be
 * replaced with underscores in Prometheus exposition formats. However, if metrics are exposed in
 * OpenTelemetry format the dots are retained.
 */
public class PrometheusNaming {

  /**
   * nameValidationScheme determines the method of name validation to be used by all calls to
   * validateMetricName() and isValidMetricName(). Setting UTF-8 mode in isolation from other
   * components that don't support UTF-8 may result in bugs or other undefined behavior. This value
   * is intended to be set by UTF-8-aware binaries as part of their startup via a properties file.
   */
  public static ValidationScheme nameValidationScheme =
      PrometheusProperties.get().getNamingProperties().getValidationScheme();

  /** Default escaping scheme for names when not specified. */
  public static final EscapingScheme DEFAULT_ESCAPING_SCHEME = EscapingScheme.UNDERSCORE_ESCAPING;

  /**
   * ESCAPING_KEY is the key in an Accept header that defines how metric and label names that do not
   * conform to the legacy character requirements should be escaped when being scraped by a legacy
   * Prometheus system. If a system does not explicitly pass an escaping parameter in the Accept
   * header, the default escaping scheme will be used.
   */
  public static final String ESCAPING_KEY = "escaping";

  private static final String METRIC_NAME_LABEL = "__name__";

  /** Legal characters for metric names, including dot. */
  private static final Pattern LEGACY_METRIC_NAME_PATTERN =
      Pattern.compile("^[a-zA-Z_.:][a-zA-Z0-9_.:]*$");

  private static final Pattern METRIC_NAME_PATTERN = Pattern.compile("^[a-zA-Z_:][a-zA-Z0-9_:]*$");

  /** Legal characters for label names, including dot. */
  private static final Pattern LEGACY_LABEL_NAME_PATTERN =
      Pattern.compile("^[a-zA-Z_.][a-zA-Z0-9_.]*$");

  /** Legal characters for unit names, including dot. */
  private static final Pattern UNIT_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.:]+$");

  /**
   * According to OpenMetrics {@code _count} and {@code _sum} (and {@code _gcount}, {@code _gsum})
   * should also be reserved metric name suffixes. However, popular instrumentation libraries have
   * Gauges with names ending in {@code _count}. Examples:
   *
   * <ul>
   *   <li>Micrometer: {@code jvm_buffer_count}
   *   <li>OpenTelemetry: {@code process_runtime_jvm_buffer_count}
   * </ul>
   *
   * We do not treat {@code _count} and {@code _sum} as reserved suffixes here for compatibility
   * with these libraries. However, there is a risk of name conflict if someone creates a gauge
   * named {@code my_data_count} and a histogram or summary named {@code my_data}, because the
   * histogram or summary will implicitly have a sample named {@code my_data_count}.
   */
  private static final String[] RESERVED_METRIC_NAME_SUFFIXES = {
    "_total", "_created", "_bucket", "_info",
    ".total", ".created", ".bucket", ".info"
  };

  // VisibleForTesting
  public static void resetForTest() {
    nameValidationScheme =
        PrometheusPropertiesLoader.load().getNamingProperties().getValidationScheme();
  }

  /**
   * Get the current validation scheme. This method exists primarily to enable testing different
   * validation behaviors while keeping the validation scheme field final and immutable.
   */
  public static ValidationScheme getValidationScheme() {
    return nameValidationScheme;
  }

  /**
   * Test if a metric name is valid. Rules:
   *
   * <ul>
   *   <li>The name must match {@link #LEGACY_METRIC_NAME_PATTERN}.
   *   <li>The name MUST NOT end with one of the {@link #RESERVED_METRIC_NAME_SUFFIXES}.
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
   */
  public static boolean isValidMetricName(String name) {
    return validateMetricName(name) == null;
  }

  public static String validateMetricName(String name) {
    switch (getValidationScheme()) {
      case LEGACY_VALIDATION:
        return validateLegacyMetricName(name);
      case UTF_8_VALIDATION:
        if (!isValidUtf8(name)) {
          return "The metric name contains unsupported characters";
        }
        return null;
      default:
        throw new RuntimeException(
            "Invalid name validation scheme requested: " + getValidationScheme());
    }
  }

  /**
   * Same as {@link #isValidMetricName(String)}, but produces an error message.
   *
   * <p>The name is valid if the error message is {@code null}.
   */
  public static String validateLegacyMetricName(String name) {
    for (String reservedSuffix : RESERVED_METRIC_NAME_SUFFIXES) {
      if (name.endsWith(reservedSuffix)) {
        return "The metric name must not include the '" + reservedSuffix + "' suffix.";
      }
    }
    if (!isValidLegacyMetricName(name)) {
      return "The metric name contains unsupported characters";
    }
    return null;
  }

  public static boolean isValidLegacyMetricName(String name) {
    switch (getValidationScheme()) {
      case LEGACY_VALIDATION:
        return LEGACY_METRIC_NAME_PATTERN.matcher(name).matches();
      case UTF_8_VALIDATION:
        return METRIC_NAME_PATTERN.matcher(name).matches();
      default:
        throw new RuntimeException(
            "Invalid name validation scheme requested: " + getValidationScheme());
    }
  }

  public static boolean isValidLabelName(String name) {
    switch (getValidationScheme()) {
      case LEGACY_VALIDATION:
        return isValidLegacyLabelName(name);
      case UTF_8_VALIDATION:
        return isValidUtf8(name);
      default:
        throw new RuntimeException(
            "Invalid name validation scheme requested: " + getValidationScheme());
    }
  }

  private static boolean isValidUtf8(String name) {
    return !name.isEmpty() && StandardCharsets.UTF_8.newEncoder().canEncode(name);
  }

  public static boolean isValidLegacyLabelName(String name) {
    return LEGACY_LABEL_NAME_PATTERN.matcher(name).matches()
        && !(name.startsWith("__")
            || name.startsWith("._")
            || name.startsWith("..")
            || name.startsWith("_."));
  }

  /**
   * Units may not have illegal characters, and they may not end with a reserved suffix like
   * 'total'.
   */
  public static boolean isValidUnitName(String name) {
    return validateUnitName(name) == null;
  }

  /** Same as {@link #isValidUnitName(String)} but returns an error message. */
  public static String validateUnitName(String name) {
    if (name.isEmpty()) {
      return "The unit name must not be empty.";
    }
    for (String reservedSuffix : RESERVED_METRIC_NAME_SUFFIXES) {
      String suffixName = reservedSuffix.substring(1);
      if (name.endsWith(suffixName)) {
        return suffixName + " is a reserved suffix in Prometheus";
      }
    }
    if (!UNIT_NAME_PATTERN.matcher(name).matches()) {
      return "The unit name contains unsupported characters";
    }
    return null;
  }

  /**
   * Get the metric or label name that is used in Prometheus exposition format.
   *
   * @param name must be a valid metric or label name, i.e. {@link #isValidMetricName(String)
   *     isValidMetricName(name)} or {@link #isValidLabelName(String) isValidLabelName(name)} must
   *     be true.
   * @return the name with dots replaced by underscores.
   */
  public static String prometheusName(String name) {
    return PrometheusNaming.escapeName(name, EscapingScheme.UNDERSCORE_ESCAPING);
  }

  /**
   * Convert an arbitrary string to a name where {@link #isValidMetricName(String)
   * isValidMetricName(name)} is true.
   */
  public static String sanitizeMetricName(String metricName) {
    if (metricName.isEmpty()) {
      throw new IllegalArgumentException("Cannot convert an empty string to a valid metric name.");
    }
    String sanitizedName = replaceIllegalCharsInMetricName(metricName);
    boolean modified = true;
    while (modified) {
      modified = false;
      for (String reservedSuffix : RESERVED_METRIC_NAME_SUFFIXES) {
        if (sanitizedName.equals(reservedSuffix)) {
          // This is for the corner case when you call sanitizeMetricName("_total").
          // In that case the result will be "total".
          return reservedSuffix.substring(1);
        }
        if (sanitizedName.endsWith(reservedSuffix)) {
          sanitizedName =
              sanitizedName.substring(0, sanitizedName.length() - reservedSuffix.length());
          modified = true;
        }
      }
    }
    return sanitizedName;
  }

  /**
   * Like {@link #sanitizeMetricName(String)}, but also makes sure that the unit is appended as a
   * suffix if the unit is not {@code null}.
   */
  public static String sanitizeMetricName(String metricName, Unit unit) {
    String result = sanitizeMetricName(metricName);
    if (unit != null) {
      if (!result.endsWith("_" + unit) && !result.endsWith("." + unit)) {
        result += "_" + unit;
      }
    }
    return result;
  }

  /**
   * Convert an arbitrary string to a name where {@link #isValidLabelName(String)
   * isValidLabelName(name)} is true.
   */
  public static String sanitizeLabelName(String labelName) {
    if (labelName.isEmpty()) {
      throw new IllegalArgumentException("Cannot convert an empty string to a valid label name.");
    }
    String sanitizedName = replaceIllegalCharsInLabelName(labelName);
    while (sanitizedName.startsWith("__")
        || sanitizedName.startsWith("_.")
        || sanitizedName.startsWith("._")
        || sanitizedName.startsWith("..")) {
      sanitizedName = sanitizedName.substring(1);
    }
    return sanitizedName;
  }

  /**
   * Convert an arbitrary string to a name where {@link #isValidUnitName(String)
   * isValidUnitName(name)} is true.
   *
   * @throws IllegalArgumentException if the {@code unitName} cannot be converted, for example if
   *     you call {@code sanitizeUnitName("total")} or {@code sanitizeUnitName("")}.
   * @throws NullPointerException if {@code unitName} is null.
   */
  public static String sanitizeUnitName(String unitName) {
    if (unitName.isEmpty()) {
      throw new IllegalArgumentException("Cannot convert an empty string to a valid unit name.");
    }
    String sanitizedName = replaceIllegalCharsInUnitName(unitName);
    boolean modified = true;
    while (modified) {
      modified = false;
      while (sanitizedName.startsWith("_") || sanitizedName.startsWith(".")) {
        sanitizedName = sanitizedName.substring(1);
        modified = true;
      }
      while (sanitizedName.endsWith(".") || sanitizedName.endsWith("_")) {
        sanitizedName = sanitizedName.substring(0, sanitizedName.length() - 1);
        modified = true;
      }
      for (String reservedSuffix : RESERVED_METRIC_NAME_SUFFIXES) {
        String suffixName = reservedSuffix.substring(1);
        if (sanitizedName.endsWith(suffixName)) {
          sanitizedName = sanitizedName.substring(0, sanitizedName.length() - suffixName.length());
          modified = true;
        }
      }
    }
    if (sanitizedName.isEmpty()) {
      throw new IllegalArgumentException(
          "Cannot convert '" + unitName + "' into a valid unit name.");
    }
    return sanitizedName;
  }

  /** Returns a string that matches {@link #LEGACY_METRIC_NAME_PATTERN}. */
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

  /** Returns a string that matches {@link #LEGACY_LABEL_NAME_PATTERN}. */
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

  /** Returns a string that matches {@link #UNIT_NAME_PATTERN}. */
  private static String replaceIllegalCharsInUnitName(String name) {
    int length = name.length();
    char[] sanitized = new char[length];
    for (int i = 0; i < length; i++) {
      char ch = name.charAt(i);
      if (ch == ':'
          || ch == '.'
          || (ch >= 'a' && ch <= 'z')
          || (ch >= 'A' && ch <= 'Z')
          || (ch >= '0' && ch <= '9')) {
        sanitized[i] = ch;
      } else {
        sanitized[i] = '_';
      }
    }
    return new String(sanitized);
  }

  /** Escapes the given metric names and labels with the given escaping scheme. */
  public static MetricSnapshot escapeMetricSnapshot(MetricSnapshot v, EscapingScheme scheme) {
    if (v == null) {
      return null;
    }

    if (scheme == EscapingScheme.NO_ESCAPING) {
      return v;
    }

    String outName =
        isValidLegacyMetricName(v.getMetadata().getPrometheusName())
            ? v.getMetadata().getPrometheusName()
            : escapeName(v.getMetadata().getPrometheusName(), scheme);

    List<DataPointSnapshot> outDataPoints = new ArrayList<>();

    for (DataPointSnapshot d : v.getDataPoints()) {
      if (!metricNeedsEscaping(d)) {
        outDataPoints.add(d);
        continue;
      }

      Labels.Builder outLabelsBuilder = Labels.builder();

      for (Label l : d.getLabels()) {
        if (METRIC_NAME_LABEL.equals(l.getName())) {
          if (l.getValue() == null || isValidLegacyMetricName(l.getValue())) {
            outLabelsBuilder.label(l.getName(), l.getValue());
            continue;
          }
          outLabelsBuilder.label(l.getName(), escapeName(l.getValue(), scheme));
          continue;
        }
        if (l.getName() == null || isValidLegacyMetricName(l.getName())) {
          outLabelsBuilder.label(l.getName(), l.getValue());
          continue;
        }
        outLabelsBuilder.label(escapeName(l.getName(), scheme), l.getValue());
      }

      Labels outLabels = outLabelsBuilder.build();
      DataPointSnapshot outDataPointSnapshot = createEscapedDataPointSnapshot(v, d, outLabels);
      outDataPoints.add(outDataPointSnapshot);
    }

    return createEscapedMetricSnapshot(v, outName, outDataPoints);
  }

  static boolean metricNeedsEscaping(DataPointSnapshot d) {
    Labels labels = d.getLabels();
    for (Label l : labels) {
      if (l.getName().equals(METRIC_NAME_LABEL) && !isValidLegacyMetricName(l.getValue())) {
        return true;
      }
      if (!isValidLegacyMetricName(l.getName())) {
        return true;
      }
    }
    return false;
  }

  private static DataPointSnapshot createEscapedDataPointSnapshot(
      MetricSnapshot v, DataPointSnapshot d, Labels outLabels) {
    if (v instanceof CounterSnapshot) {
      return CounterSnapshot.CounterDataPointSnapshot.builder()
          .value(((CounterSnapshot.CounterDataPointSnapshot) d).getValue())
          .exemplar(((CounterSnapshot.CounterDataPointSnapshot) d).getExemplar())
          .labels(outLabels)
          .createdTimestampMillis(d.getCreatedTimestampMillis())
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else if (v instanceof GaugeSnapshot) {
      return GaugeSnapshot.GaugeDataPointSnapshot.builder()
          .value(((GaugeSnapshot.GaugeDataPointSnapshot) d).getValue())
          .exemplar(((GaugeSnapshot.GaugeDataPointSnapshot) d).getExemplar())
          .labels(outLabels)
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else if (v instanceof HistogramSnapshot) {
      return HistogramSnapshot.HistogramDataPointSnapshot.builder()
          .classicHistogramBuckets(
              ((HistogramSnapshot.HistogramDataPointSnapshot) d).getClassicBuckets())
          .nativeSchema(((HistogramSnapshot.HistogramDataPointSnapshot) d).getNativeSchema())
          .nativeZeroCount(((HistogramSnapshot.HistogramDataPointSnapshot) d).getNativeZeroCount())
          .nativeZeroThreshold(
              ((HistogramSnapshot.HistogramDataPointSnapshot) d).getNativeZeroThreshold())
          .nativeBucketsForPositiveValues(
              ((HistogramSnapshot.HistogramDataPointSnapshot) d)
                  .getNativeBucketsForPositiveValues())
          .nativeBucketsForNegativeValues(
              ((HistogramSnapshot.HistogramDataPointSnapshot) d)
                  .getNativeBucketsForNegativeValues())
          .count(((HistogramSnapshot.HistogramDataPointSnapshot) d).getCount())
          .sum(((HistogramSnapshot.HistogramDataPointSnapshot) d).getSum())
          .exemplars(((HistogramSnapshot.HistogramDataPointSnapshot) d).getExemplars())
          .labels(outLabels)
          .createdTimestampMillis(d.getCreatedTimestampMillis())
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else if (v instanceof SummarySnapshot) {
      return SummarySnapshot.SummaryDataPointSnapshot.builder()
          .quantiles(((SummarySnapshot.SummaryDataPointSnapshot) d).getQuantiles())
          .count(((SummarySnapshot.SummaryDataPointSnapshot) d).getCount())
          .sum(((SummarySnapshot.SummaryDataPointSnapshot) d).getSum())
          .exemplars(((SummarySnapshot.SummaryDataPointSnapshot) d).getExemplars())
          .labels(outLabels)
          .createdTimestampMillis(d.getCreatedTimestampMillis())
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else if (v instanceof InfoSnapshot) {
      return InfoSnapshot.InfoDataPointSnapshot.builder()
          .labels(outLabels)
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else if (v instanceof StateSetSnapshot) {
      StateSetSnapshot.StateSetDataPointSnapshot.Builder builder =
          StateSetSnapshot.StateSetDataPointSnapshot.builder()
              .labels(outLabels)
              .scrapeTimestampMillis(d.getScrapeTimestampMillis());
      for (StateSetSnapshot.State state : ((StateSetSnapshot.StateSetDataPointSnapshot) d)) {
        builder.state(state.getName(), state.isTrue());
      }
      return builder.build();
    } else if (v instanceof UnknownSnapshot) {
      return UnknownSnapshot.UnknownDataPointSnapshot.builder()
          .labels(outLabels)
          .value(((UnknownSnapshot.UnknownDataPointSnapshot) d).getValue())
          .exemplar(((UnknownSnapshot.UnknownDataPointSnapshot) d).getExemplar())
          .scrapeTimestampMillis(d.getScrapeTimestampMillis())
          .build();
    } else {
      throw new IllegalArgumentException("Unknown MetricSnapshot type: " + v.getClass());
    }
  }

  private static MetricSnapshot createEscapedMetricSnapshot(
      MetricSnapshot v, String outName, List<DataPointSnapshot> outDataPoints) {
    if (v instanceof CounterSnapshot) {
      CounterSnapshot.Builder builder =
          CounterSnapshot.builder()
              .name(outName)
              .help(v.getMetadata().getHelp())
              .unit(v.getMetadata().getUnit());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((CounterSnapshot.CounterDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof GaugeSnapshot) {
      GaugeSnapshot.Builder builder =
          GaugeSnapshot.builder()
              .name(outName)
              .help(v.getMetadata().getHelp())
              .unit(v.getMetadata().getUnit());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((GaugeSnapshot.GaugeDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof HistogramSnapshot) {
      HistogramSnapshot.Builder builder =
          HistogramSnapshot.builder()
              .name(outName)
              .help(v.getMetadata().getHelp())
              .unit(v.getMetadata().getUnit())
              .gaugeHistogram(((HistogramSnapshot) v).isGaugeHistogram());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((HistogramSnapshot.HistogramDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof SummarySnapshot) {
      SummarySnapshot.Builder builder =
          SummarySnapshot.builder()
              .name(outName)
              .help(v.getMetadata().getHelp())
              .unit(v.getMetadata().getUnit());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((SummarySnapshot.SummaryDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof InfoSnapshot) {
      InfoSnapshot.Builder builder =
          InfoSnapshot.builder().name(outName).help(v.getMetadata().getHelp());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((InfoSnapshot.InfoDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof StateSetSnapshot) {
      StateSetSnapshot.Builder builder =
          StateSetSnapshot.builder().name(outName).help(v.getMetadata().getHelp());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((StateSetSnapshot.StateSetDataPointSnapshot) d);
      }
      return builder.build();
    } else if (v instanceof UnknownSnapshot) {
      UnknownSnapshot.Builder builder =
          UnknownSnapshot.builder()
              .name(outName)
              .help(v.getMetadata().getHelp())
              .unit(v.getMetadata().getUnit());
      for (DataPointSnapshot d : outDataPoints) {
        builder.dataPoint((UnknownSnapshot.UnknownDataPointSnapshot) d);
      }
      return builder.build();
    } else {
      throw new IllegalArgumentException("Unknown MetricSnapshot type: " + v.getClass());
    }
  }

  /**
   * Escapes the incoming name according to the provided escaping scheme. Depending on the rules of
   * escaping, this may cause no change in the string that is returned (especially NO_ESCAPING,
   * which by definition is a noop). This method does not do any validation of the name.
   */
  public static String escapeName(String name, EscapingScheme scheme) {
    if (name.isEmpty()) {
      return name;
    }
    StringBuilder escaped = new StringBuilder();
    switch (scheme) {
      case NO_ESCAPING:
        return name;
      case UNDERSCORE_ESCAPING:
        if (isValidLegacyMetricName(name)) {
          return name;
        }
        for (int i = 0; i < name.length(); ) {
          int c = name.codePointAt(i);
          if (isValidLegacyChar(c, i)) {
            escaped.appendCodePoint(c);
          } else {
            escaped.append('_');
          }
          i += Character.charCount(c);
        }
        return escaped.toString();
      case DOTS_ESCAPING:
        // Do not early return for legacy valid names, we still escape underscores.
        for (int i = 0; i < name.length(); ) {
          int c = name.codePointAt(i);
          if (c == '_') {
            escaped.append("__");
          } else if (c == '.') {
            escaped.append("_dot_");
          } else if (isValidLegacyChar(c, i)) {
            escaped.appendCodePoint(c);
          } else {
            escaped.append("__");
          }
          i += Character.charCount(c);
        }
        return escaped.toString();
      case VALUE_ENCODING_ESCAPING:
        if (isValidLegacyMetricName(name)) {
          return name;
        }
        escaped.append("U__");
        for (int i = 0; i < name.length(); ) {
          int c = name.codePointAt(i);
          if (c == '_') {
            escaped.append("__");
          } else if (isValidLegacyChar(c, i)) {
            escaped.appendCodePoint(c);
          } else if (!isValidUtf8Char(c)) {
            escaped.append("_FFFD_");
          } else {
            escaped.append('_');
            escaped.append(Integer.toHexString(c));
            escaped.append('_');
          }
          i += Character.charCount(c);
        }
        return escaped.toString();
      default:
        throw new IllegalArgumentException("Invalid escaping scheme " + scheme);
    }
  }

  /**
   * Unescapes the incoming name according to the provided escaping scheme if possible. Some schemes
   * are partially or totally non-roundtripable. If any error is encountered, returns the original
   * input.
   */
  @SuppressWarnings("IncrementInForLoopAndHeader")
  static String unescapeName(String name, EscapingScheme scheme) {
    if (name.isEmpty()) {
      return name;
    }
    switch (scheme) {
      case NO_ESCAPING:
        return name;
      case UNDERSCORE_ESCAPING:
        // It is not possible to unescape from underscore replacement.
        return name;
      case DOTS_ESCAPING:
        name = name.replaceAll("_dot_", ".");
        name = name.replaceAll("__", "_");
        return name;
      case VALUE_ENCODING_ESCAPING:
        Matcher matcher = Pattern.compile("U__").matcher(name);
        if (matcher.find()) {
          String escapedName = name.substring(matcher.end());
          StringBuilder unescaped = new StringBuilder();
          for (int i = 0; i < escapedName.length(); ) {
            // All non-underscores are treated normally.
            int c = escapedName.codePointAt(i);
            if (c != '_') {
              unescaped.appendCodePoint(c);
              i += Character.charCount(c);
              continue;
            }
            i++;
            if (i >= escapedName.length()) {
              return name;
            }
            // A double underscore is a single underscore.
            if (escapedName.codePointAt(i) == '_') {
              unescaped.append('_');
              i++;
              continue;
            }
            // We think we are in a UTF-8 code, process it.
            int utf8Val = 0;
            boolean foundClosingUnderscore = false;
            for (int j = 0; i < escapedName.length(); j++) {
              // This is too many characters for a UTF-8 value.
              if (j >= 6) {
                return name;
              }
              // Found a closing underscore, convert to a char, check validity, and append.
              if (escapedName.codePointAt(i) == '_') {
                // char utf8Char = (char) utf8Val;
                foundClosingUnderscore = true;
                if (!isValidUtf8Char(utf8Val)) {
                  return name;
                }
                unescaped.appendCodePoint(utf8Val);
                i++;
                break;
              }
              char r = Character.toLowerCase(escapedName.charAt(i));
              utf8Val *= 16;
              if (r >= '0' && r <= '9') {
                utf8Val += r - '0';
              } else if (r >= 'a' && r <= 'f') {
                utf8Val += r - 'a' + 10;
              } else {
                return name;
              }
              i++;
            }
            if (!foundClosingUnderscore) {
              return name;
            }
          }
          return unescaped.toString();
        } else {
          return name;
        }
      default:
        throw new IllegalArgumentException("Invalid escaping scheme " + scheme);
    }
  }

  static boolean isValidLegacyChar(int c, int i) {
    return (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || c == '_'
        || c == ':'
        || (c >= '0' && c <= '9' && i > 0);
  }

  private static boolean isValidUtf8Char(int c) {
    return (0 <= c && c < MIN_HIGH_SURROGATE) || (MAX_LOW_SURROGATE < c && c <= MAX_CODE_POINT);
  }
}
