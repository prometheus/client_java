package io.prometheus.client.dropwizard.samplebuilder;

import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Default implementation of {@link SampleBuilder}.
 * Sanitises the metric name if necessary.
 *
 * @see io.prometheus.client.Collector#sanitizeMetricName(String)
 */
public class DefaultSampleBuilder implements SampleBuilder {
    @Override
    public Collector.MetricFamilySamples.Sample createSample(final String dropwizardName, final String nameSuffix, final List<String> additionalLabelNames, final List<String> additionalLabelValues, final double value) {
        final String suffix = nameSuffix == null ? "" : nameSuffix;
        final List<String> labelNames = additionalLabelNames == null ? Collections.<String>emptyList() : additionalLabelNames;
        final List<String> labelValues = additionalLabelValues == null ? Collections.<String>emptyList() : additionalLabelValues;
        return new Collector.MetricFamilySamples.Sample(
                Collector.sanitizeMetricName(dropwizardName + suffix),
                new ArrayList<String>(labelNames),
                new ArrayList<String>(labelValues),
                value
        );
    }
}
