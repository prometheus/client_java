package io.prometheus.metrics.model;

import java.util.regex.Pattern;

public final class MetricMetadata {
    private static final Pattern METRIC_NAME_RE = Pattern.compile("^[a-zA-Z_:][a-zA-Z0-9_:]+$");
    private static final String[] RESERVED_SUFFIXES = {"_total", "_created", "_count", "_sum", "_bucket", "_gcount", "gsum", "_info"};

    /**
     * Name is the name without suffix. For example, the name for a counter "http_requests_total" would be "http_requests".
     * The name of a info called "target_info" is "target".
     */
    private final String name;
    private final String help;
    private final Unit unit;

    /**
     * See {@link #MetricMetadata(String, String, Unit)}
     */
    public MetricMetadata(String name) {
        this(name, null, null);
    }

    /**
     * See {@link #MetricMetadata(String, String, Unit)}
     */
    public MetricMetadata(String name, String help) {
        this(name, help, null);
    }

    /**
     * Naming conventions:
     * <ul>
     *     <li>The name MUST NOT include the {@code _total} suffix for counter metrics or the
     *         {@code _info} suffix for info metrics.
     *         If in doubt, use {@link #sanitizeMetricName(String)} to remove these prefixes.</li>
     *     <li>If name is {@code null}, a {@link NullPointerException} is thrown.</li>
     *     <li>If {@link #isValidMetricName(String) isValidMetricName(name)} is false,
     *         an {@link IllegalArgumentException} is thrown.
     *         Use {@link #sanitizeMetricName(String)} to convert arbitrary Strings to valid names.</li>
     *     <li>If unit != null, the name SHOULD contain the unit as a suffix. Example:
     *         <pre>
     *         new MetricMetadata("cache_size_bytes", "current size of the cache", Unit.BYTES);
     *         </pre>
     *         This is not enforced. No Exception will be thrown if the name does not have the unit as suffix.</li>
     *     <li>The name MUST NOT contain the {@code _total} or {@code _created} suffixes for counters,
     *         the {@code _count}, {@code _sum}, or {@code _created} suffixes for summaries,
     *         the {@code _count}, {@code _sum}, {@code _bucket}, or {@code _created} suffixes for classic histograms,
     *         the {@code _gcount}, {@code _gsum}, {@code _bucket} suffixes for classic gauge histograms,
     *         or the {@code _info} suffix for info metrics.</li>
     * </ul>
     *
     * @param name must follow the naming conventions described above.
     * @param help optional. May be {@code null}.
     * @param unit optional. May be {@code null}.
     */
    public MetricMetadata(String name, String help, Unit unit) {
        this.name = name;
        this.help = help;
        this.unit = unit;
        validate();
    }

    /**
     * The name does not include the {@code _total} suffix for counter metrics
     * or the {@code _info} suffix for Info metrics.
     */
    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

    public boolean hasUnit() {
        return unit != null;
    }

    public Unit getUnit() {
        return unit;
    }

    private void validate() {
        if (name == null) {
            throw new IllegalArgumentException("Missing required field: name is null");
        }
        if (!isValidMetricName(name)) {
            throw new IllegalArgumentException("'" + name + "': illegal metric name");
        }
    }

    /**
     * Test if a metric name is valid. Rules:
     * <ul>
     * <li>The name must match {@link #METRIC_NAME_RE}.</li>
     * <li>The name MUST NOT end with one of the {@link #RESERVED_SUFFIXES}.</li>
     * </ul>
     * If a metric has a {@link Unit}, the metric name SHOULD end with the unit as a suffix (note that in
     * <a href="https://openmetrics.io/">OpenMetrics</a> this is a MUST, but this library does not enforce Unit
     * suffixes).
     * <p>
     * Example: If you create a Counter for a processing time with {@link Unit#SECONDS}, the name should be
     * {@code processing_time_seconds}. When exposed in OpenMetrics Text format, this will be represented as two
     * values: {@code processing_time_seconds_total} for the counter value, and the optional
     * {@code processing_time_seconds_created} timestamp.
     * <p>
     * Use {@link #sanitizeMetricName(String)} to convert arbitrary Strings to valid metric names.
     */
    public static boolean isValidMetricName(String name) {
        for (String reservedSuffix : RESERVED_SUFFIXES) {
            if (name.endsWith(reservedSuffix)) {
                return false;
            }
        }
        return METRIC_NAME_RE.matcher(name).matches();
    }

    /**
     * Convert arbitrary metric names to valid Prometheus metric names.
     */
    public static String sanitizeMetricName(String metricName) {
        if (metricName.isEmpty()) {
            throw new IllegalArgumentException("Cannot convert an empty string into a valid metric name.");
        }
        int length = metricName.length();
        char[] sanitized = new char[length];
        for (int i = 0; i < length; i++) {
            char ch = metricName.charAt(i);
            if (ch == ':' ||
                    (ch >= 'a' && ch <= 'z') ||
                    (ch >= 'A' && ch <= 'Z') ||
                    (i > 0 && ch >= '0' && ch <= '9')) {
                sanitized[i] = ch;
            } else {
                sanitized[i] = '_';
            }
        }
        String sanitizedString = new String(sanitized);
        boolean modified = true;
        while (modified) {
            modified = false;
            for (String reservedSuffix : RESERVED_SUFFIXES) {
                if (sanitizedString.equals(reservedSuffix)) {
                    // This is for the corner case when you call sanitizeMetricName("_total").
                    // In that case the result will be "total".
                    return reservedSuffix.substring(1);
                }
                if (sanitizedString.endsWith(reservedSuffix)) {
                    sanitizedString = sanitizedString.substring(0, sanitizedString.length() - reservedSuffix.length());
                    modified = true;
                }
            }
        }
        return sanitizedString;
    }
}
