package io.prometheus.metrics.config;

import java.util.HashMap;
import java.util.Map;

/**
 * The Prometheus Java client library can be configured at runtime (e.g. using a properties file).
 * <p>
 * This class represents the runtime configuration.
 */
public class PrometheusProperties {

    private static final PrometheusProperties instance = PrometheusPropertiesLoader.load();

    private final MetricsProperties defaultMetricsProperties;
    private final Map<String, MetricsProperties> metricProperties = new HashMap<>();
    private final ExemplarsProperties exemplarProperties;
    private final ExporterProperties exporterProperties;
    private final ExporterFilterProperties exporterFilterProperties;
    private final ExporterHttpServerProperties httpServerConfig;

    /**
     * Get the properties instance. When called for the first time, {@code get()} loads the properties from the following locations:
     * <ul>
     *     <li>{@code prometheus.properties} file found in the classpath.</li>
     *     <li>Properties file specified in the {@code PROMETHEUS_CONFIG} environment variable or the {@code prometheus.config} system property.</li>
     *     <li>Individual properties from system properties.</li>
     * </ul>
     */
    public static PrometheusProperties get() throws PrometheusPropertiesException {
        return instance;
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

    /**
     * The default metric properties apply for metrics where {@link #getMetricProperties(String)} is {@code null}.
     */
    public MetricsProperties getDefaultMetricProperties() {
        return defaultMetricsProperties;
    }

    /**
     * Properties specific for one metric. Should be merged with {@link #getDefaultMetricProperties()}.
     * May return {@code null} if no metric-specific properties are configured for a metric name.
     */
    public MetricsProperties getMetricProperties(String metricName) {
        return metricProperties.get(metricName.replace(".", "_"));
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
