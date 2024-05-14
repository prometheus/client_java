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
 * <p>
 * It would be great to implement a subset of
 * <a href="https://docs.spring.io/spring-boot/docs/3.1.x/reference/html/features.html#features.external-config">Spring Boot's Externalized Configuration</a>,
 * like support for YAML, Properties, and env vars, or support for Spring's naming conventions for properties.
 */
public class PrometheusPropertiesLoader {

    /**
     * See {@link PrometheusProperties#get()}.
     */
    public static PrometheusProperties load() throws PrometheusPropertiesException {
        return load(new Properties());
    }

    public static PrometheusProperties load(Map<Object, Object> externalProperties) throws PrometheusPropertiesException {
        Map<Object, Object> properties = loadProperties(externalProperties);
        Map<String, MetricsProperties> metricsConfigs = loadMetricsConfigs(properties);
        MetricsProperties defaultMetricsProperties = MetricsProperties.load("io.prometheus.metrics", properties);
        ExemplarsProperties exemplarConfig = ExemplarsProperties.load("io.prometheus.exemplars", properties);
        ExporterProperties exporterProperties = ExporterProperties.load("io.prometheus.exporter", properties);
        ExporterFilterProperties exporterFilterProperties = ExporterFilterProperties.load("io.prometheus.exporter.filter", properties);
        ExporterHttpServerProperties exporterHttpServerProperties = ExporterHttpServerProperties.load("io.prometheus.exporter.httpServer", properties);
        ExporterPushgatewayProperties exporterPushgatewayProperties = ExporterPushgatewayProperties.load("io.prometheus.exporter.pushgateway", properties);
        ExporterOpenTelemetryProperties exporterOpenTelemetryProperties = ExporterOpenTelemetryProperties.load("io.prometheus.exporter.opentelemetry", properties);
        validateAllPropertiesProcessed(properties);
        return new PrometheusProperties(defaultMetricsProperties, metricsConfigs, exemplarConfig, exporterProperties, exporterFilterProperties, exporterHttpServerProperties, exporterPushgatewayProperties, exporterOpenTelemetryProperties);
    }

    // This will remove entries from properties when they are processed.
    private static Map<String, MetricsProperties> loadMetricsConfigs(Map<Object, Object> properties) {
        Map<String, MetricsProperties> result = new HashMap<>();
        // Note that the metric name in the properties file must be as exposed in the Prometheus exposition formats,
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
                    result.put(metricName, MetricsProperties.load("io.prometheus.metrics." + metricName, properties));
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
        properties.putAll(System.getProperties()); // overriding the entries from the properties file
        properties.putAll(externalProperties); // overriding all the entries above
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
