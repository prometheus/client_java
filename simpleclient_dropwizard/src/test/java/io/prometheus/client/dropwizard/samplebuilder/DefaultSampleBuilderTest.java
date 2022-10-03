package io.prometheus.client.dropwizard.samplebuilder;

import io.prometheus.metrics.Collector;
import io.prometheus.client.dropwizard.samplebuilder.DefaultSampleBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class DefaultSampleBuilderTest {
    @Test
    public void test_WHEN_NoSuffixAndExtraLabels_THEN_ShouldReturnCorrectSample() {
        final DefaultSampleBuilder builder = new DefaultSampleBuilder();
        final Collector.MetricFamilySamples.Sample result = builder.createSample("org.github.name", null, null, null, 1d);
        Assert.assertEquals(
                new Collector.MetricFamilySamples.Sample("org_github_name", Collections.<String>emptyList(), Collections.<String>emptyList(), 1d)
                , result);
    }

    @Test
    public void test_WHEN_SuffixAndExtraLabels_THEN_ShouldReturnCorrectSample() {
        final DefaultSampleBuilder builder = new DefaultSampleBuilder();
        final Collector.MetricFamilySamples.Sample result = builder.createSample("org.github.name", "suffix.test", Collections.singletonList("another"), Arrays.asList("label"), 1d);
        Assert.assertEquals(
                new Collector.MetricFamilySamples.Sample("org_github_namesuffix_test", Collections.singletonList("another"),
                        Arrays.asList("label"), 1d), result);

    }

}