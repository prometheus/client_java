package io.prometheus.metrics.instrumentation.dropwizard5.labels;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * POJO containing info on how to map a graphite metric to a prometheus one.
 * Example mapping in yaml format:
 * <p>
 * match: test.dispatcher.*.*.*
 * name: dispatcher_events_total
 * labels:
 * action: ${1}
 * outcome: ${2}_out
 * processor: ${0}
 * status: ${1}_${2}
 * <p>
 * Dropwizard metrics that match the "match" pattern will be further processed to have a new name and new labels based on this config.
 */
public class MapperConfig {
    // each part of the metric name between dots
    private static final String METRIC_PART_REGEX = "[a-zA-Z_0-9](-?[a-zA-Z0-9_])+";
    // Simplified GLOB: we can have "*." at the beginning and "*" only at the end
    static final String METRIC_GLOB_REGEX = "^(\\*\\.|" + METRIC_PART_REGEX + "\\.)+(\\*|" + METRIC_PART_REGEX + ")$";
    // Labels validation.
    private static final String LABEL_REGEX = "^[a-zA-Z_][a-zA-Z0-9_]+$";
    private static final Pattern MATCH_EXPRESSION_PATTERN = Pattern.compile(METRIC_GLOB_REGEX);
    private static final Pattern LABEL_PATTERN = Pattern.compile(LABEL_REGEX);

    /**
     * Regex used to match incoming metric name.
     * Uses a simplified glob syntax where only '*' are allowed.
     * E.g:
     * org.company.controller.*.status.*
     * Will be used to match
     * org.company.controller.controller1.status.200
     * and
     * org.company.controller.controller2.status.400
     */
    private String match;

    /**
     * New metric name. Can contain placeholders to be replaced with actual values from the incoming metric name.
     * Placeholders are in the ${n} format where n is the zero based index of the group to extract from the original metric name.
     * E.g.:
     * match: test.dispatcher.*.*.*
     * name: dispatcher_events_total_${1}
     * <p>
     * A metric "test.dispatcher.old.test.yay" will be converted in a new metric with name "dispatcher_events_total_test"
     */
    private String name;

    /**
     * Labels to be extracted from the metric name.
     * They should contain placeholders to be replaced with actual values from the incoming metric name.
     * Placeholders are in the ${n} format where n is the zero based index of the group to extract from the original metric name.
     * E.g.:
     * match: test.dispatcher.*.*
     * name: dispatcher_events_total_${0}
     * labels:
     * label1: ${1}_t
     * <p>
     * A metric "test.dispatcher.sp1.yay" will be converted in a new metric with name "dispatcher_events_total_sp1" with label {label1: yay_t}
     * <p>
     * Label names have to match the regex ^[a-zA-Z_][a-zA-Z0-9_]+$
     */

    private Map<String, String> labels = new HashMap<String, String>();

    public MapperConfig() {
        // empty constructor
    }

    // for tests
    MapperConfig(final String match) {
        validateMatch(match);
        this.match = match;
    }

    public MapperConfig(final String match, final String name, final Map<String, String> labels) {
        this.name = name;
        validateMatch(match);
        this.match = match;
        validateLabels(labels);
        this.labels = labels;
    }

    @Override
    public String toString() {
        return String.format("MapperConfig{match=%s, name=%s, labels=%s}", match, name, labels);
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(final String match) {
        validateMatch(match);
        this.match = match;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;

    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(final Map<String, String> labels) {
        validateLabels(labels);
        this.labels = labels;
    }

    private void validateMatch(final String match)
    {
        if (!MATCH_EXPRESSION_PATTERN.matcher(match).matches()) {
            throw new IllegalArgumentException(String.format("Match expression [%s] does not match required pattern %s", match, MATCH_EXPRESSION_PATTERN));
        }
    }

    private void validateLabels(final Map<String, String> labels)
    {
        if (labels != null) {
            for (final String key : labels.keySet()) {
                if (!LABEL_PATTERN.matcher(key).matches()) {
                    throw new IllegalArgumentException(String.format("Label [%s] does not match required pattern %s", match, LABEL_PATTERN));
                }
            }

        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final MapperConfig that = (MapperConfig) o;

        if (match != null ? !match.equals(that.match) : that.match != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return labels != null ? labels.equals(that.labels) : that.labels == null;
    }

    @Override
    public int hashCode() {
        int result = match != null ? match.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (labels != null ? labels.hashCode() : 0);
        return result;
    }
}
