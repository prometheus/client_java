package io.prometheus.metrics.exporter.opentelemetry;

import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.common.InstrumentationScopeInfo;

import java.util.Properties;

class PrometheusInstrumentationScope {

    private static final String instrumentationScopePropertiesFile = "instrumentationScope.properties";
    private static final String instrumentationScopeNameKey = "instrumentationScope.name";
    private static final String instrumentationScopeVersionKey = "instrumentationScope.version";

    public static InstrumentationScopeInfo loadInstrumentationScopeInfo() {
        try {
            Properties properties = new Properties();
            properties.load(PrometheusInstrumentationScope.class.getClassLoader().getResourceAsStream(instrumentationScopePropertiesFile));
            String instrumentationScopeName = properties.getProperty(instrumentationScopeNameKey);
            if (instrumentationScopeName == null) {
                throw new IllegalStateException("Prometheus metrics library initialization error: " + instrumentationScopeNameKey + " not found in " + instrumentationScopePropertiesFile + " in classpath.");
            }
            String instrumentationScopeVersion = properties.getProperty(instrumentationScopeVersionKey);
            if (instrumentationScopeVersion == null) {
                throw new IllegalStateException("Prometheus metrics library initialization error: " + instrumentationScopeVersionKey + " not found in " + instrumentationScopePropertiesFile + " in classpath.");
            }
            return InstrumentationScopeInfo.builder(instrumentationScopeName)
                    .setVersion(instrumentationScopeVersion)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Prometheus metrics library initialization error: Failed to read " + instrumentationScopePropertiesFile + " from classpath.", e);
        }
    }
}
