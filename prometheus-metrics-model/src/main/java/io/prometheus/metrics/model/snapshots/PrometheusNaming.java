package io.prometheus.metrics.model.snapshots;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Utility for Prometheus Metric and Label naming.
 * <p>
 * Note that this library allows dots in metric and label names. Dots will automatically be replaced with underscores
 * in Prometheus exposition formats. However, if metrics are exposed in OpenTelemetry format the dots are retained.
 */
public class PrometheusNaming {
    public static ValidationScheme nameValidationScheme = ValidationScheme.LegacyValidation;

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
            case LegacyValidation:
                return validateLegacyMetricName(name);
            case UTF8Validation:
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
            case LegacyValidation:
                return LEGACY_METRIC_NAME_PATTERN.matcher(name).matches();
            case UTF8Validation:
                return METRIC_NAME_PATTERN.matcher(name).matches();
            default:
                throw new RuntimeException("Invalid name validation scheme requested: " + nameValidationScheme);
        }
    }

    public static boolean isValidLabelName(String name) {
        switch (nameValidationScheme) {
            case LegacyValidation:
                return isValidLegacyLabelName(name) &&
                        !(name.startsWith("__") || name.startsWith("._") || name.startsWith("..") || name.startsWith("_."));
            case UTF8Validation:
                return StandardCharsets.UTF_8.newEncoder().canEncode(name);
            default:
                throw new RuntimeException("Invalid name validation scheme requested: " + nameValidationScheme);
        }
    }

    public static boolean isValidLegacyLabelName(String name) {
        switch (nameValidationScheme) {
            case LegacyValidation:
                return LEGACY_LABEL_NAME_PATTERN.matcher(name).matches();
            case UTF8Validation:
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
            case LegacyValidation:
                return name.replace(".", "_");
            case UTF8Validation:
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
}
