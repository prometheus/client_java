package io.prometheus.metrics.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * PropertySource encapsulates three separate property maps with different priorities and
 * transformation rules.
 *
 * <p>Properties are checked in the following order:
 *
 * <ol>
 *   <li>External properties (highest priority) - exact key match
 *   <li>Environment variables (second priority) - with dot-to-underscore transformation
 *   <li>Regular properties (lowest priority) - from system properties, files, and classpath
 * </ol>
 *
 * <p>Property lookups remove entries from the maps. This allows detecting unused properties at the
 * end of configuration loading.
 */
class PropertySource {

  private final Map<Object, Object> externalProperties; // Highest priority
  private final Map<Object, Object> envVarProperties; // Second priority
  private final Map<Object, Object> regularProperties; // Lowest priority

  /**
   * Creates a PropertySource with three separate property maps.
   *
   * @param externalProperties properties passed explicitly (e.g., from application code)
   * @param envVarProperties properties from environment variables (keys in env var format with
   *     underscores, lowercase)
   * @param regularProperties properties from system properties, files, and classpath (keys
   *     normalized to snake_case)
   */
  PropertySource(
      Map<Object, Object> externalProperties,
      Map<Object, Object> envVarProperties,
      Map<Object, Object> regularProperties) {
    this.externalProperties = externalProperties;
    this.envVarProperties = envVarProperties;
    this.regularProperties = regularProperties;
  }

  /**
   * Creates a PropertySource with only regular properties (for testing).
   *
   * <p>Creates empty maps for external and environment variable properties.
   *
   * @param regularProperties properties from system properties, files, and classpath (keys
   *     normalized to snake_case)
   */
  PropertySource(Map<Object, Object> regularProperties) {
    this(new HashMap<>(), new HashMap<>(), regularProperties);
  }

  /**
   * Gets a property value by key, checking all three sources in priority order.
   *
   * <p>For external and regular properties, performs exact key match. For environment variables,
   * transforms dots to underscores before lookup (e.g., "io.prometheus.metrics.exemplars_enabled"
   * becomes "io_prometheus_metrics_exemplars_enabled").
   *
   * <p>Removes the property from ALL source maps to prevent duplicate detection during validation.
   *
   * @param key the property key to look up
   * @return the property value, or null if not found
   */
  @Nullable
  String getProperty(String key) {
    String result = null;

    // Check external properties first (highest priority)
    Object value = externalProperties.remove(key);
    if (value != null) {
      result = value.toString();
    }

    // Check env vars with transformation (second priority)
    // Transform dots to underscores: io.prometheus.metrics.exemplars_enabled
    // -> io_prometheus_metrics_exemplars_enabled
    String envKey = key.replace(".", "_");
    Object envValue = envVarProperties.remove(envKey);
    if (result == null && envValue != null) {
      result = envValue.toString();
    }

    // Check regular properties last (lowest priority)
    Object regularValue = regularProperties.remove(key);
    if (result == null && regularValue != null) {
      result = regularValue.toString();
    }

    return result;
  }

  /**
   * Gets a property value by prefix and property name.
   *
   * <p>This is a convenience method that concatenates the prefix and property name with a dot and
   * calls {@link #getProperty(String)}.
   *
   * @param prefix the property prefix (e.g., "io.prometheus.metrics"), or empty string for no
   *     prefix
   * @param propertyName the property name (e.g., "exemplars_enabled")
   * @return the property value, or null if not found
   */
  @Nullable
  String getProperty(String prefix, String propertyName) {
    String fullKey = prefix.isEmpty() ? propertyName : prefix + "." + propertyName;
    return getProperty(fullKey);
  }

  /**
   * Returns all keys from all three property sources.
   *
   * <p>Keys are returned in the format they are stored in each source: external and regular
   * properties typically use dot-separated keys, while environment variables are exposed in their
   * underscore form (e.g., "io_prometheus_metrics_exemplars_enabled").
   *
   * <p>This is used for pattern matching to find metric-specific configurations.
   *
   * @return a set of all property keys
   */
  Set<String> getAllKeys() {
    Set<String> allKeys = new HashSet<>();
    for (Object key : externalProperties.keySet()) {
      allKeys.add(key.toString());
    }
    // Include env var keys as stored (underscore-separated, lowercase)
    for (Object key : envVarProperties.keySet()) {
      String envKey = key.toString();
      allKeys.add(envKey);
    }
    for (Object key : regularProperties.keySet()) {
      allKeys.add(key.toString());
    }
    return allKeys;
  }

  /**
   * Returns all remaining keys from all three property sources.
   *
   * <p>This is used for validation to detect unused properties that might indicate configuration
   * errors.
   *
   * @return a set of all remaining property keys
   */
  Set<String> getRemainingKeys() {
    Set<String> remainingKeys = new HashSet<>();
    for (Object key : externalProperties.keySet()) {
      remainingKeys.add(key.toString());
    }
    for (Object key : envVarProperties.keySet()) {
      remainingKeys.add(key.toString());
    }
    for (Object key : regularProperties.keySet()) {
      remainingKeys.add(key.toString());
    }
    return remainingKeys;
  }
}
