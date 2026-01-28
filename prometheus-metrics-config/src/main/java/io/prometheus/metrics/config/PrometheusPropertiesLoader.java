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
    properties.putAll(loadPropertiesFromClasspath());
    properties.putAll(loadPropertiesFromFile()); // overriding the entries from the classpath file
    // overriding the entries from the properties file
    // copy System properties to avoid ConcurrentModificationException
    System.getProperties().stringPropertyNames().stream()
        .filter(key -> key.startsWith("io.prometheus"))
        .forEach(key -> properties.put(key, System.getProperty(key)));
    properties.putAll(loadPropertiesFromEnvironment()); // overriding with environment variables
    properties.putAll(externalProperties); // overriding all the entries above
    return properties;
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
   * IO_PROMETHEUS_METRICS_EXEMPLARS_ENABLED becomes io.prometheus.metrics.exemplarsEnabled.
   *
   * <p>Only environment variables starting with IO_PROMETHEUS are considered.
   *
   * @return properties loaded from environment variables
   */
  private static Map<Object, Object> loadPropertiesFromEnvironment() {
    Map<Object, Object> properties = new HashMap<>();
    System.getenv().forEach(
        (key, value) -> {
          if (key.startsWith("IO_PROMETHEUS")) {
            String propertyKey = convertEnvVarToPropertyKey(key);
            properties.put(propertyKey, value);
          }
        });
    return properties;
  }

  /**
   * Convert an environment variable name to a property key.
   *
   * <p>For example: IO_PROMETHEUS_METRICS_EXEMPLARS_ENABLED →
   * io.prometheus.metrics.exemplarsEnabled
   *
   * <p>The conversion follows these rules:
   *
   * <ul>
   *   <li>Convert to lowercase
   *   <li>Replace underscores with dots
   *   <li>Apply camelCase for the last segment after dots (e.g., EXEMPLARS_ENABLED →
   *       exemplarsEnabled)
   * </ul>
   *
   * @param envVar the environment variable name
   * @return the property key
   */
  static String convertEnvVarToPropertyKey(String envVar) {
    // Convert to lowercase and split by underscore
    String lower = envVar.toLowerCase(java.util.Locale.ROOT);
    String[] parts = lower.split("_");

    // Find the index where camelCase should start
    // This is the index of the first property-specific part after the prefix
    // Examples:
    // - io.prometheus.metrics.PROPERTY_NAME -> start camelCase at index 3
    // - io.prometheus.exporter.PROPERTY_NAME -> start camelCase at index 3
    // - io.prometheus.exporter.opentelemetry.PROPERTY_NAME -> start camelCase at index 4
    // - io.prometheus.exporter.filter.PROPERTY_NAME -> start camelCase at index 4
    int camelCaseStartIndex = findCamelCaseStartIndex(parts);

    StringBuilder result = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i == 0) {
        result.append(parts[i]);
      } else if (i < camelCaseStartIndex) {
        result.append('.').append(parts[i]);
      } else if (i == camelCaseStartIndex) {
        // First camelCase word - add dot and lowercase
        result.append('.').append(parts[i]);
      } else {
        // Subsequent camelCase words - capitalize first letter
        result.append(Character.toUpperCase(parts[i].charAt(0)));
        if (parts[i].length() > 1) {
          result.append(parts[i].substring(1));
        }
      }
    }
    return result.toString();
  }

  private static int findCamelCaseStartIndex(String[] parts) {
    // Known prefixes that use dots:
    // - io.prometheus.metrics
    // - io.prometheus.exporter.opentelemetry
    // - io.prometheus.exporter.filter
    // - io.prometheus.exporter.httpServer
    // - io.prometheus.exporter.pushgateway
    // - io.prometheus.exemplars
    if (parts.length >= 4
        && parts[0].equals("io")
        && parts[1].equals("prometheus")
        && parts[2].equals("exporter")
        && (parts[3].equals("opentelemetry")
            || parts[3].equals("filter")
            || parts[3].equals("httpserver")
            || parts[3].equals("pushgateway"))) {
      return 4;
    }
    // Default case: io.prometheus.metrics or io.prometheus.exporter or io.prometheus.exemplars
    return 3;
  }
}
