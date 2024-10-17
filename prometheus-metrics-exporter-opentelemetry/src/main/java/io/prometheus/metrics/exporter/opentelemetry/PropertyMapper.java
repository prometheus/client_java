package io.prometheus.metrics.exporter.opentelemetry;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.prometheus.metrics.config.ExporterOpenTelemetryProperties;
import io.prometheus.metrics.config.PrometheusPropertiesException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class PropertyMapper {

  private static final String METRICS_ENDPOINT = "otel.exporter.otlp.metrics.endpoint";
  Map<String, String> configLowPriority = new HashMap<>();
  Map<String, String> configHighPriority = new HashMap<>();

  static PropertyMapper create(
      ExporterOpenTelemetryProperties properties, OpenTelemetryExporter.Builder builder)
      throws PrometheusPropertiesException {
    return new PropertyMapper()
        .addString(
            builder.protocol, properties.getProtocol(), "otel.exporter.otlp.metrics.protocol")
        .addString(builder.endpoint, properties.getEndpoint(), METRICS_ENDPOINT)
        .addString(
            mapToOtelString(builder.headers),
            mapToOtelString(properties.getHeaders()),
            "otel.exporter.otlp.metrics.headers")
        .addString(builder.interval, properties.getInterval(), "otel.metric.export.interval")
        .addString(builder.timeout, properties.getTimeout(), "otel.exporter.otlp.metrics.timeout")
        .addString(builder.serviceName, properties.getServiceName(), "otel.service.name");
  }

  PropertyMapper addString(String builderValue, String propertyValue, String otelKey) {
    if (builderValue != null) {
      // the low priority config should not be used for the metrics settings, so that both general
      // and metrics settings
      // can be used to override the values
      configLowPriority.put(otelKey.replace("otlp.metrics", "otlp"), builderValue);
    }
    if (propertyValue != null) {
      configHighPriority.put(otelKey, propertyValue);
    }
    return this;
  }

  private static String mapToOtelString(Map<String, String> map) {
    if (map.isEmpty()) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
    }
    return sb.substring(0, sb.length() - 1);
  }

  static Map<String, String> customizeProperties(Map<String, String> result, ConfigProperties c) {
    Map<String, String> map = fixEndpointPaths(result, c);
    map.put("otel.logs.exporter", "none");
    map.put("otel.traces.exporter", "none");
    return map;
  }

  static Map<String, String> fixEndpointPaths(Map<String, String> result, ConfigProperties c) {
    transformEndpointPath(
        result,
        c,
        METRICS_ENDPOINT,
        endpoint -> {
          if (!endpoint.endsWith("v1/metrics")) {
            if (!endpoint.endsWith("/")) {
              return endpoint + "/v1/metrics";
            } else {
              return endpoint + "v1/metrics";
            }
          }
          return endpoint;
        });

    transformEndpointPath(
        result,
        c,
        "otel.exporter.otlp.endpoint",
        endpoint -> {
          if (endpoint.endsWith("v1/metrics")) {
            return endpoint.substring(0, endpoint.length() - "v1/metrics".length());
          }
          return endpoint;
        });

    return result;
  }

  static void transformEndpointPath(
      Map<String, String> result,
      ConfigProperties c,
      String key,
      Function<String, String> valueMapper) {
    String endpoint = c.getString(key);
    if (endpoint == null) {
      return;
    }
    String protocol = c.getString("otel.exporter.otlp.metrics.protocol");
    if (protocol == null) {
      protocol = c.getString("otel.exporter.otlp.protocol");
    }

    if (!"grpc".equals(protocol)) { // http/protobuf
      endpoint = valueMapper.apply(endpoint);
      result.put(key, endpoint);
    }
  }
}
