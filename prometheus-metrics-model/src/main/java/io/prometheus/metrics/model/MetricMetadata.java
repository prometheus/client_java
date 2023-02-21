package io.prometheus.metrics.model;

import java.util.regex.Pattern;

public final class MetricMetadata {
    private static final Pattern METRIC_NAME_RE = Pattern.compile("^[a-zA-Z_:][a-zA-Z0-9_:]+$");
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
     *         the {@code _count}, {@code _sum}, {@code _bucket}, or {@code _created} suffixes for fixed histograms,
     *         the {@code _gcount}, {@code _gsum}, {@code _bucket} suffixes for fixed gauge histograms,
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

    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

    public Unit getUnit() {
        return unit;
    }

    private void validate() {
        if (name == null) {
            throw new NullPointerException("name is required");
        }
        if (!isValidMetricName(name)) {
            throw new IllegalArgumentException("'" + name + "': illegal metric name");
        }
    }

    /**
     * If this is false, the name cannot be used as a metric name.
     */
    public static boolean isValidMetricName(String name) {
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
        return new String(sanitized);
    }
}
