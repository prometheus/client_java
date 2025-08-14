package io.prometheus.metrics.exporter.opentelemetry;

import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class OpenTelemetryExporter implements AutoCloseable {
  private final MetricReader reader;

  public OpenTelemetryExporter(MetricReader reader) {
    this.reader = reader;
  }

  @Override
  public void close() {
    reader.shutdown();
  }

  public static Builder builder() {
    return new Builder(PrometheusProperties.get());
  }

  public static Builder builder(PrometheusProperties config) {
    return new Builder(config);
  }

  public static class Builder {

    private final PrometheusProperties config;
    @Nullable private PrometheusRegistry registry = null;
    @Nullable String protocol;
    @Nullable String endpoint;
    final Map<String, String> headers = new HashMap<>();
    @Nullable String interval;
    @Nullable String timeout;
    @Nullable String serviceName;
    @Nullable String serviceNamespace;
    @Nullable String serviceInstanceId;
    @Nullable String serviceVersion;
    final Map<String, String> resourceAttributes = new HashMap<>();

    private Builder(PrometheusProperties config) {
      this.config = config;
    }

    public Builder registry(PrometheusRegistry registry) {
      this.registry = registry;
      return this;
    }

    /**
     * Specifies the OTLP transport protocol to be used when exporting metrics.
     *
     * <p>Supported values are {@code "grpc"} and {@code "http/protobuf"}. Default is {@code
     * "grpc"}.
     *
     * <p>See OpenTelemetry's <a
     * href="https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/#otel_exporter_otlp_protocol">OTEL_EXPORTER_OTLP_PROTOCOL</a>.
     */
    public Builder protocol(String protocol) {
      if (!protocol.equals("grpc") && !protocol.equals("http/protobuf")) {
        throw new IllegalArgumentException(
            protocol + ": Unsupported protocol. Expecting grpc or http/protobuf");
      }
      this.protocol = protocol;
      return this;
    }

    /**
     * The OTLP endpoint to send metric data to.
     *
     * <p>The default depends on the protocol:
     *
     * <ul>
     *   <li>{@code "grpc"}: {@code "http://localhost:4317"}
     *   <li>{@code "http/protobuf"}: {@code "http://localhost:4318/v1/metrics"}
     * </ul>
     *
     * If the protocol is {@code "http/protobuf"} and the endpoint does not have the {@code
     * "/v1/metrics"} suffix, the {@code "/v1/metrics"} suffix will automatically be appended.
     *
     * <p>See OpenTelemetry's <a
     * href="https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/#otel_exporter_otlp_metrics_endpoint">OTEL_EXPORTER_OTLP_METRICS_ENDPOINT</a>.
     */
    public Builder endpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    /**
     * Add an HTTP header to be applied to outgoing requests. Call multiple times to add multiple
     * headers.
     *
     * <p>See OpenTelemetry's <a
     * href="https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/#otel_exporter_otlp_headers">OTEL_EXPORTER_OTLP_HEADERS</a>.
     */
    public Builder header(String name, String value) {
      this.headers.put(name, value);
      return this;
    }

    /**
     * The interval between the start of two export attempts. Default is 60000.
     *
     * <p>Like OpenTelemetry's <a
     * href="https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#periodic-metric-reader">OTEL_METRIC_EXPORT_INTERVAL</a>,
     * but in seconds rather than milliseconds.
     */
    public Builder intervalSeconds(int intervalSeconds) {
      if (intervalSeconds <= 0) {
        throw new IllegalStateException(intervalSeconds + ": expecting a push interval > 0s");
      }
      this.interval = intervalSeconds + "s";
      return this;
    }

    /**
     * The timeout for outgoing requests. Default is 10.
     *
     * <p>Like OpenTelemetry's <a
     * href="https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/#otel_exporter_otlp_metrics_timeout">OTEL_EXPORTER_OTLP_METRICS_TIMEOUT</a>,
     * but in seconds rather than milliseconds.
     */
    public Builder timeoutSeconds(int timeoutSeconds) {
      if (timeoutSeconds <= 0) {
        throw new IllegalStateException(timeoutSeconds + ": expecting a push interval > 0s");
      }
      this.timeout = timeoutSeconds + "s";
      return this;
    }

    /**
     * The {@code service.name} resource attribute.
     *
     * <p>If not explicitly specified, {@code client_java} will try to initialize it with a
     * reasonable default, like the JAR file name.
     *
     * <p>See {@code service.name} in OpenTelemetry's <a
     * href="https://opentelemetry.io/docs/specs/otel/resource/semantic_conventions/#service">Resource
     * Semantic Conventions</a>.
     */
    public Builder serviceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    /**
     * The {@code service.namespace} resource attribute.
     *
     * <p>See {@code service.namespace} in OpenTelemetry's <a
     * href="https://opentelemetry.io/docs/specs/otel/resource/semantic_conventions/#service-experimental">Resource
     * Semantic Conventions</a>.
     */
    public Builder serviceNamespace(String serviceNamespace) {
      this.serviceNamespace = serviceNamespace;
      return this;
    }

    /**
     * The {@code service.instance.id} resource attribute.
     *
     * <p>See {@code service.instance.id} in OpenTelemetry's <a
     * href="https://opentelemetry.io/docs/specs/otel/resource/semantic_conventions/#service-experimental">Resource
     * Semantic Conventions</a>.
     */
    public Builder serviceInstanceId(String serviceInstanceId) {
      this.serviceInstanceId = serviceInstanceId;
      return this;
    }

    /**
     * The {@code service.version} resource attribute.
     *
     * <p>See {@code service.version} in OpenTelemetry's <a
     * href="https://opentelemetry.io/docs/specs/otel/resource/semantic_conventions/#service-experimental">Resource
     * Semantic Conventions</a>.
     */
    public Builder serviceVersion(String serviceVersion) {
      this.serviceVersion = serviceVersion;
      return this;
    }

    /**
     * Add a resource attribute. Call multiple times to add multiple resource attributes.
     *
     * <p>See OpenTelemetry's <a
     * href="https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#general-sdk-configuration">OTEL_RESOURCE_ATTRIBUTES</a>.
     */
    public Builder resourceAttribute(String name, String value) {
      this.resourceAttributes.put(name, value);
      return this;
    }

    public OpenTelemetryExporter buildAndStart() {
      if (registry == null) {
        registry = PrometheusRegistry.defaultRegistry;
      }
      return new OpenTelemetryExporter(OtelAutoConfig.createReader(this, config, registry));
    }
  }
}
