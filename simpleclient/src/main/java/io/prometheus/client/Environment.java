package io.prometheus.client;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Environment {

    private static final String DISABLE_CREATED_SERIES_ENVIRONMENT_VARIABLE = "PROMETHEUS_DISABLE_CREATED_SERIES";
    private static final String DISABLE_CREATED_SERIES_SYSTEM_PROPERTY = "prometheus.disable.created.series";

    private static final List<String> DISABLE_CREATED_SERIES_TRUE = Arrays.asList("true", "1", "t");

    private Environment() {
        // DO NOTHING
    }

    private static AtomicBoolean includeCreatedSeries =
            new AtomicBoolean(
                    !isTrue(DISABLE_CREATED_SERIES_SYSTEM_PROPERTY, DISABLE_CREATED_SERIES_ENVIRONMENT_VARIABLE));

    public static boolean includeCreatedSeries() {
        return includeCreatedSeries.get();
    }

    public static void enabledCreatedSeries() {
        includeCreatedSeries.set(true);
    }

    public static void disableCreatedSeries() {
        includeCreatedSeries.set(false);
    }

    private static boolean isTrue(String systemPropertyName, String environmentVariableName) {
        String stringValue = System.getProperty(systemPropertyName);
        if (stringValue != null) {
            return DISABLE_CREATED_SERIES_TRUE.contains(stringValue.toLowerCase());
        }

        stringValue = System.getenv(environmentVariableName);
        if (stringValue != null) {
            return DISABLE_CREATED_SERIES_TRUE.contains(stringValue.toLowerCase());
        }

        return false;
    }
}
