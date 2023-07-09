package io.prometheus.metrics.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Allow overriding configuration like histogram bucket boundaries at application startup time.
 */
public class PrometheusProperties {

    private static final PrometheusProperties defaultInstance = PrometheusPropertiesLoader.load();

    private final MetricsProperties defaultMetricsProperties;
    private final Map<String, MetricsProperties> metricProperties = new HashMap<>();
    private final ExemplarsProperties exemplarProperties;
    private final ExporterProperties exporterProperties;
    private final ExporterFilterProperties exporterFilterProperties;
    private final ExporterHttpServerProperties httpServerConfig;

    public static PrometheusProperties get() throws PrometheusPropertiesException {
        return defaultInstance;
    }

    public PrometheusProperties(
            MetricsProperties defaultMetricsProperties,
            Map<String, MetricsProperties> metricProperties,
            ExemplarsProperties exemplarProperties,
            ExporterProperties exporterProperties,
            ExporterFilterProperties exporterFilterProperties,
            ExporterHttpServerProperties httpServerConfig) {
        this.defaultMetricsProperties = defaultMetricsProperties;
        this.metricProperties.putAll(metricProperties);
        this.exemplarProperties = exemplarProperties;
        this.exporterProperties = exporterProperties;
        this.exporterFilterProperties = exporterFilterProperties;
        this.httpServerConfig = httpServerConfig;
    }

    public MetricsProperties getDefaultMetricProperties() {
        return defaultMetricsProperties;
    }

    public MetricsProperties getMetricProperties(String metricName) {
        return metricProperties.get(metricName);
    }

    public ExemplarsProperties getExemplarProperties() {
        return exemplarProperties;
    }

    public ExporterProperties getExporterProperties() {
        return exporterProperties;
    }

    public ExporterFilterProperties getExporterFilterProperties() {
        return exporterFilterProperties;
    }

    public ExporterHttpServerProperties getExporterHttpServerProperties() {
        return httpServerConfig;
    }
}
