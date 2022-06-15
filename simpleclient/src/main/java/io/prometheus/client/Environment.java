package io.prometheus.client;

import java.util.Arrays;
import java.util.List;

class Environment {

    private static final String DISABLE_CREATED_SERIES = "PROMETHEUS_DISABLE_CREATED_SERIES";
    private static final List<String> DISABLE_CREATED_SERIES_TRUE = Arrays.asList("true", "1", "t");
    private static final boolean includeCreatedSeries = !isTrue(DISABLE_CREATED_SERIES);

    static boolean includeCreatedSeries() {
        return includeCreatedSeries;
    }

    private static boolean isTrue(String envVarName) {
        String stringValue = System.getenv(envVarName);
        if (stringValue != null) {
            return DISABLE_CREATED_SERIES_TRUE.contains(stringValue.toLowerCase());
        }
        return false;
    }
}
