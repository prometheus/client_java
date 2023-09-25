package io.prometheus.metrics.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Properties starting with io.prometheus.exporter.filter
 */
public class ExporterFilterProperties {

    public static final String METRIC_NAME_MUST_BE_EQUAL_TO = "metricNameMustBeEqualTo";
    public static final String METRIC_NAME_MUST_NOT_BE_EQUAL_TO = "metricNameMustNotBeEqualTo";
    public static final String METRIC_NAME_MUST_START_WITH = "metricNameMustStartWith";
    public static final String METRIC_NAME_MUST_NOT_START_WITH = "metricNameMustNotStartWith";

    private final List<String> allowedNames;
    private final List<String> excludedNames;
    private final List<String> allowedPrefixes;
    private final List<String> excludedPrefixes;

    private ExporterFilterProperties(List<String> allowedNames, List<String> excludedNames, List<String> allowedPrefixes, List<String> excludedPrefixes) {
        this(allowedNames, excludedNames, allowedPrefixes, excludedPrefixes, "");
    }

    private ExporterFilterProperties(List<String> allowedNames, List<String> excludedNames, List<String> allowedPrefixes, List<String> excludedPrefixes, String prefix) {
        this.allowedNames = allowedNames == null ? null : Collections.unmodifiableList(new ArrayList<>(allowedNames));
        this.excludedNames = excludedNames == null ? null : Collections.unmodifiableList(new ArrayList<>(excludedNames));
        this.allowedPrefixes = allowedPrefixes == null ? null : Collections.unmodifiableList(new ArrayList<>(allowedPrefixes));
        this.excludedPrefixes = excludedPrefixes == null ? null : Collections.unmodifiableList(new ArrayList<>(excludedPrefixes));
        validate(prefix);
    }

    public List<String> getAllowedMetricNames() {
        return allowedNames;
    }

    public List<String> getExcludedMetricNames() {
        return excludedNames;
    }

    public List<String> getAllowedMetricNamePrefixes() {
        return allowedPrefixes;
    }

    public List<String> getExcludedMetricNamePrefixes() {
        return excludedPrefixes;
    }

    private void validate(String prefix) throws PrometheusPropertiesException {
    }

    /**
     * Note that this will remove entries from {@code properties}.
     * This is because we want to know if there are unused properties remaining after all properties have been loaded.
     */
    static ExporterFilterProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        List<String> allowedNames = Util.loadStringList(prefix + "." + METRIC_NAME_MUST_BE_EQUAL_TO, properties);
        List<String> excludedNames = Util.loadStringList(prefix + "." + METRIC_NAME_MUST_NOT_BE_EQUAL_TO, properties);
        List<String> allowedPrefixes = Util.loadStringList(prefix + "." + METRIC_NAME_MUST_START_WITH, properties);
        List<String> excludedPrefixes = Util.loadStringList(prefix + "." + METRIC_NAME_MUST_NOT_START_WITH, properties);
        return new ExporterFilterProperties(allowedNames, excludedNames, allowedPrefixes, excludedPrefixes, prefix);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<String> allowedNames;
        private List<String> excludedNames;
        private List<String> allowedPrefixes;
        private List<String> excludedPrefixes;

        private Builder() {
        }

        /**
         * Only allowed metric names will be exposed.
         */
        public Builder allowedNames(String... allowedNames) {
            this.allowedNames = Arrays.asList(allowedNames);
            return this;
        }

        /**
         * Excluded metric names will not be exposed.
         */
        public Builder excludedNames(String... excludedNames) {
            this.excludedNames = Arrays.asList(excludedNames);
            return this;
        }

        /**
         * Only metrics with a name starting with an allowed prefix will be exposed.
         */
        public Builder allowedPrefixes(String... allowedPrefixes) {
            this.allowedPrefixes = Arrays.asList(allowedPrefixes);
            return this;
        }

        /**
         * Metrics with a name starting with an excluded prefix will not be exposed.
         */
        public Builder excludedPrefixes(String... excludedPrefixes) {
            this.excludedPrefixes = Arrays.asList(excludedPrefixes);
            return this;
        }

        public ExporterFilterProperties build() {
            return new ExporterFilterProperties(allowedNames, excludedNames, allowedPrefixes, excludedPrefixes);
        }
    }
}
