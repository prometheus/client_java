package io.prometheus.metrics.exporter.opentelemetry;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class PrometheusInstrumentationScope {

  private static final String instrumentationScopePropertiesFile =
      "instrumentationScope.properties";
  private static final String instrumentationScopeNameKey = "instrumentationScope.name";
  private static final String instrumentationScopeVersionKey = "instrumentationScope.version";

  public static InstrumentationScopeInfo loadInstrumentationScopeInfo() {
    return loadInstrumentationScopeInfo(
        instrumentationScopePropertiesFile,
        instrumentationScopeNameKey,
        instrumentationScopeVersionKey);
  }

  static InstrumentationScopeInfo loadInstrumentationScopeInfo(
      String path, String nameKey, String versionKey) {
    try {
      Properties properties = new Properties();
      InputStream stream =
          PrometheusInstrumentationScope.class.getClassLoader().getResourceAsStream(path);
      if (stream == null) {
        throw new IllegalStateException(
            "Prometheus metrics library initialization error: Failed to read "
                + path
                + " from classpath.");
      }
      properties.load(stream);
      String instrumentationScopeName = properties.getProperty(nameKey);
      if (instrumentationScopeName == null) {
        throw new IllegalStateException(
            "Prometheus metrics library initialization error: "
                + nameKey
                + " not found in "
                + path
                + " in classpath.");
      }
      String instrumentationScopeVersion = properties.getProperty(versionKey);
      if (instrumentationScopeVersion == null) {
        throw new IllegalStateException(
            "Prometheus metrics library initialization error: "
                + versionKey
                + " not found in "
                + path
                + " in classpath.");
      }
      return InstrumentationScopeInfo.builder(instrumentationScopeName)
          .setVersion(instrumentationScopeVersion)
          .build();
    } catch (IOException e) {
      throw new IllegalStateException(
          "Prometheus metrics library initialization error: Failed to read "
              + path
              + " from classpath.",
          e);
    }
  }
}
