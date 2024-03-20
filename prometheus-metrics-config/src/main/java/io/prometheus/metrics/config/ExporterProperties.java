package io.prometheus.metrics.config;

import java.util.Map;

/**
 * Properties starting with io.prometheus.exporter
 */
public class ExporterProperties {

    private static final String INCLUDE_CREATED_TIMESTAMPS = "includeCreatedTimestamps";
    private static final String EXEMPLARS_ON_ALL_METRIC_TYPES = "exemplarsOnAllMetricTypes";

    private final Boolean includeCreatedTimestamps;
    private final Boolean exemplarsOnAllMetricTypes;

    private ExporterProperties(Boolean includeCreatedTimestamps, Boolean exemplarsOnAllMetricTypes) {
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
     * Default is {@code false}, which means Exemplars will only be added for Counters and Histogram buckets.
     */
    public boolean getExemplarsOnAllMetricTypes() {
        return exemplarsOnAllMetricTypes != null && exemplarsOnAllMetricTypes;
    }

    /**
     * Note that this will remove entries from {@code properties}.
     * This is because we want to know if there are unused properties remaining after all properties have been loaded.
     */
    static ExporterProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        Boolean includeCreatedTimestamps = Util.loadBoolean(prefix + "." + INCLUDE_CREATED_TIMESTAMPS, properties);
        Boolean exemplarsOnAllMetricTypes = Util.loadBoolean(prefix + "." + EXEMPLARS_ON_ALL_METRIC_TYPES, properties);
        return new ExporterProperties(includeCreatedTimestamps, exemplarsOnAllMetricTypes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Boolean includeCreatedTimestamps;
        private Boolean exemplarsOnAllMetricTypes;

        private Builder() {
        }

        /**
         * See {@link #getIncludeCreatedTimestamps()}
         */
        public Builder includeCreatedTimestamps(boolean includeCreatedTimestamps) {
            this.includeCreatedTimestamps = includeCreatedTimestamps;
            return this;
        }

        /**
         * See {@link #getExemplarsOnAllMetricTypes()}.
         */
        public Builder exemplarsOnAllMetricTypes(boolean exemplarsOnAllMetricTypes) {
            this.exemplarsOnAllMetricTypes = exemplarsOnAllMetricTypes;
            return this;
        }

        public ExporterProperties build() {
            return new ExporterProperties(includeCreatedTimestamps, exemplarsOnAllMetricTypes);
        }
    }
}
