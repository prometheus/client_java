package io.prometheus.metrics.config;

import java.util.HashMap;
import java.util.Map;

public class PrometheusProperties {

    private static final PrometheusProperties defaultInstance = PrometheusPropertiesLoader.load();

    private final MetricProperties defaultMetricProperties;
    private final Map<String, MetricProperties> metricProperties = new HashMap<>();
    private final ExemplarProperties exemplarConfig;
    private final ExpositionFormatProperties expositionFormatProperties;
    private final HttpServerProperties httpServerConfig;

    public static PrometheusProperties get() throws PrometheusPropertiesException {
        return defaultInstance;
    }

    public PrometheusProperties(
            MetricProperties defaultMetricProperties,
            Map<String, MetricProperties> metricProperties,
            ExemplarProperties exemplarConfig,
            ExpositionFormatProperties expositionFormatProperties,
            HttpServerProperties httpServerConfig) {
        this.defaultMetricProperties = defaultMetricProperties;
        this.metricProperties.putAll(metricProperties);
        this.exemplarConfig = exemplarConfig;
        this.expositionFormatProperties = expositionFormatProperties;
        this.httpServerConfig = httpServerConfig;
    }

    public MetricProperties getDefaultMetricProperties() {
        return defaultMetricProperties;
    }

    public MetricProperties getMetricProperties(String metricName) {
        return metricProperties.get(metricName);
    }

    public ExemplarProperties getExemplarConfig() {
        return exemplarConfig;
    }

    public ExpositionFormatProperties getExpositionFormatConfig() {
        return expositionFormatProperties;
    }

    public HttpServerProperties getHttpServerConfig() {
        return httpServerConfig;
    }
}
