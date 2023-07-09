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

    public static final String NAME_MUST_BE_EQUAL_TO = "nameMustBeEqualTo";
    public static final String NAME_MUST_NOT_BE_EQUAL_TO = "nameMustNotBeEqualTo";
    public static final String NAME_MUST_START_WITH = "nameMustStartWith";
    public static final String NAME_MUST_NOT_START_WITH = "nameMustNotStartWith";

    private final List<String> allowedNames;
    private final List<String> excludedNames;
    private final List<String> allowedPrefixes;
    private final List<String> excludedPrefixes;

    public ExporterFilterProperties(List<String> allowedNames, List<String> excludedNames, List<String> allowedPrefixes, List<String> excludedPrefixes) {
        this(allowedNames, excludedNames, allowedPrefixes, excludedPrefixes, "");
    }

    private ExporterFilterProperties(List<String> allowedNames, List<String> excludedNames, List<String> allowedPrefixes, List<String> excludedPrefixes, String prefix) {
        this.allowedNames = allowedNames == null ? null : Collections.unmodifiableList(new ArrayList<>(allowedNames));
        this.excludedNames = excludedNames == null ? null : Collections.unmodifiableList(new ArrayList<>(excludedNames));
        this.allowedPrefixes = allowedPrefixes == null ? null : Collections.unmodifiableList(new ArrayList<>(allowedPrefixes));
        this.excludedPrefixes = excludedPrefixes == null ? null : Collections.unmodifiableList(new ArrayList<>(excludedPrefixes));
        validate(prefix);
    }

    public List<String> getAllowedNames() {
        return allowedNames;
    }

    public List<String> getExcludedNames() {
        return excludedNames;
    }

    public List<String> getAllowedPrefixes() {
        return allowedPrefixes;
    }

    public List<String> getExcludedPrefixes() {
        return excludedPrefixes;
    }

    private void validate(String prefix) throws PrometheusPropertiesException {
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private List<String> allowedNames;
        private List<String> excludedNames;
        private List<String> allowedPrefixes;
        private List<String> excludedPrefixes;

        private Builder() {
        }

        public Builder withAllowedNames(String... allowedNames) {
            this.allowedNames = Arrays.asList(allowedNames);
            return this;
        }

        public Builder withExcludedNames(String... excludedNames) {
            this.excludedNames = Arrays.asList(excludedNames);
            return this;
        }

        public Builder withAllowedPrefixes(String... allowedPrefixes) {
            this.allowedPrefixes = Arrays.asList(allowedPrefixes);
            return this;
        }

        public Builder withExcludedPrefixes(String... excludedPrefixes) {
            this.excludedPrefixes = Arrays.asList(excludedPrefixes);
            return this;
        }

        public ExporterFilterProperties build() {
            return new ExporterFilterProperties(allowedNames, excludedNames, allowedPrefixes, excludedPrefixes);
        }
    }

    static ExporterFilterProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        List<String> allowedNames = Util.loadStringList(prefix + "." + NAME_MUST_BE_EQUAL_TO, properties);
        List<String> excludedNames = Util.loadStringList(prefix + "." + NAME_MUST_NOT_BE_EQUAL_TO, properties);
        List<String> allowedPrefixes = Util.loadStringList(prefix + "." + NAME_MUST_START_WITH, properties);
        List<String> excludedPrefixes = Util.loadStringList(prefix + "." + NAME_MUST_NOT_START_WITH, properties);
        return new ExporterFilterProperties(allowedNames, excludedNames, allowedPrefixes, excludedPrefixes, prefix);
    }
}
