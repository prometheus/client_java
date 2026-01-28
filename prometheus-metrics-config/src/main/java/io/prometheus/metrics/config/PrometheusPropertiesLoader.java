package io.prometheus.metrics.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    Map<Object, Object> properties = loadProperties(externalProperties);
    Map<String, MetricsProperties> metricsConfigs = loadMetricsConfigs(properties);
    MetricsProperties defaultMetricsProperties =
        MetricsProperties.load("io.prometheus.metrics", properties);
    ExemplarsProperties exemplarConfig = ExemplarsProperties.load(properties);
    ExporterProperties exporterProperties = ExporterProperties.load(properties);
    ExporterFilterProperties exporterFilterProperties = ExporterFilterProperties.load(properties);
    ExporterHttpServerProperties exporterHttpServerProperties =
        ExporterHttpServerProperties.load(properties);
    ExporterPushgatewayProperties exporterPushgatewayProperties =
        ExporterPushgatewayProperties.load(properties);
    ExporterOpenTelemetryProperties exporterOpenTelemetryProperties =
        ExporterOpenTelemetryProperties.load(properties);
    validateAllPropertiesProcessed(properties);
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

  // This will remove entries from properties when they are processed.
  private static Map<String, MetricsProperties> loadMetricsConfigs(Map<Object, Object> properties) {
    Map<String, MetricsProperties> result = new HashMap<>();
    // Note that the metric name in the properties file must be as exposed in the Prometheus
    // exposition formats,
    // i.e. all dots replaced with underscores.
    Pattern pattern = Pattern.compile("io\\.prometheus\\.metrics\\.([^.]+)\\.");
    // Create a copy of the keySet() for iterating. We cannot iterate directly over keySet()
    // because entries are removed when MetricsConfig.load(...) is called.
    Set<String> propertyNames = new HashSet<>();
    for (Object key : properties.keySet()) {
      propertyNames.add(key.toString());
    }
    for (String propertyName : propertyNames) {
      Matcher matcher = pattern.matcher(propertyName);
      if (matcher.find()) {
        String metricName = matcher.group(1).replace(".", "_");
        if (!result.containsKey(metricName)) {
          result.put(
              metricName,
              MetricsProperties.load("io.prometheus.metrics." + metricName, properties));
        }
      }
    }
    return result;
  }

  // If there are properties left starting with io.prometheus it's likely a typo,
  // because we didn't use that property.
  // Throw a config error to let the user know that this property doesn't exist.
  private static void validateAllPropertiesProcessed(Map<Object, Object> properties) {
    for (Object key : properties.keySet()) {
      if (key.toString().startsWith("io.prometheus")) {
        throw new PrometheusPropertiesException(key + ": Unknown property");
      }
    }
  }

  private static Map<Object, Object> loadProperties(Map<Object, Object> externalProperties) {
    Map<Object, Object> properties = new HashMap<>();
    // Normalize all properties at load time to handle camelCase in files for backward compatibility
    normalizeAndPutAll(properties, loadPropertiesFromClasspath());
    normalizeAndPutAll(
        properties, loadPropertiesFromFile()); // overriding the entries from the classpath file
    // overriding the entries from the properties file
    // copy System properties to avoid ConcurrentModificationException
    // normalize camelCase system properties to snake_case for backward compatibility
    System.getProperties().stringPropertyNames().stream()
        .filter(key -> key.startsWith("io.prometheus"))
        .forEach(key -> properties.put(normalizePropertyKey(key), System.getProperty(key)));
    properties.putAll(loadPropertiesFromEnvironment()); // overriding with environment variables
    // normalize external properties for backward compatibility with camelCase
    externalProperties.forEach(
        (key, value) -> properties.put(normalizePropertyKey(key.toString()), value));
    return properties;
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
   * <p>Environment variables are converted to property keys by converting to lowercase and
   * replacing underscores with dots. For example, the environment variable
   * IO_PROMETHEUS_METRICS_EXEMPLARS_ENABLED becomes io.prometheus.metrics.exemplars_enabled.
   *
   * <p>Only environment variables starting with IO_PROMETHEUS are considered.
   *
   * @return properties loaded from environment variables
   */
  private static Map<Object, Object> loadPropertiesFromEnvironment() {
    Map<Object, Object> properties = new HashMap<>();
    System.getenv()
        .forEach(
            (key, value) -> {
              if (key.startsWith("IO_PROMETHEUS")) {
                String propertyKey = normalizeEnvironmentVariableKey(key);
                properties.put(propertyKey, value);
              }
            });
    return properties;
  }

  /**
   * Normalize an environment variable key to a property key.
   *
   * <p>Converts environment variables to lowercase and replaces underscores with dots in the prefix
   * part, but keeps underscores in the property name for snake_case format.
   *
   * <p>For example: IO_PROMETHEUS_METRICS_EXEMPLARS_ENABLED →
   * io.prometheus.metrics.exemplars_enabled
   *
   * @param envVar the environment variable name
   * @return the normalized property key
   */
  static String normalizeEnvironmentVariableKey(String envVar) {
    String lower = envVar.toLowerCase(java.util.Locale.ROOT);
    // Replace underscores with dots only in known prefixes
    // Try longest prefixes first to handle nested namespaces correctly
    if (lower.startsWith("io_prometheus_exporter_opentelemetry_")) {
      return lower.replace(
          "io_prometheus_exporter_opentelemetry_", "io.prometheus.exporter.opentelemetry.");
    } else if (lower.startsWith("io_prometheus_exporter_filter_")) {
      return lower.replace("io_prometheus_exporter_filter_", "io.prometheus.exporter.filter.");
    } else if (lower.startsWith("io_prometheus_exporter_httpserver_")
        || lower.startsWith("io_prometheus_exporter_http_server_")) {
      return lower
          .replace("io_prometheus_exporter_httpserver_", "io.prometheus.exporter.http_server.")
          .replace("io_prometheus_exporter_http_server_", "io.prometheus.exporter.http_server.");
    } else if (lower.startsWith("io_prometheus_exporter_pushgateway_")) {
      return lower.replace(
          "io_prometheus_exporter_pushgateway_", "io.prometheus.exporter.pushgateway.");
    } else if (lower.startsWith("io_prometheus_metrics_")) {
      return lower.replace("io_prometheus_metrics_", "io.prometheus.metrics.");
    } else if (lower.startsWith("io_prometheus_exporter_")) {
      return lower.replace("io_prometheus_exporter_", "io.prometheus.exporter.");
    } else if (lower.startsWith("io_prometheus_exemplars_")) {
      return lower.replace("io_prometheus_exemplars_", "io.prometheus.exemplars.");
    } else {
      // Fallback for unknown prefixes
      return lower.replace("io_prometheus_", "io.prometheus.");
    }
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
