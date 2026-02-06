package io.prometheus.metrics.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

/**
 * The Prometheus Java client library can be configured at runtime (e.g. using a properties file).
 *
 * <p>This class represents the runtime configuration.
 */
public class PrometheusProperties {

  private static final PrometheusProperties instance = PrometheusPropertiesLoader.load();

  private final MetricsProperties defaultMetricsProperties;
  private final Map<String, MetricsProperties> metricProperties = new HashMap<>();
  private final ExemplarsProperties exemplarProperties;
  private final ExporterProperties exporterProperties;
  private final ExporterFilterProperties exporterFilterProperties;
  private final ExporterHttpServerProperties exporterHttpServerProperties;
  private final ExporterOpenTelemetryProperties exporterOpenTelemetryProperties;
  private final ExporterPushgatewayProperties exporterPushgatewayProperties;

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
      MetricsProperties defaultMetricsProperties,
      Map<String, MetricsProperties> metricProperties,
      ExemplarsProperties exemplarProperties,
      ExporterProperties exporterProperties,
      ExporterFilterProperties exporterFilterProperties,
      ExporterHttpServerProperties httpServerConfig,
      ExporterPushgatewayProperties pushgatewayProperties,
      ExporterOpenTelemetryProperties otelConfig) {
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
    return exporterHttpServerProperties;
  }

  public ExporterPushgatewayProperties getExporterPushgatewayProperties() {
    return exporterPushgatewayProperties;
  }

  public ExporterOpenTelemetryProperties getExporterOpenTelemetryProperties() {
    return exporterOpenTelemetryProperties;
  }

  public boolean useOtelMetrics(String prometheusMetric, String otelMetric) {
    Boolean useByPrometheusMetric = usesOtelMetric(prometheusMetric);
    if (Boolean.FALSE.equals(useByPrometheusMetric)) return false;
    Boolean useByOtelMetric = usesOtelMetric(otelMetric);
    if (Boolean.FALSE.equals(useByOtelMetric)) return false;
    return Boolean.TRUE.equals(getDefaultMetricProperties().useOtelMetrics());
  }

  private Boolean usesOtelMetric(String metric) {
    return Optional.ofNullable(getMetricProperties(metric))
        .map(MetricsProperties::useOtelMetrics)
        .orElse(null);
  }

  public static class Builder {
    private MetricsProperties defaultMetricsProperties = MetricsProperties.builder().build();
    private Map<String, MetricsProperties> metricProperties = new HashMap<>();
    private ExemplarsProperties exemplarProperties = ExemplarsProperties.builder().build();
    private ExporterProperties exporterProperties = ExporterProperties.builder().build();
    private ExporterFilterProperties exporterFilterProperties =
        ExporterFilterProperties.builder().build();
    private ExporterHttpServerProperties exporterHttpServerProperties =
        ExporterHttpServerProperties.builder().build();
    private ExporterPushgatewayProperties pushgatewayProperties =
        ExporterPushgatewayProperties.builder().build();
    private ExporterOpenTelemetryProperties otelConfig =
        ExporterOpenTelemetryProperties.builder().build();

    private Builder() {}

    public Builder defaultMetricsProperties(MetricsProperties defaultMetricsProperties) {
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

    public Builder exemplarProperties(ExemplarsProperties exemplarProperties) {
      this.exemplarProperties = exemplarProperties;
      return this;
    }

    public Builder exporterProperties(ExporterProperties exporterProperties) {
      this.exporterProperties = exporterProperties;
      return this;
    }

    public Builder exporterFilterProperties(ExporterFilterProperties exporterFilterProperties) {
      this.exporterFilterProperties = exporterFilterProperties;
      return this;
    }

    public Builder exporterHttpServerProperties(
        ExporterHttpServerProperties exporterHttpServerProperties) {
      this.exporterHttpServerProperties = exporterHttpServerProperties;
      return this;
    }

    public Builder pushgatewayProperties(ExporterPushgatewayProperties pushgatewayProperties) {
      this.pushgatewayProperties = pushgatewayProperties;
      return this;
    }

    public Builder exporterOpenTelemetryProperties(
        ExporterOpenTelemetryProperties exporterOpenTelemetryProperties) {
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
