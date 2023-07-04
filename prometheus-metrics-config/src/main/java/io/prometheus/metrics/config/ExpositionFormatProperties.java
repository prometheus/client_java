package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.expositionFormat
 */
public class ExpositionFormatProperties {

    private static final String INCLUDE_CREATED_TIMESTAMPS = "includeCreatedTimestamps";
    private static final String EXEMPLARS_ON_ALL_METRIC_TYPES = "exemplarsOnAllMetricTypes";

    private final Boolean includeCreatedTimestamps;
    private final Boolean exemplarsOnAllMetricTypes;

    public ExpositionFormatProperties(Boolean includeCreatedTimestamps, Boolean exemplarsOnAllMetricTypes) {
        this.includeCreatedTimestamps = includeCreatedTimestamps;
        this.exemplarsOnAllMetricTypes = exemplarsOnAllMetricTypes;
    }

    /**
     * Include the {@code _created} timestamps in text format? Default is {@code false}.
     */
    public boolean getIncludeCreatedTimestamps() {
        return includeCreatedTimestamps != null && includeCreatedTimestamps;
    }

    /**
     * Allow Exemplars on all metric types in OpenMetrics format?
     * Default is {@code false}, which means Exemplars will only be added for Counters and Histograms.
     */
    public boolean getExemplarsOnAllMetricTypes() {
        return exemplarsOnAllMetricTypes != null && exemplarsOnAllMetricTypes;
    }

    static ExpositionFormatProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        Boolean includeCreatedTimestamps = Util.loadBoolean(prefix + "." + INCLUDE_CREATED_TIMESTAMPS, properties);
        Boolean exemplarsOnAllMetricTypes = Util.loadBoolean(prefix + "." + EXEMPLARS_ON_ALL_METRIC_TYPES, properties);
        return new ExpositionFormatProperties(includeCreatedTimestamps, exemplarsOnAllMetricTypes);
    }
}
