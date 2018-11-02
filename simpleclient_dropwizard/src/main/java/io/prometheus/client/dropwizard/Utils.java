package io.prometheus.client.dropwizard;

import java.util.regex.Pattern;

public class Utils {
    private static final Pattern METRIC_NAME_RE = Pattern.compile("[^a-zA-Z0-9:_]");

    /**
     * Replace all unsupported chars with '_', prepend '_' if name starts with digit.
     *
     * @param dropwizardName original metric name.
     * @return the sanitized metric name.
     */
    public static String sanitizeMetricName(String dropwizardName) {
        String name = METRIC_NAME_RE.matcher(dropwizardName).replaceAll("_");
        if (!name.isEmpty() && Character.isDigit(name.charAt(0))) {
            name = "_" + name;
        }
        return name;
    }
}
