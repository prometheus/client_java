package io.prometheus.metrics.config;

import java.util.HashMap;
import java.util.Map;

public class PrometheusConfig {

    private static final PrometheusConfig instance = PrometheusConfigLoader.load();

    private final MetricsConfig defaultMetricsConfig;

    private final Map<String, MetricsConfig> metricsConfigs = new HashMap<>();
    private final ExemplarConfig exemplarConfig;
    private final ExpositionFormatConfig expositionFormatConfig;
    private final HttpServerConfig httpServerConfig;

    public static PrometheusConfig getInstance() throws PrometheusConfigException {
        return instance;
    }

    public PrometheusConfig(
            MetricsConfig defaultMetricsConfig,
            Map<String, MetricsConfig> metricsConfigs,
            ExemplarConfig exemplarConfig,
            ExpositionFormatConfig expositionFormatConfig,
            HttpServerConfig httpServerConfig) {
        this.defaultMetricsConfig = defaultMetricsConfig;
        this.metricsConfigs.putAll(metricsConfigs);
        this.exemplarConfig = exemplarConfig;
        this.expositionFormatConfig = expositionFormatConfig;
        this.httpServerConfig = httpServerConfig;
    }

    public MetricsConfig getDefaultMetricsConfig() {
        return defaultMetricsConfig;
    }

    public MetricsConfig getMetricsConfig(String metricName) {
        return metricsConfigs.get(metricName);
    }

    public ExemplarConfig getExemplarConfig() {
        return exemplarConfig;
    }

    public ExpositionFormatConfig getExpositionFormatConfig() {
        return expositionFormatConfig;
    }

    public HttpServerConfig getHttpServerConfig() {
        return httpServerConfig;
    }
}
