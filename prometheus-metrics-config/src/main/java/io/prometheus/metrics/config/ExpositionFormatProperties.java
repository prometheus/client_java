package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.expositionFormat
 */
public class ExpositionFormatProperties {

    private static final String INCLUDE_CREATED_TIMESTAMPS = "includeCreatedTimestamps";

    private final Boolean includeCreatedTimestamps;

    public ExpositionFormatProperties(Boolean includeCreatedTimestamps) {
        this.includeCreatedTimestamps = includeCreatedTimestamps;
    }

    public Boolean getIncludeCreatedTimestamps() {
        return includeCreatedTimestamps;
    }

    static ExpositionFormatProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        Boolean includeCreatedTimestamps = Util.loadBoolean(prefix + "." + INCLUDE_CREATED_TIMESTAMPS, properties);
        return new ExpositionFormatProperties(includeCreatedTimestamps);
    }
}
