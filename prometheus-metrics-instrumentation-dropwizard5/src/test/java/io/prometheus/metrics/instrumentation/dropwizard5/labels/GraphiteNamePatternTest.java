package io.prometheus.metrics.instrumentation.dropwizard5.labels;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphiteNamePatternTest {

    @Test(expected = IllegalArgumentException.class)
    public void createNew_WHEN_InvalidPattern_THEN_ShouldThrowException() {
        final List<String> invalidPatterns = Arrays.asList(
                "",
                "a",
                "1org",
                "1org.",
                "org.",
                "org.**",
                "org.**",
                "org.company-",
                "org.company-.",
                "org.company-*",
                "org.company.**",
                "org.company.**-",
                "org.com*pany.*",
                "org.test.contr.oller.gather.status..400",
                "org.test.controller.gather.status..400"
        );
        final GraphiteNamePattern graphiteNamePattern = new GraphiteNamePattern("");
        for (String pattern : invalidPatterns) {
            try {
                new GraphiteNamePattern(pattern);

                Assertions.failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
            } catch (IllegalArgumentException e) {
                Assertions.assertThat(e).hasMessageContaining(pattern);
            }
        }
    }

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
}