package io.prometheus.metrics.model.snapshots;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.MAX_LOW_SURROGATE;
import static java.lang.Character.MIN_HIGH_SURROGATE;

/**
 * Utility for Prometheus Metric and Label naming.
 * <p>
 * Note that this library allows dots in metric and label names. Dots will automatically be replaced with underscores
 * in Prometheus exposition formats. However, if metrics are exposed in OpenTelemetry format the dots are retained.
 */
public class PrometheusNaming {
    public static ValidationScheme nameValidationScheme = ValidationScheme.LEGACY_VALIDATION;

    public static EscapingScheme nameEscapingScheme = EscapingScheme.VALUE_ENCODING_ESCAPING;

    public static final String ESCAPING_KEY = "escaping";

    private static final String LOWERHEX = "0123456789abcdef";

    private static final String METRIC_NAME_LABEL= "__name__";

    /**
     * Legal characters for metric names, including dot.
     */
    private static final Pattern LEGACY_METRIC_NAME_PATTERN = Pattern.compile("^[a-zA-Z_.:][a-zA-Z0-9_.:]+$");

    private static final Pattern METRIC_NAME_PATTERN = Pattern.compile("^[a-zA-Z_:][a-zA-Z0-9_:]+$");

    /**
     * Legal characters for label names, including dot.
     */
    private static final Pattern LEGACY_LABEL_NAME_PATTERN = Pattern.compile("^[a-zA-Z_.][a-zA-Z0-9_.]*$");

    private static final Pattern LABEL_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    /**
     * According to OpenMetrics {@code _count} and {@code _sum} (and {@code _gcount}, {@code _gsum}) should also be
     * reserved metric name suffixes. However, popular instrumentation libraries have Gauges with names
     * ending in {@code _count}.
     * Examples:
     * <ul>
     * <li>Micrometer: {@code jvm_buffer_count}</li>
     * <li>OpenTelemetry: {@code process_runtime_jvm_buffer_count}</li>
     * </ul>
     * We do not treat {@code _count} and {@code _sum} as reserved suffixes here for compatibility with these libraries.
     * However, there is a risk of name conflict if someone creates a gauge named {@code my_data_count} and a
     * histogram or summary named {@code my_data}, because the histogram or summary will implicitly have a sample
     * named {@code my_data_count}.
     */
    private static final String[] RESERVED_METRIC_NAME_SUFFIXES = {
            "_total", "_created", "_bucket", "_info",
            ".total", ".created", ".bucket", ".info"
    };

    /**
     * Test if a metric name is valid. Rules:
     * <ul>
     * <li>The name must match {@link #LEGACY_METRIC_NAME_PATTERN}.</li>
     * <li>The name MUST NOT end with one of the {@link #RESERVED_METRIC_NAME_SUFFIXES}.</li>
     * </ul>
     * If a metric has a {@link Unit}, the metric name SHOULD end with the unit as a suffix.
     * Note that <a href="https://openmetrics.io/">OpenMetrics</a> requires metric names to have their unit as suffix,
     * and we implement this in {@code prometheus-metrics-core}. However, {@code prometheus-metrics-model}
     * does not enforce Unit suffixes.
     * <p>
     * Example: If you create a Counter for a processing time with Unit {@link Unit#SECONDS SECONDS},
     * the name should be {@code processing_time_seconds}. When exposed in OpenMetrics Text format,
     * this will be represented as two values: {@code processing_time_seconds_total} for the counter value,
     * and the optional {@code processing_time_seconds_created} timestamp.
     * <p>
     * Use {@link #sanitizeMetricName(String)} to convert arbitrary Strings to valid metric names.
     */
    public static boolean isValidMetricName(String name) {
        return validateMetricName(name) == null;
    }

    static String validateMetricName(String name) {
        switch (nameValidationScheme) {
            case LEGACY_VALIDATION:
                return validateLegacyMetricName(name);
            case UTF_8_VALIDATION:
                if(name.isEmpty() || !StandardCharsets.UTF_8.newEncoder().canEncode(name)) {
                    return "The metric name contains unsupported characters";
                }
                return null;
            default:
                throw new RuntimeException("Invalid name validation scheme requested: " + nameValidationScheme);
        }
    }

    /**
     * Same as {@link #isValidMetricName(String)}, but produces an error message.
     * <p>
     * The name is valid if the error message is {@code null}.
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
        switch (nameValidationScheme) {
            case LEGACY_VALIDATION:
                return LEGACY_METRIC_NAME_PATTERN.matcher(name).matches();
            case UTF_8_VALIDATION:
                return METRIC_NAME_PATTERN.matcher(name).matches();
            default:
                throw new RuntimeException("Invalid name validation scheme requested: " + nameValidationScheme);
        }
    }

    public static boolean isValidLabelName(String name) {
        switch (nameValidationScheme) {
            case LEGACY_VALIDATION:
                return isValidLegacyLabelName(name) &&
                        !(name.startsWith("__") || name.startsWith("._") || name.startsWith("..") || name.startsWith("_."));
            case UTF_8_VALIDATION:
                return StandardCharsets.UTF_8.newEncoder().canEncode(name);
            default:
                throw new RuntimeException("Invalid name validation scheme requested: " + nameValidationScheme);
        }
    }

    public static boolean isValidLegacyLabelName(String name) {
        switch (nameValidationScheme) {
            case LEGACY_VALIDATION:
                return LEGACY_LABEL_NAME_PATTERN.matcher(name).matches();
            case UTF_8_VALIDATION:
                return LABEL_NAME_PATTERN.matcher(name).matches();
            default:
                throw new RuntimeException("Invalid name validation scheme requested: " + nameValidationScheme);
        }
    }

    /**
     * Get the metric or label name that is used in Prometheus exposition format.
     *
     * @param name must be a valid metric or label name,
     *             i.e. {@link #isValidMetricName(String) isValidMetricName(name)}
     *             or {@link #isValidLabelName(String) isValidLabelName(name)}  must be true.
     * @return the name with dots replaced by underscores.
     */
    public static String prometheusName(String name) {
        switch (nameValidationScheme) {
            case LEGACY_VALIDATION:
                return name.replace(".", "_");
            case UTF_8_VALIDATION:
                return name;
            default:
                throw new RuntimeException("Invalid name validation scheme requested: " + nameValidationScheme);
        }
    }

    /**
     * Convert an arbitrary string to a name where {@link #isValidMetricName(String) isValidMetricName(name)} is true.
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
                    sanitizedName = sanitizedName.substring(0, sanitizedName.length() - reservedSuffix.length());
                    modified = true;
                }
            }
        }
        return sanitizedName;
    }

    /**
     * Convert an arbitrary string to a name where {@link #isValidLabelName(String) isValidLabelName(name)} is true.
     */
    public static String sanitizeLabelName(String labelName) {
        if (labelName.isEmpty()) {
            throw new IllegalArgumentException("Cannot convert an empty string to a valid label name.");
        }
        String sanitizedName = replaceIllegalCharsInLabelName(labelName);
        while (sanitizedName.startsWith("__") || sanitizedName.startsWith("_.") || sanitizedName.startsWith("._") || sanitizedName.startsWith("..")) {
            sanitizedName = sanitizedName.substring(1);
        }
        return sanitizedName;
    }

    /**
     * Returns a string that matches {@link #LEGACY_METRIC_NAME_PATTERN}.
     */
    private static String replaceIllegalCharsInMetricName(String name) {
        int length = name.length();
        char[] sanitized = new char[length];
        for (int i = 0; i < length; i++) {
            char ch = name.charAt(i);
            if (ch == ':' ||
                    ch == '.' ||
                    (ch >= 'a' && ch <= 'z') ||
                    (ch >= 'A' && ch <= 'Z') ||
                    (i > 0 && ch >= '0' && ch <= '9')) {
                sanitized[i] = ch;
            } else {
                sanitized[i] = '_';
            }
        }
        return new String(sanitized);
    }

    /**
     * Returns a string that matches {@link #LEGACY_LABEL_NAME_PATTERN}.
     */
    private static String replaceIllegalCharsInLabelName(String name) {
        int length = name.length();
        char[] sanitized = new char[length];
        for (int i = 0; i < length; i++) {
            char ch = name.charAt(i);
            if (ch == '.' ||
                    (ch >= 'a' && ch <= 'z') ||
                    (ch >= 'A' && ch <= 'Z') ||
                    (i > 0 && ch >= '0' && ch <= '9')) {
                sanitized[i] = ch;
            } else {
                sanitized[i] = '_';
            }
        }
        return new String(sanitized);
    }

    public static MetricSnapshot escapeMetricSnapshot(MetricSnapshot v, EscapingScheme scheme) {
        if (v == null) {
            return null;
        }

        if (scheme == EscapingScheme.NO_ESCAPING) {
            return v;
        }

        String outName;

        if (v.getMetadata().getPrometheusName() == null || isValidLegacyMetricName(v.getMetadata().getPrometheusName())) {
            outName = v.getMetadata().getPrometheusName();
        } else {
            outName = escapeName(v.getMetadata().getPrometheusName(), scheme);
        }

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
            DataPointSnapshot outDataPointSnapshot = null;

            if (v instanceof CounterSnapshot) {
                outDataPointSnapshot = CounterSnapshot.CounterDataPointSnapshot.builder()
                        .value(((CounterSnapshot.CounterDataPointSnapshot) d).getValue())
                        .exemplar(((CounterSnapshot.CounterDataPointSnapshot) d).getExemplar())
                        .labels(outLabels)
                        .createdTimestampMillis(d.getCreatedTimestampMillis())
                        .scrapeTimestampMillis(d.getScrapeTimestampMillis())
                        .build();
            } else if (v instanceof GaugeSnapshot) {
                outDataPointSnapshot = GaugeSnapshot.GaugeDataPointSnapshot.builder()
                        .value(((GaugeSnapshot.GaugeDataPointSnapshot) d).getValue())
                        .exemplar(((GaugeSnapshot.GaugeDataPointSnapshot) d).getExemplar())
                        .labels(outLabels)
                        .scrapeTimestampMillis(d.getScrapeTimestampMillis())
                        .build();
            } else if (v instanceof HistogramSnapshot) {
                outDataPointSnapshot = HistogramSnapshot.HistogramDataPointSnapshot.builder()
                        .classicHistogramBuckets(((HistogramSnapshot.HistogramDataPointSnapshot) d).getClassicBuckets())
                        .nativeSchema(((HistogramSnapshot.HistogramDataPointSnapshot) d).getNativeSchema())
                        .nativeZeroCount(((HistogramSnapshot.HistogramDataPointSnapshot) d).getNativeZeroCount())
                        .nativeZeroThreshold(((HistogramSnapshot.HistogramDataPointSnapshot) d).getNativeZeroThreshold())
                        .nativeBucketsForPositiveValues(((HistogramSnapshot.HistogramDataPointSnapshot) d).getNativeBucketsForPositiveValues())
                        .nativeBucketsForNegativeValues(((HistogramSnapshot.HistogramDataPointSnapshot) d).getNativeBucketsForNegativeValues())
                        .count(((HistogramSnapshot.HistogramDataPointSnapshot) d).getCount())
                        .sum(((HistogramSnapshot.HistogramDataPointSnapshot) d).getSum())
                        .exemplars(((HistogramSnapshot.HistogramDataPointSnapshot) d).getExemplars())
                        .labels(outLabels)
                        .createdTimestampMillis(d.getCreatedTimestampMillis())
                        .scrapeTimestampMillis(d.getScrapeTimestampMillis())
                        .build();
            } else if (v instanceof SummarySnapshot) {
                outDataPointSnapshot = SummarySnapshot.SummaryDataPointSnapshot.builder()
                        .quantiles(((SummarySnapshot.SummaryDataPointSnapshot) d).getQuantiles())
                        .count(((SummarySnapshot.SummaryDataPointSnapshot) d).getCount())
                        .sum(((SummarySnapshot.SummaryDataPointSnapshot) d).getSum())
                        .exemplars(((SummarySnapshot.SummaryDataPointSnapshot) d).getExemplars())
                        .labels(outLabels)
                        .createdTimestampMillis(d.getCreatedTimestampMillis())
                        .scrapeTimestampMillis(d.getScrapeTimestampMillis())
                        .build();
            } else if (v instanceof InfoSnapshot) {
                outDataPointSnapshot = InfoSnapshot.InfoDataPointSnapshot.builder()
                        .labels(outLabels)
                        .scrapeTimestampMillis(d.getScrapeTimestampMillis())
                        .build();
            } else if (v instanceof StateSetSnapshot) {
                StateSetSnapshot.StateSetDataPointSnapshot.Builder builder = StateSetSnapshot.StateSetDataPointSnapshot.builder()
                        .labels(outLabels)
                        .scrapeTimestampMillis(d.getScrapeTimestampMillis());
                for (StateSetSnapshot.State state : ((StateSetSnapshot.StateSetDataPointSnapshot) d)) {
                    builder.state(state.getName(), state.isTrue());
                }
                outDataPointSnapshot = builder.build();
            } else if (v instanceof UnknownSnapshot) {
                outDataPointSnapshot = UnknownSnapshot.UnknownDataPointSnapshot.builder()
                        .labels(outLabels)
                        .value(((UnknownSnapshot.UnknownDataPointSnapshot) d).getValue())
                        .exemplar(((UnknownSnapshot.UnknownDataPointSnapshot) d).getExemplar())
                        .scrapeTimestampMillis(d.getScrapeTimestampMillis())
                        .build();
            }

            outDataPoints.add(outDataPointSnapshot);
        }

        MetricSnapshot out;

        if (v instanceof CounterSnapshot) {
            CounterSnapshot.Builder builder = CounterSnapshot.builder()
                    .name(outName)
                    .help(v.getMetadata().getHelp())
                    .unit(v.getMetadata().getUnit());
            for (DataPointSnapshot d : outDataPoints) {
                builder.dataPoint((CounterSnapshot.CounterDataPointSnapshot) d);
            }
            out = builder.build();
        } else if (v instanceof GaugeSnapshot) {
            GaugeSnapshot.Builder builder = GaugeSnapshot.builder()
                    .name(outName)
                    .help(v.getMetadata().getHelp())
                    .unit(v.getMetadata().getUnit());
            for (DataPointSnapshot d : outDataPoints) {
                builder.dataPoint((GaugeSnapshot.GaugeDataPointSnapshot) d);
            }
            out = builder.build();
        } else if (v instanceof HistogramSnapshot) {
            HistogramSnapshot.Builder builder = HistogramSnapshot.builder()
                    .name(outName)
                    .help(v.getMetadata().getHelp())
                    .unit(v.getMetadata().getUnit())
                    .gaugeHistogram(((HistogramSnapshot) v).isGaugeHistogram());
            for (DataPointSnapshot d : outDataPoints) {
                builder.dataPoint((HistogramSnapshot.HistogramDataPointSnapshot) d);
            }
            out = builder.build();
        } else if (v instanceof SummarySnapshot) {
            SummarySnapshot.Builder builder = SummarySnapshot.builder()
                    .name(outName)
                    .help(v.getMetadata().getHelp())
                    .unit(v.getMetadata().getUnit());
            for (DataPointSnapshot d : outDataPoints) {
                builder.dataPoint((SummarySnapshot.SummaryDataPointSnapshot) d);
            }
            out = builder.build();
        } else if (v instanceof InfoSnapshot) {
            InfoSnapshot.Builder builder = InfoSnapshot.builder()
                    .name(outName)
                    .help(v.getMetadata().getHelp());
            for (DataPointSnapshot d : outDataPoints) {
                builder.dataPoint((InfoSnapshot.InfoDataPointSnapshot) d);
            }
            out = builder.build();
        } else if (v instanceof StateSetSnapshot) {
            StateSetSnapshot.Builder builder = StateSetSnapshot.builder()
                    .name(outName)
                    .help(v.getMetadata().getHelp());
            for (DataPointSnapshot d : outDataPoints) {
                builder.dataPoint((StateSetSnapshot.StateSetDataPointSnapshot) d);
            }
            out = builder.build();
        } else if (v instanceof UnknownSnapshot) {
            UnknownSnapshot.Builder builder = UnknownSnapshot.builder()
                    .name(outName)
                    .help(v.getMetadata().getHelp())
                    .unit(v.getMetadata().getUnit());
            for (DataPointSnapshot d : outDataPoints) {
                builder.dataPoint((UnknownSnapshot.UnknownDataPointSnapshot) d);
            }
            out = builder.build();
        } else {
            throw new IllegalArgumentException("Unknown MetricSnapshot type: " + v.getClass());
        }

        return out;
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

    static String escapeName(String name, EscapingScheme scheme) {
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
                for (int i = 0; i < name.length(); i++) {
                    char c = name.charAt(i);
                    if (isValidLegacyChar(c, i)) {
                        escaped.append(c);
                    } else {
                        escaped.append('_');
                    }
                }
                return escaped.toString();
            case DOTS_ESCAPING:
                for (int i = 0; i < name.length(); i++) {
                    char c = name.charAt(i);
                    if (c == '_') {
                        escaped.append("__");
                    } else if (c == '.') {
                        escaped.append("_dot_");
                    } else if (isValidLegacyChar(c, i)) {
                        escaped.append(c);
                    } else {
                        escaped.append('_');
                    }
                }
                return escaped.toString();
            case VALUE_ENCODING_ESCAPING:
                if (isValidLegacyMetricName(name)) {
                    return name;
                }
                escaped.append("U__");
                for (int i = 0; i < name.length(); i++) {
                    char c = name.charAt(i);
                    if (isValidLegacyChar(c, i)) {
                        escaped.append(c);
                    } else if (!isValidUTF8Char(c)) {
                        escaped.append("_FFFD_");
                    } else if (c < 0x100) {
                        // TODO Check if this is ok
                        escaped.append('_');
                        for (int s = 4; s >= 0; s -= 4) {
                            escaped.append(LOWERHEX.charAt((c >> s) & 0xF));
                        }
                        escaped.append('_');
                    } else {
                        escaped.append('_');
                        for (int s = 12; s >= 0; s -= 4) {
                            escaped.append(LOWERHEX.charAt((c >> s) & 0xF));
                        }
                        escaped.append('_');
                    }
                }
                return escaped.toString();
            default:
                throw new IllegalArgumentException("Invalid escaping scheme " + scheme);
        }
    }

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
                // TODO Check if this is ok
                Matcher matcher = Pattern.compile("U__").matcher(name);
                if (matcher.find()) {
                    String escapedName = name.substring(matcher.end());
                    StringBuilder unescaped = new StringBuilder();
                    TOP:
                    for (int i = 0; i < escapedName.length(); i++) {
                        // All non-underscores are treated normally.
                        if (escapedName.charAt(i) != '_') {
                            unescaped.append(escapedName.charAt(i));
                            continue;
                        }
                        i++;
                        if (i >= escapedName.length()) {
                            return name;
                        }
                        // A double underscore is a single underscore.
                        if (escapedName.charAt(i) == '_') {
                            unescaped.append('_');
                            continue;
                        }
                        // We think we are in a UTF-8 code, process it.
                        long utf8Val = 0;
                        for (int j = 0; i < escapedName.length(); j++) {
                            // This is too many characters for a UTF-8 value.
                            if (j > 4) {
                                return name;
                            }
                            // Found a closing underscore, convert to a char, check validity, and append.
                            if (escapedName.charAt(i) == '_') {
                                char utf8Char = (char) utf8Val;
                                if (!isValidUTF8Char(utf8Char)) {
                                    return name;
                                }
                                unescaped.append(utf8Char);
                                continue TOP;
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
                        // Didn't find closing underscore, invalid.
                        return name;
                    }
                    return unescaped.toString();
                } else {
                    return name;
                }
            default:
                throw new IllegalArgumentException("Invalid escaping scheme " + scheme);
        }
    }

    static boolean isValidLegacyChar(char c, int i) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == ':' || (c >= '0' && c <= '9' && i > 0);
    }

    private static boolean isValidUTF8Char(char b) {
        return ((b < MIN_HIGH_SURROGATE || b > MAX_LOW_SURROGATE) &&
                (b < 0xFFFE));
    }
}
