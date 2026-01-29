package io.prometheus.metrics.config;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Properties for configuring the OpenTelemetry exporter.
 *
 * <p>These properties can be configured via {@code prometheus.properties}, system properties, or
 * programmatically.
 *
 * <p>All properties are prefixed with {@code io.prometheus.exporter.opentelemetry}.
 *
 * <p>Available properties:
 *
 * <ul>
 *   <li>{@code protocol} - OTLP protocol: {@code "grpc"} or {@code "http/protobuf"}
 *   <li>{@code endpoint} - OTLP endpoint URL
 *   <li>{@code headers} - HTTP headers for outgoing requests
 *   <li>{@code intervalSeconds} - Export interval in seconds
 *   <li>{@code timeoutSeconds} - Request timeout in seconds
 *   <li>{@code serviceName} - Service name resource attribute
 *   <li>{@code serviceNamespace} - Service namespace resource attribute
 *   <li>{@code serviceInstanceId} - Service instance ID resource attribute
 *   <li>{@code serviceVersion} - Service version resource attribute
 *   <li>{@code resourceAttributes} - Additional resource attributes
 * </ul>
 *
 * @see <a
 *     href="https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/">OpenTelemetry
 *     SDK Environment Variables</a>
 */
public class ExporterOpenTelemetryProperties {

  // See
  // https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md
  private static final String PROTOCOL = "protocol"; // otel.exporter.otlp.protocol
  private static final String ENDPOINT = "endpoint"; // otel.exporter.otlp.endpoint
  private static final String HEADERS = "headers"; // otel.exporter.otlp.headers
  private static final String INTERVAL_SECONDS = "intervalSeconds"; // otel.metric.export.interval
  private static final String TIMEOUT_SECONDS = "timeoutSeconds"; // otel.exporter.otlp.timeout
  private static final String SERVICE_NAME = "serviceName"; // otel.service.name
  private static final String SERVICE_NAMESPACE = "serviceNamespace";
  private static final String SERVICE_INSTANCE_ID = "serviceInstanceId";
  private static final String SERVICE_VERSION = "serviceVersion";
  private static final String RESOURCE_ATTRIBUTES =
      "resourceAttributes"; // otel.resource.attributes
  private static final String PREFIX = "io.prometheus.exporter.opentelemetry";

  @Nullable private final String endpoint;
  @Nullable private final String protocol;
  private final Map<String, String> headers;
  @Nullable private final String interval;
  @Nullable private final String timeout;
  @Nullable private final String serviceName;
  @Nullable private final String serviceNamespace;
  @Nullable private final String serviceInstanceId;
  @Nullable private final String serviceVersion;
  private final Map<String, String> resourceAttributes;

  private ExporterOpenTelemetryProperties(
      @Nullable String protocol,
      @Nullable String endpoint,
      Map<String, String> headers,
      @Nullable String interval,
      @Nullable String timeout,
      @Nullable String serviceName,
      @Nullable String serviceNamespace,
      @Nullable String serviceInstanceId,
      @Nullable String serviceVersion,
      Map<String, String> resourceAttributes) {
    this.protocol = protocol;
    this.endpoint = endpoint;
    this.headers = headers;
    this.interval = interval;
    this.timeout = timeout;
    this.serviceName = serviceName;
    this.serviceNamespace = serviceNamespace;
    this.serviceInstanceId = serviceInstanceId;
    this.serviceVersion = serviceVersion;
    this.resourceAttributes = resourceAttributes;
  }

  @Nullable
  public String getProtocol() {
    return protocol;
  }

  @Nullable
  public String getEndpoint() {
    return endpoint;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  @Nullable
  public String getInterval() {
    return interval;
  }

  @Nullable
  public String getTimeout() {
    return timeout;
  }

  @Nullable
  public String getServiceName() {
    return serviceName;
  }

  @Nullable
  public String getServiceNamespace() {
    return serviceNamespace;
  }

  @Nullable
  public String getServiceInstanceId() {
    return serviceInstanceId;
  }

  @Nullable
  public String getServiceVersion() {
    return serviceVersion;
  }

  public Map<String, String> getResourceAttributes() {
    return resourceAttributes;
  }

  /**
   * Note that this will remove entries from {@code properties}. This is because we want to know if
   * there are unused properties remaining after all properties have been loaded.
   */
  static ExporterOpenTelemetryProperties load(Map<Object, Object> properties)
      throws PrometheusPropertiesException {
    String protocol = Util.loadString(PREFIX + "." + PROTOCOL, properties);
    String endpoint = Util.loadString(PREFIX + "." + ENDPOINT, properties);
    Map<String, String> headers = Util.loadMap(PREFIX + "." + HEADERS, properties);
    String interval = Util.loadStringAddSuffix(PREFIX + "." + INTERVAL_SECONDS, properties, "s");
    String timeout = Util.loadStringAddSuffix(PREFIX + "." + TIMEOUT_SECONDS, properties, "s");
    String serviceName = Util.loadString(PREFIX + "." + SERVICE_NAME, properties);
    String serviceNamespace = Util.loadString(PREFIX + "." + SERVICE_NAMESPACE, properties);
    String serviceInstanceId = Util.loadString(PREFIX + "." + SERVICE_INSTANCE_ID, properties);
    String serviceVersion = Util.loadString(PREFIX + "." + SERVICE_VERSION, properties);
    Map<String, String> resourceAttributes =
        Util.loadMap(PREFIX + "." + RESOURCE_ATTRIBUTES, properties);
    return new ExporterOpenTelemetryProperties(
        protocol,
        endpoint,
        headers,
        interval,
        timeout,
        serviceName,
        serviceNamespace,
        serviceInstanceId,
        serviceVersion,
        resourceAttributes);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    @Nullable private String protocol;
    @Nullable private String endpoint;
    private final Map<String, String> headers = new HashMap<>();
    @Nullable private String interval;
    @Nullable private String timeout;
    @Nullable private String serviceName;
    @Nullable private String serviceNamespace;
    @Nullable private String serviceInstanceId;
    @Nullable private String serviceVersion;
    private final Map<String, String> resourceAttributes = new HashMap<>();

    private Builder() {}

    /**
     * The OTLP protocol to use.
     *
     * <p>Supported values: {@code "grpc"} or {@code "http/protobuf"}.
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
        throw new IllegalArgumentException(intervalSeconds + ": Expecting intervalSeconds > 0");
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
        throw new IllegalArgumentException(timeoutSeconds + ": Expecting timeoutSeconds > 0");
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

    public ExporterOpenTelemetryProperties build() {
      return new ExporterOpenTelemetryProperties(
          protocol,
          endpoint,
          headers,
          interval,
          timeout,
          serviceName,
          serviceNamespace,
          serviceInstanceId,
          serviceVersion,
          resourceAttributes);
    }
  }
}
