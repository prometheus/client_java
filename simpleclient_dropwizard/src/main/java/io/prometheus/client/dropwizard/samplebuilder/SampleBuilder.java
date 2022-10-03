package io.prometheus.client.dropwizard.samplebuilder;

import io.prometheus.metrics.Collector;

import java.util.List;

/**
 * SampleBuilder defines the action of creating a {@link io.prometheus.metrics.Collector.MetricFamilySamples.Sample} for the given parameters.
 */
public interface SampleBuilder {

    /**
     * Creates a new {@link io.prometheus.metrics.Collector.MetricFamilySamples.Sample} for the given parameters.
     *
     * @param dropwizardName        Metric name coming from Dropwizard.
     * @param nameSuffix            Optional suffix to add.
     * @param additionalLabelNames  Optional additional label names. Needs to have same size as additionalLabelValues.
     * @param additionalLabelValues Optional additional label values. Needs to have same size as additionalLabelNames.
     * @param value                 Metric value
     * @return A new {@link io.prometheus.metrics.Collector.MetricFamilySamples.Sample}.
     */
    Collector.MetricFamilySamples.Sample createSample(String dropwizardName, String nameSuffix, List<String> additionalLabelNames, List<String> additionalLabelValues, double value);
}
