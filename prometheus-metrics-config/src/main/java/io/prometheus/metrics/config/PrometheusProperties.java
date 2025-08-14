package io.prometheus.metrics.config;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * The Prometheus Java client library can be configured at runtime (e.g. using a properties file).
 *
 * <p>This class represents the runtime configuration.
 */
public class PrometheusProperties {

  private static final PrometheusProperties instance = PrometheusPropertiesLoader.load();

  @Nullable private final MetricsProperties defaultMetricsProperties;
  private final Map<String, MetricsProperties> metricProperties = new HashMap<>();
  @Nullable private final ExemplarsProperties exemplarProperties;
  @Nullable private final ExporterProperties exporterProperties;
  @Nullable private final ExporterFilterProperties exporterFilterProperties;
  @Nullable private final ExporterHttpServerProperties exporterHttpServerProperties;
  @Nullable private final ExporterOpenTelemetryProperties exporterOpenTelemetryProperties;
  @Nullable private final ExporterPushgatewayProperties exporterPushgatewayProperties;

  /**
   * Get the properties instance. When called for the first time, {@code get()} loads the properties
   * from the following locations:
   *
   * <ul>
   *   <li>{@code prometheus.properties} file found in the classpath.
   *   <li>Properties file specified in the {@code PROMETHEUS_CONFIG} environment variable or the
   *       {@code prometheus.config} system property.
   *   <li>Individual properties from system properties.
   * </ul>
   */
  public static PrometheusProperties get() throws PrometheusPropertiesException {
    return instance;
  }

  public static Builder builder() {
    return new Builder();
  }

  public PrometheusProperties(
      @Nullable MetricsProperties defaultMetricsProperties,
      Map<String, MetricsProperties> metricProperties,
      @Nullable ExemplarsProperties exemplarProperties,
      @Nullable ExporterProperties exporterProperties,
      @Nullable ExporterFilterProperties exporterFilterProperties,
      @Nullable ExporterHttpServerProperties httpServerConfig,
      @Nullable ExporterPushgatewayProperties pushgatewayProperties,
      @Nullable ExporterOpenTelemetryProperties otelConfig) {
    this.defaultMetricsProperties = defaultMetricsProperties;
    this.metricProperties.putAll(metricProperties);
    this.exemplarProperties = exemplarProperties;
    this.exporterProperties = exporterProperties;
    this.exporterFilterProperties = exporterFilterProperties;
    this.exporterHttpServerProperties = httpServerConfig;
    this.exporterPushgatewayProperties = pushgatewayProperties;
    this.exporterOpenTelemetryProperties = otelConfig;
  }

  /**
   * The default metric properties apply for metrics where {@link #getMetricProperties(String)} is
   * {@code null}.
   */
  @Nullable
  public MetricsProperties getDefaultMetricProperties() {
    return defaultMetricsProperties;
  }

  /**
   * Properties specific for one metric. Should be merged with {@link
   * #getDefaultMetricProperties()}. May return {@code null} if no metric-specific properties are
   * configured for a metric name.
   */
  @Nullable
  public MetricsProperties getMetricProperties(String metricName) {
    return metricProperties.get(metricName.replace(".", "_"));
  }

  @Nullable
  public ExemplarsProperties getExemplarProperties() {
    return exemplarProperties;
  }

  @Nullable
  public ExporterProperties getExporterProperties() {
    return exporterProperties;
  }

  @Nullable
  public ExporterFilterProperties getExporterFilterProperties() {
    return exporterFilterProperties;
  }

  @Nullable
  public ExporterHttpServerProperties getExporterHttpServerProperties() {
    return exporterHttpServerProperties;
  }

  @Nullable
  public ExporterPushgatewayProperties getExporterPushgatewayProperties() {
    return exporterPushgatewayProperties;
  }

  @Nullable
  public ExporterOpenTelemetryProperties getExporterOpenTelemetryProperties() {
    return exporterOpenTelemetryProperties;
  }

  public static class Builder {
    @Nullable private MetricsProperties defaultMetricsProperties;
    private Map<String, MetricsProperties> metricProperties = new HashMap<>();
    @Nullable private ExemplarsProperties exemplarProperties;
    @Nullable private ExporterProperties exporterProperties;
    @Nullable private ExporterFilterProperties exporterFilterProperties;
    @Nullable private ExporterHttpServerProperties exporterHttpServerProperties;
    @Nullable private ExporterPushgatewayProperties pushgatewayProperties;
    @Nullable private ExporterOpenTelemetryProperties otelConfig;

    private Builder() {}

    public Builder defaultMetricsProperties(@Nullable MetricsProperties defaultMetricsProperties) {
      this.defaultMetricsProperties = defaultMetricsProperties;
      return this;
    }

    public Builder metricProperties(Map<String, MetricsProperties> metricProperties) {
      this.metricProperties = metricProperties;
      return this;
    }

    /** Convenience for adding a single named MetricsProperties */
    public Builder putMetricProperty(String name, MetricsProperties props) {
      this.metricProperties.put(name, props);
      return this;
    }

    public Builder exemplarProperties(@Nullable ExemplarsProperties exemplarProperties) {
      this.exemplarProperties = exemplarProperties;
      return this;
    }

    public Builder exporterProperties(@Nullable ExporterProperties exporterProperties) {
      this.exporterProperties = exporterProperties;
      return this;
    }

    public Builder exporterFilterProperties(
        @Nullable ExporterFilterProperties exporterFilterProperties) {
      this.exporterFilterProperties = exporterFilterProperties;
      return this;
    }

    public Builder exporterHttpServerProperties(
        @Nullable ExporterHttpServerProperties exporterHttpServerProperties) {
      this.exporterHttpServerProperties = exporterHttpServerProperties;
      return this;
    }

    public Builder pushgatewayProperties(
        @Nullable ExporterPushgatewayProperties pushgatewayProperties) {
      this.pushgatewayProperties = pushgatewayProperties;
      return this;
    }

    public Builder exporterOpenTelemetryProperties(
        @Nullable ExporterOpenTelemetryProperties exporterOpenTelemetryProperties) {
      this.otelConfig = exporterOpenTelemetryProperties;
      return this;
    }

    public PrometheusProperties build() {
      return new PrometheusProperties(
          defaultMetricsProperties,
          metricProperties,
          exemplarProperties,
          exporterProperties,
          exporterFilterProperties,
          exporterHttpServerProperties,
          pushgatewayProperties,
          otelConfig);
    }
  }
}
