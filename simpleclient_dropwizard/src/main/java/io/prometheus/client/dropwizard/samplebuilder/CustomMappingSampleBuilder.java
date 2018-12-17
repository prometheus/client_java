package io.prometheus.client.dropwizard.samplebuilder;

import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Custom {@link SampleBuilder} implementation to allow Dropwizard metrics to be translated to Prometheus metrics including custom labels and names.
 * Prometheus metric name and labels are extracted from the Dropwizard name based on the provided list of {@link MapperConfig}s.
 * The FIRST matching config will be used.
 * If no config is matched, the {@link DefaultSampleBuilder} is used.
 */
public class CustomMappingSampleBuilder implements SampleBuilder {
    private final List<CompiledMapperConfig> compiledMapperConfigs;
    private final DefaultSampleBuilder defaultMetricSampleBuilder = new DefaultSampleBuilder();

    public CustomMappingSampleBuilder(final List<MapperConfig> mapperConfigs) {
        if (mapperConfigs == null || mapperConfigs.isEmpty()) {
            throw new IllegalArgumentException("CustomMappingSampleBuilder needs some mapper configs!");
        }

        this.compiledMapperConfigs = new ArrayList<CompiledMapperConfig>(mapperConfigs.size());
        for (MapperConfig config : mapperConfigs) {
            this.compiledMapperConfigs.add(new CompiledMapperConfig(config));
        }
    }

    @Override
    public Collector.MetricFamilySamples.Sample createSample(final String dropwizardName, final String nameSuffix, final List<String> additionalLabelNames, final List<String> additionalLabelValues, final double value) {
        if (dropwizardName == null) {
            throw new IllegalArgumentException("Dropwizard metric name cannot be null");
        }

        CompiledMapperConfig matchingConfig = null;
        for (CompiledMapperConfig config : this.compiledMapperConfigs) {
            if (config.pattern.matches(dropwizardName)) {
                matchingConfig = config;
                break;
            }
        }

        if (matchingConfig != null) {
            final Map<String, String> params = matchingConfig.pattern.extractParameters(dropwizardName);
            final NameAndLabels nameAndLabels = getNameAndLabels(matchingConfig.mapperConfig, params);
            nameAndLabels.labelNames.addAll(additionalLabelNames);
            nameAndLabels.labelValues.addAll(additionalLabelValues);
            return defaultMetricSampleBuilder.createSample(
                    nameAndLabels.name, nameSuffix,
                    nameAndLabels.labelNames,
                    nameAndLabels.labelValues,
                    value
            );
        }


        return defaultMetricSampleBuilder.createSample(
                dropwizardName, nameSuffix,
                additionalLabelNames,
                additionalLabelValues,
                value
        );
    }

    protected NameAndLabels getNameAndLabels(final MapperConfig config, final Map<String, String> parameters) {
        final String metricName = formatTemplate(config.getName(), parameters);
        final List<String> labels = new ArrayList<String>(config.getLabels().size());
        final List<String> labelValues = new ArrayList<String>(config.getLabels().size());
        for (Map.Entry<String, String> entry : config.getLabels().entrySet()) {
            labels.add(entry.getKey());
            labelValues.add(formatTemplate(entry.getValue(), parameters));
        }

        return new NameAndLabels(metricName, labels, labelValues);
    }

    private String formatTemplate(final String template, final Map<String, String> params) {
        String result = template;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;
    }

    static class CompiledMapperConfig {
        final MapperConfig mapperConfig;
        final GraphiteNamePattern pattern;

        CompiledMapperConfig(final MapperConfig mapperConfig) {
            this.mapperConfig = mapperConfig;
            this.pattern = new GraphiteNamePattern(mapperConfig.getMatch());
        }
    }

    static class NameAndLabels {
        final String name;
        final List<String> labelNames;
        final List<String> labelValues;

        NameAndLabels(final String name, final List<String> labelNames, final List<String> labelValues) {
            this.name = name;
            this.labelNames = labelNames;
            this.labelValues = labelValues;
        }
    }
}
