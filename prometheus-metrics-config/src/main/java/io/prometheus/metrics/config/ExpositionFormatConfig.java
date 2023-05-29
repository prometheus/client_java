package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.expositionFormat
 */
public class ExpositionFormatConfig {

    private static final String INCLUDE_CREATED_TIMESTAMPS = "includeCreatedTimestamps";

    private final Boolean includeCreatedTimestamps;

    public ExpositionFormatConfig(Boolean includeCreatedTimestamps) {
        this.includeCreatedTimestamps = includeCreatedTimestamps;
    }

    public Boolean getIncludeCreatedTimestamps() {
        return includeCreatedTimestamps;
    }

    static ExpositionFormatConfig load(String prefix, Map<Object, Object> properties) throws PrometheusConfigException {
        Boolean includeCreatedTimestamps = Util.loadBoolean(prefix + "." + INCLUDE_CREATED_TIMESTAMPS, properties);
        return new ExpositionFormatConfig(includeCreatedTimestamps);
    }
}
