package io.prometheus.metrics.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The Properties Loader is early stages.
 *
 * <p>It would be great to implement a subset of <a
 * href="https://docs.spring.io/spring-boot/docs/3.1.x/reference/html/features.html#features.external-config">Spring
 * Boot's Externalized Configuration</a>, like support for YAML, Properties, and env vars, or
 * support for Spring's naming conventions for properties.
 */
public class PrometheusPropertiesLoader {

  /** See {@link PrometheusProperties#get()}. */
  public static PrometheusProperties load() throws PrometheusPropertiesException {
    return load(new Properties());
  }

  public static PrometheusProperties load(Map<Object, Object> externalProperties)
      throws PrometheusPropertiesException {
    PropertySource propertySource = loadProperties(externalProperties);
    PrometheusProperties.MetricPropertiesMap metricsConfigs = loadMetricsConfigs(propertySource);
    MetricsProperties defaultMetricsProperties =
        MetricsProperties.load("io.prometheus.metrics", propertySource);
    ExemplarsProperties exemplarConfig = ExemplarsProperties.load(propertySource);
    ExporterProperties exporterProperties = ExporterProperties.load(propertySource);
    ExporterFilterProperties exporterFilterProperties =
        ExporterFilterProperties.load(propertySource);
    ExporterHttpServerProperties exporterHttpServerProperties =
        ExporterHttpServerProperties.load(propertySource);
    ExporterPushgatewayProperties exporterPushgatewayProperties =
        ExporterPushgatewayProperties.load(propertySource);
    ExporterOpenTelemetryProperties exporterOpenTelemetryProperties =
        ExporterOpenTelemetryProperties.load(propertySource);
    validateAllPropertiesProcessed(propertySource);
    return new PrometheusProperties(
        defaultMetricsProperties,
        metricsConfigs,
        exemplarConfig,
        exporterProperties,
        exporterFilterProperties,
        exporterHttpServerProperties,
        exporterPushgatewayProperties,
        exporterOpenTelemetryProperties);
  }

  // This will remove entries from propertySource when they are processed.
  static PrometheusProperties.MetricPropertiesMap loadMetricsConfigs(
      PropertySource propertySource) {
    PrometheusProperties.MetricPropertiesMap result =
        new PrometheusProperties.MetricPropertiesMap();
    // Note that the metric name in the properties file must be as exposed in the Prometheus
    // exposition formats,
    // i.e. all dots replaced with underscores.

    // Get a snapshot of all keys for pattern matching. Entries will be removed
    // when MetricsProperties.load(...) is called.
    Set<String> propertyNames = propertySource.getAllKeys();
    for (String propertyName : propertyNames) {
      String metricName = null;

      if (propertyName.startsWith("io.prometheus.metrics.")) {
        // Dot-separated format (from regular properties, system properties, or files)
        String remainder = propertyName.substring("io.prometheus.metrics.".length());
        // Try to match against known property suffixes
        for (String suffix : MetricsProperties.PROPERTY_SUFFIXES) {
          if (remainder.endsWith("." + suffix)) {
            // Metric name in dot format, convert dots to underscores for exposition format
            metricName =
                remainder.substring(0, remainder.length() - suffix.length() - 1).replace(".", "_");
            break;
          }
        }
      } else if (propertyName.startsWith("io_prometheus_metrics_")) {
        // Underscore-separated format (from environment variables)
        String remainder = propertyName.substring("io_prometheus_metrics_".length());
        // Try to match against known property suffixes
        for (String suffix : MetricsProperties.PROPERTY_SUFFIXES) {
          if (remainder.endsWith("_" + suffix)) {
            metricName = remainder.substring(0, remainder.length() - suffix.length() - 1);
            break;
          }
        }
      }

      if (metricName != null && result.get(metricName) == null) {
        result.put(
            metricName,
            MetricsProperties.load("io.prometheus.metrics." + metricName, propertySource));
      }
    }
    return result;
  }

  // If there are properties left starting with io.prometheus it's likely a typo,
  // because we didn't use that property.
  // Throw a config error to let the user know that this property doesn't exist.
  private static void validateAllPropertiesProcessed(PropertySource propertySource) {
    for (String key : propertySource.getRemainingKeys()) {
      if (key.startsWith("io.prometheus") || key.startsWith("io_prometheus")) {
        throw new PrometheusPropertiesException(key + ": Unknown property");
      }
    }
  }

  private static PropertySource loadProperties(Map<Object, Object> externalProperties) {
    // Regular properties (lowest priority): classpath, file, system properties
    Map<Object, Object> regularProperties = new HashMap<>();
    // Normalize all properties at load time to handle camelCase in files for backward compatibility
    normalizeAndPutAll(regularProperties, loadPropertiesFromClasspath());
    normalizeAndPutAll(
        regularProperties,
        loadPropertiesFromFile()); // overriding the entries from the classpath file
    // overriding the entries from the properties file
    // copy System properties to avoid ConcurrentModificationException
    // normalize camelCase system properties to snake_case for backward compatibility
    System.getProperties().stringPropertyNames().stream()
        .filter(key -> key.startsWith("io.prometheus"))
        .forEach(key -> regularProperties.put(normalizePropertyKey(key), System.getProperty(key)));

    // Environment variables (second priority): just lowercase, keep underscores
    Map<Object, Object> envVarProperties = loadPropertiesFromEnvironment();

    // External properties (highest priority): normalize camelCase for backward compatibility
    Map<Object, Object> normalizedExternalProperties = new HashMap<>();
    externalProperties.forEach(
        (key, value) ->
            normalizedExternalProperties.put(normalizePropertyKey(key.toString()), value));

    return new PropertySource(normalizedExternalProperties, envVarProperties, regularProperties);
  }

  private static void normalizeAndPutAll(Map<Object, Object> target, Map<Object, Object> source) {
    source.forEach((key, value) -> target.put(normalizePropertyKey(key.toString()), value));
  }

  private static Properties loadPropertiesFromClasspath() {
    Properties properties = new Properties();
    try (InputStream stream =
        Thread.currentThread()
            .getContextClassLoader()
            .getResourceAsStream("prometheus.properties")) {
      properties.load(stream);
    } catch (Exception ignored) {
      // No properties file found on the classpath.
    }
    return properties;
  }

  private static Properties loadPropertiesFromFile() throws PrometheusPropertiesException {
    Properties properties = new Properties();
    String path = System.getProperty("prometheus.config");
    if (System.getenv("PROMETHEUS_CONFIG") != null) {
      path = System.getenv("PROMETHEUS_CONFIG");
    }
    if (path != null) {
      try (InputStream stream = Files.newInputStream(Paths.get(path))) {
        properties.load(stream);
      } catch (IOException e) {
        throw new PrometheusPropertiesException(
            "Failed to read Prometheus properties from " + path + ": " + e.getMessage(), e);
      }
    }
    return properties;
  }

  /**
   * Load properties from environment variables.
   *
   * <p>Environment variables are converted to lowercase but keep underscores as-is. For example,
   * the environment variable IO_PROMETHEUS_METRICS_EXEMPLARS_ENABLED becomes
   * io_prometheus_metrics_exemplars_enabled.
   *
   * <p>The transformation to dot notation happens at access time in PropertySource.
   *
   * <p>Only environment variables starting with IO_PROMETHEUS are considered.
   *
   * @return properties loaded from environment variables (with lowercase keys and underscores)
   */
  private static Map<Object, Object> loadPropertiesFromEnvironment() {
    Map<Object, Object> properties = new HashMap<>();
    System.getenv()
        .forEach(
            (key, value) -> {
              if (key.startsWith("IO_PROMETHEUS")) {
                String normalizedKey = key.toLowerCase(java.util.Locale.ROOT);
                properties.put(normalizedKey, value);
              }
            });
    return properties;
  }

  /**
   * Normalize a property key for consistent lookup.
   *
   * <p>Converts camelCase property keys to snake_case. This allows both snake_case (preferred) and
   * camelCase (deprecated) property names to be used.
   *
   * <p>For example: exemplarsEnabled → exemplars_enabled exemplars_enabled → exemplars_enabled
   * (unchanged)
   *
   * @param key the property key
   * @return the normalized property key
   */
  static String normalizePropertyKey(String key) {
    // Insert underscores before uppercase letters to convert camelCase to snake_case
    String withUnderscores = key.replaceAll("([a-z])([A-Z])", "$1_$2");
    return withUnderscores.toLowerCase(java.util.Locale.ROOT);
  }
}
