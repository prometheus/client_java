package io.prometheus.metrics.model;

import java.util.Collection;
import java.util.regex.Pattern;

public final class MetricMetadata {
    private static final Pattern METRIC_NAME_RE = Pattern.compile("^[a-zA-Z_:][a-zA-Z0-9_:]+$");
    private final String name;
    private final String help;
    private final MetricType type;
    private final String unit;

    public MetricMetadata(String name, String help, MetricType type, String unit) {
        this.name = name;
        this.help = help;
        this.type = type;
        this.unit = unit;
        validate();
    }

    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

    public MetricType getType() {
        return type;
    }

    public String getUnit() {
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

    public static boolean isValidMetricName(String name) {
        return METRIC_NAME_RE.matcher(name).matches();
    }

    /**
     * Convert arbitrary metric names to valid Prometheus metric names.
     */
    public static String sanitizeMetricName(String metricName) {
        int length = metricName.length();
        char[] sanitized = new char[length];
        for(int i = 0; i < length; i++) {
            char ch = metricName.charAt(i);
            if(ch == ':' ||
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
