package io.prometheus.metrics.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * The Prometheus Java client library can be configured at runtime (e.g. using a properties file).
 *
 * <p>This class represents the runtime configuration.
 */
public class PrometheusProperties {

  private static final PrometheusProperties instance = PrometheusPropertiesLoader.load();

  private final MetricsProperties defaultMetricsProperties;
  private final MetricPropertiesMap metricProperties;
  private final ExemplarsProperties exemplarProperties;
  private final ExporterProperties exporterProperties;
  private final ExporterFilterProperties exporterFilterProperties;
  private final ExporterHttpServerProperties exporterHttpServerProperties;
  private final ExporterOpenTelemetryProperties exporterOpenTelemetryProperties;
  private final ExporterPushgatewayProperties exporterPushgatewayProperties;

  /**
   * Map that stores metric-specific properties keyed by metric name in exposition format
   * (underscores instead of dots).
   *
   * <p>This wrapper makes it explicit that metric names are normalized to underscore format for
   * storage, so that environment variables and properties with dots in metric names can be
   * correctly looked up using normalized names.
   */
  static class MetricPropertiesMap {
    private final Map<String, MetricsProperties> map = new HashMap<>();

    void set(Map<String, MetricsProperties> properties) {
      map.clear();
      properties.forEach(this::put);
    }

    void put(String metricName, MetricsProperties properties) {
      map.put(normalize(metricName), properties);
    }

    /**
     * Get metric properties by metric name.
     *
     * <p>Accepts metric names in any format (with dots or underscores) and automatically converts
     * them to the normalized underscore format used for storage.
     *
     * @param metricName the metric name (dots will be converted to underscores)
     * @return the metric properties, or null if not configured
     */
    @Nullable
    MetricsProperties get(String metricName) {
      return map.get(normalize(metricName));
    }

    // copied from PrometheusNaming - but we can't reuse that class here because it's in a module
    // that
    // depends on PrometheusProperties, which would create a circular dependency.
    private static String normalize(String name) {
      StringBuilder escaped = new StringBuilder();

      for (int i = 0; i < name.length(); ) {
        int c = name.codePointAt(i);
        if (isValidLegacyChar(c, i)) {
          escaped.appendCodePoint(c);
        } else {
          escaped.append('_');
        }
        i += Character.charCount(c);
      }
      return escaped.toString();
    }
  }

  private static boolean isValidLegacyChar(int c, int i) {
    return (c >= 'a' && c <= 'z')
        || (c >= 'A' && c <= 'Z')
        || c == '_'
        || c == ':'
        || (c >= '0' && c <= '9' && i > 0);
  }

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

  // Package-private constructor for PrometheusPropertiesLoader and Builder
  PrometheusProperties(
      MetricsProperties defaultMetricsProperties,
      MetricPropertiesMap metricProperties,
      ExemplarsProperties exemplarProperties,
      ExporterProperties exporterProperties,
      ExporterFilterProperties exporterFilterProperties,
      ExporterHttpServerProperties httpServerConfig,
      ExporterPushgatewayProperties pushgatewayProperties,
      ExporterOpenTelemetryProperties otelConfig) {
    this.defaultMetricsProperties = defaultMetricsProperties;
    this.metricProperties = metricProperties;
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
   *
   * @param metricName the metric name (dots will be automatically converted to underscores to match
   *     exposition format)
   */
  @Nullable
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
    return exporterHttpServerProperties;
  }

  public ExporterPushgatewayProperties getExporterPushgatewayProperties() {
    return exporterPushgatewayProperties;
  }

  public ExporterOpenTelemetryProperties getExporterOpenTelemetryProperties() {
    return exporterOpenTelemetryProperties;
  }

  public boolean useOtelSemconv(String otelMetric) {
    List<String> list = getDefaultMetricProperties().useOtelSemconv();
    if (list == null || list.isEmpty()) {
      return false;
    }
    if (list.contains("*")) {
      return true;
    }
    return list.contains(otelMetric);
  }

  public static class Builder {
    private MetricsProperties defaultMetricsProperties = MetricsProperties.builder().build();
    private final MetricPropertiesMap metricProperties = new MetricPropertiesMap();
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
      this.metricProperties.set(metricProperties);
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
