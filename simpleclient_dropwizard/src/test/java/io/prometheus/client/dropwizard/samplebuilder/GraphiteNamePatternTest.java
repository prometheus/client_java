package io.prometheus.client.dropwizard.samplebuilder;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class GraphiteNamePatternTest {

    @Test
    public void createNew_WHEN_ValidPattern_THEN_ShouldCreateThePatternSuccessfully() {
        final List<String> validPatterns = Arrays.asList(
                "org.test.controller.gather.status.400",
                "org.test.controller.*.status.400",
                "org.test.controller.*.status.*",
                "*.test.controller.*.status.*",
                "*.test.controller-1.*.status.*",
                "*.amazing-test.controller-1.*.status.*"

        );
        for (String pattern : validPatterns) {
            new GraphiteNamePattern(pattern);
        }
    }


    @Test
    public void createNew_WHEN_ValidPattern_THEN_ShouldInitInternalPatternSuccessfully() {
        final Map<String, String> validPatterns = new HashMap<String, String>();
        validPatterns.put("org.test.controller.gather.status.400", "^\\Qorg.test.controller.gather.status.400\\E$");
        validPatterns.put("org.test.controller.*.status.400", "^\\Qorg.test.controller.\\E([^.]*)\\Q.status.400\\E$");
        validPatterns.put("org.test.controller.*.status.*", "^\\Qorg.test.controller.\\E([^.]*)\\Q.status.\\E([^.]*)\\Q\\E$");
        validPatterns.put("*.test.controller.*.status.*", "^\\Q\\E([^.]*)\\Q.test.controller.\\E([^.]*)\\Q.status.\\E([^.]*)\\Q\\E$");

        for (Map.Entry<String, String> expected : validPatterns.entrySet()) {
            final GraphiteNamePattern pattern = new GraphiteNamePattern(expected.getKey());
            Assertions.assertThat(pattern.getPatternString()).isEqualTo(expected.getValue());
        }
    }


    @Test
    public void match_WHEN_NotMatchingMetricNameProvided_THEN_ShouldNotMatch() {
        final GraphiteNamePattern pattern = new GraphiteNamePattern("org.test.controller.*.status.*");
        final List<String> notMatchingMetricNamed = Arrays.asList(
                "org.test.controller.status.400",
                "",
                null
        );

        for (String metricName : notMatchingMetricNamed) {
            Assertions.assertThat(pattern.matches(metricName)).as("Matching [%s] against [%s]", metricName, pattern.getPatternString()).isFalse();
        }
    }


    @Test
    public void match_WHEN_MatchingMetricNameProvided_THEN_ShouldMatch() {
        final GraphiteNamePattern pattern = new GraphiteNamePattern("org.test.controller.*.status.*");
        final List<String> matchingMetricNamed = Arrays.asList(
                "org.test.controller.gather.status.400",
                "org.test.controller.gather2.status.500",
                "org.test.controller.gather1.status.",
                "org.test.controller.*.status.*",
                "org.test.controller..status.*"
        );

        for (String metricName : matchingMetricNamed) {
            Assertions.assertThat(pattern.matches(metricName)).as("Matching [%s] against [%s]", metricName, pattern.getPatternString()).isTrue();
        }
    }


    @Test
    public void match_WHEN_onlyGlob_THEN_ShouldMatchAny() {
        GraphiteNamePattern pattern = new GraphiteNamePattern("*");
        Assertions.assertThat(pattern.matches("foo")).isTrue();
        Assertions.assertThat(pattern.matches("bar")).isTrue();
    }


    @Test
    public void match_WHEN_varyingFormats_THEN_ShouldMatchRegardless() {
        Map<String, String> metricsToPatterns = new HashMap<String, String>();
        metricsToPatterns.put("snake_case_example_metric", "snake_case_*_metric");
        metricsToPatterns.put("CamelCasedExampleMetric", "CamelCased*Metric");
        metricsToPatterns.put("Weird.Mixture_Of_Formats.Example_metric", "Weird.Mixture_Of_Formats.*_metric");

        for (Entry<String, String> metricToPattern : metricsToPatterns.entrySet()) {
            Assertions.assertThat(new GraphiteNamePattern(metricToPattern.getValue()).matches(metricToPattern.getKey())).isTrue();
        }
    }


    @Test
    public void extractParameters() {
        GraphiteNamePattern pattern;
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("${0}", "gather");
        expected.put("${1}", "400");
        pattern = new GraphiteNamePattern("org.test.controller.*.status.*");
        Assertions.assertThat(pattern.extractParameters("org.test.controller.gather.status.400"))
                  .isEqualTo(expected);

        expected = new HashMap<String, String>();
        expected.put("${0}", "org");
        expected.put("${1}", "gather");
        expected.put("${2}", "400");
        pattern = new GraphiteNamePattern("*.test.controller.*.status.*");
        Assertions.assertThat(pattern.extractParameters("org.test.controller.gather.status.400"))
                  .isEqualTo(expected);
    }


    @Test
    public void extractParameters_WHEN_emptyStringInDottedMetricsName_THEN_ShouldReturnEmptyString() {
        GraphiteNamePattern pattern;
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("${0}", "");
        expected.put("${1}", "400");
        pattern = new GraphiteNamePattern("org.test.controller.*.status.*");
        Assertions.assertThat(pattern.extractParameters("org.test.controller..status.400"))
                  .isEqualTo(expected);

    }


    @Test
    public void extractParameters_WHEN_moreDots_THEN_ShouldReturnNoMatches() {
        GraphiteNamePattern pattern;
        pattern = new GraphiteNamePattern("org.test.controller.*.status.*");
        Assertions.assertThat(pattern.extractParameters("org.test.controller...status.400"))
                  .isEqualTo(Collections.emptyMap());

    }

    @Test
    public void extractParameters_WHEN_onlyGlob_THEN_ShouldExtractEntireMetric() {
        String metric = "http_requests";
        GraphiteNamePattern pattern = new GraphiteNamePattern("*");
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("${0}", metric);
        Assertions.assertThat(pattern.extractParameters(metric)).isEqualTo(expected);
    }


    @Test
    public void  extractParameters_WHEN_varyingFormats_THEN_ShouldExtractRegardless() {
        Map<String, String> metricsToPatterns = new HashMap<String, String>();
        metricsToPatterns.put("snake_case_example_metric", "snake_case_*_metric");
        metricsToPatterns.put("CamelCasedExampleMetric", "CamelCased*Metric");
        metricsToPatterns.put("Weird.Mixture_Of_Formats.Example_metric", "Weird.Mixture_Of_Formats.*_metric");

        for (Entry<String, String> metricToPattern : metricsToPatterns.entrySet()) {
            GraphiteNamePattern graphiteNamePattern = new GraphiteNamePattern(metricToPattern.getValue());
            Entry<String, String> actual = graphiteNamePattern.extractParameters(metricToPattern.getKey()).entrySet().iterator().next();
            Assertions.assertThat(actual.getKey()).isEqualTo("${0}");
            Assertions.assertThat(actual.getValue()).isEqualToIgnoringCase("example");
        }
    }
}