package io.prometheus.metrics.instrumentation.dropwizard5.labels;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.prometheus.metrics.instrumentation.dropwizard5.labels.MapperConfig.METRIC_GLOB_REGEX;

/**
 * GraphiteNamePattern is initialised with a simplified glob pattern that only allows '*' as special character.
 * Examples of valid patterns:
 * <ul>
 * <li>org.test.controller.gather.status.400</li>
 * <li>org.test.controller.gather.status.*</li>
 * <li>org.test.controller.*.status.*</li>
 * <li>*.test.controller.*.status.*</li>
 * </ul>
 * <p>
 * It contains logic to match a metric name and to extract named parameters from it.
 */
class GraphiteNamePattern {
    private static final Pattern VALIDATION_PATTERN = Pattern.compile(METRIC_GLOB_REGEX);

    private Pattern pattern;
    private String patternStr;

    /**
     * Creates a new GraphiteNamePattern from the given simplified glob pattern.
     *
     * @param pattern The glob style pattern to be used.
     */
    GraphiteNamePattern(final String pattern) throws IllegalArgumentException {
        if (!VALIDATION_PATTERN.matcher(pattern).matches()) {
            throw new IllegalArgumentException(String.format("Provided pattern [%s] does not matches [%s]", pattern, METRIC_GLOB_REGEX));
        }
        initializePattern(pattern);
    }

    /**
     * Matches the metric name against the pattern.
     *
     * @param metricName The metric name to be tested.
     * @return {@code true} if the name is matched, {@code false} otherwise.
     */
    boolean matches(final String metricName) {
        return metricName != null && pattern.matcher(metricName).matches();
    }

    /**
     * Extracts parameters from the given metric name based on the pattern.
     * The resulting map has keys named as '${n}' where n is the 0 based position in the pattern.
     * E.g.:
     * pattern: org.test.controller.*.status.*
     * extractParameters("org.test.controller.gather.status.400") ->
     * {${0} -> "gather", ${1} -> "400"}
     *
     * @param metricName The metric name to extract parameters from.
     * @return A parameter map where keys are named '${n}' where n is 0 based parameter position in the pattern.
     */
    Map<String, String> extractParameters(final String metricName) {
        final Matcher matcher = this.pattern.matcher(metricName);
        final Map<String, String> params = new HashMap<String, String>();
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                params.put(String.format("${%d}", i - 1), matcher.group(i));
            }
        }

        return params;
    }

    /**
     * Turns the GLOB pattern into a REGEX.
     *
     * @param pattern The pattern to use
     */
    private void initializePattern(final String pattern) {
        final String[] split = pattern.split(Pattern.quote("*"), -1);
        final StringBuilder escapedPattern = new StringBuilder(Pattern.quote(split[0]));
        for (int i = 1; i < split.length; i++) {
            String quoted = Pattern.quote(split[i]);
            escapedPattern.append("([^.]*)").append(quoted);
        }

        final String regex = "^" + escapedPattern.toString() + "$";
        this.patternStr = regex;
        this.pattern = Pattern.compile(regex);
    }

    String getPatternString() {
        return this.patternStr;
    }
}
