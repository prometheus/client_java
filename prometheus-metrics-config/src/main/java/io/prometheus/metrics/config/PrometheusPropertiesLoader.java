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

public class PrometheusPropertiesLoader {

    public static PrometheusProperties load() throws PrometheusPropertiesException {
        Map<Object, Object> properties = loadProperties();
        Map<String, MetricProperties> metricsConfigs = loadMetricsConfigs(properties);
        MetricProperties defaultMetricProperties = MetricProperties.load("io.prometheus.metrics", properties);
        ExemplarProperties exemplarConfig = ExemplarProperties.load("io.prometheus.exemplars", properties);
        ExpositionFormatProperties expositionFormatProperties = ExpositionFormatProperties.load("io.prometheus.expositionFormat", properties);
        HttpServerProperties httpServerConfig = HttpServerProperties.load("io.prometheus.httpServer", properties);
        validateAllPropertiesProcessed(properties);
        return new PrometheusProperties(defaultMetricProperties, metricsConfigs, exemplarConfig, expositionFormatProperties, httpServerConfig);
    }

    // This will remove entries from properties when they are processed.
    private static Map<String, MetricProperties> loadMetricsConfigs(Map<Object, Object> properties) {
        Map<String, MetricProperties> result = new HashMap<>();
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
                String metricName = matcher.group(1);
                if (!result.containsKey(metricName)) {
                    result.put(metricName, MetricProperties.load("io.prometheus.metrics." + metricName, properties));
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

    private static Map<Object, Object> loadProperties() {
        Map<Object, Object> properties = new HashMap<>();
        properties.putAll(loadPropertiesFromClasspath());
        properties.putAll(loadPropertiesFromFile()); // overriding the entries from the classpath file
        properties.putAll(System.getProperties()); // overriding the entries from the properties file
        // TODO: Add environment variables like EXEMPLARS_ENABLED.
        return properties;
    }

    private static Properties loadPropertiesFromClasspath() {
        Properties properties = new Properties();
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("prometheus.properties")) {
            properties.load(stream);
        } catch (Exception ignored) {
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
                throw new PrometheusPropertiesException("Failed to read Prometheus properties from " + path + ": " + e.getMessage(), e);
            }
        }
        return properties;
    }
}
