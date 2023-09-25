package io.prometheus.metrics.config;

import java.util.HashMap;
import java.util.Map;

// TODO: JavaDoc is currently only in OpenTelemetryExporter.Builder. Look there for reference.
public class ExporterOpenTelemetryProperties {

    // See https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md
    private static String PROTOCOL = "protocol"; // otel.exporter.otlp.protocol
    private static String ENDPOINT = "endpoint"; // otel.exporter.otlp.endpoint
    private static String HEADERS = "headers"; // otel.exporter.otlp.headers
    private static String INTERVAL_SECONDS = "intervalSeconds"; // otel.metric.export.interval
    private static String TIMEOUT_SECONDS = "timeoutSeconds"; // otel.exporter.otlp.timeout
    private static String SERVICE_NAME = "serviceName"; // otel.service.name
    private static String SERVICE_NAMESPACE = "serviceNamespace";
    private static String SERVICE_INSTANCE_ID = "serviceInstanceId";
    private static String SERVICE_VERSION = "serviceVersion";
    private static String RESOURCE_ATTRIBUTES = "resourceAttributes"; // otel.resource.attributes

    private final String protocol;
    private final String endpoint;
    private final Map<String, String> headers;
    private final Integer intervalSeconds;
    private final Integer timeoutSeconds;
    private final String serviceName;
    private final String serviceNamespace;
    private final String serviceInstanceId;
    private final String serviceVersion;
    private final Map<String, String> resourceAttributes;

    private ExporterOpenTelemetryProperties(String protocol, String endpoint, Map<String, String> headers, Integer intervalSeconds, Integer timeoutSeconds, String serviceName, String serviceNamespace, String serviceInstanceId, String serviceVersion, Map<String, String> resourceAttributes) {
        this.protocol = protocol;
        this.endpoint = endpoint;
        this.headers = headers;
        this.intervalSeconds = intervalSeconds;
        this.timeoutSeconds = timeoutSeconds;
        this.serviceName = serviceName;
        this.serviceNamespace = serviceNamespace;
        this.serviceInstanceId = serviceInstanceId;
        this.serviceVersion = serviceVersion;
        this.resourceAttributes = resourceAttributes;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceNamespace() {
        return serviceNamespace;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public Map<String, String> getResourceAttributes() {
        return resourceAttributes;
    }

    /**
     * Note that this will remove entries from {@code properties}.
     * This is because we want to know if there are unused properties remaining after all properties have been loaded.
     */
    static ExporterOpenTelemetryProperties load(String prefix, Map<Object, Object> properties) throws PrometheusPropertiesException {
        String protocol = Util.loadString(prefix + "." + PROTOCOL, properties);
        String endpoint = Util.loadString(prefix + "." + ENDPOINT, properties);
        Map<String, String> headers = Util.loadMap(prefix + "." + HEADERS, properties);
        Integer intervalSeconds = Util.loadInteger(prefix + "." + INTERVAL_SECONDS, properties);
        Integer timeoutSeconds = Util.loadInteger(prefix + "." + TIMEOUT_SECONDS, properties);
        String serviceName = Util.loadString(prefix + "." + SERVICE_NAME, properties);
        String serviceNamespace = Util.loadString(prefix + "." + SERVICE_NAMESPACE, properties);
        String serviceInstanceId = Util.loadString(prefix + "." + SERVICE_INSTANCE_ID, properties);
        String serviceVersion = Util.loadString(prefix + "." + SERVICE_VERSION, properties);
        Map<String, String> resourceAttributes = Util.loadMap(prefix + "." + RESOURCE_ATTRIBUTES, properties);
        Util.assertValue(intervalSeconds, t -> t > 0, "Expecting value > 0", prefix, INTERVAL_SECONDS);
        Util.assertValue(timeoutSeconds, t -> t > 0, "Expecting value > 0", prefix, TIMEOUT_SECONDS);
        if (protocol != null && !protocol.equals("grpc") && !protocol.equals("http/protobuf")) {
            throw new PrometheusPropertiesException(protocol + ": Unsupported OpenTelemetry exporter protocol. Expecting grpc or http/protobuf");
        }
        return new ExporterOpenTelemetryProperties(protocol, endpoint, headers, intervalSeconds, timeoutSeconds, serviceName, serviceNamespace, serviceInstanceId, serviceVersion, resourceAttributes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String protocol;
        private String endpoint;
        private Map<String, String> headers = new HashMap<>();
        private Integer intervalSeconds;
        private Integer timeoutSeconds;
        private String serviceName;
        private String serviceNamespace;
        private String serviceInstanceId;
        private String serviceVersion;
        private Map<String, String> resourceAttributes = new HashMap<>();

        private Builder() {}

        public Builder protocol(String protocol) {
            if (!protocol.equals("grpc") && !protocol.equals("http/protobuf")) {
                throw new IllegalArgumentException(protocol + ": Unsupported protocol. Expecting grpc or http/protobuf");
            }
            this.protocol = protocol;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Add a request header. Call multiple times to add multiple headers.
         */
        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder intervalSeconds(int intervalSeconds) {
            if (intervalSeconds <= 0) {
                throw new IllegalArgumentException(intervalSeconds + ": Expecting intervalSeconds > 0");
            }
            this.intervalSeconds = intervalSeconds;
            return this;
        }

        public Builder timeoutSeconds(int timeoutSeconds) {
            if (timeoutSeconds <= 0) {
                throw new IllegalArgumentException(timeoutSeconds + ": Expecting timeoutSeconds > 0");
            }
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder serviceNamespace(String serviceNamespace) {
            this.serviceNamespace = serviceNamespace;
            return this;
        }

        public Builder serviceInstanceId(String serviceInstanceId) {
            this.serviceInstanceId = serviceInstanceId;
            return this;
        }

        public Builder serviceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
            return this;
        }

        public Builder resourceAttribute(String name, String value) {
            this.resourceAttributes.put(name, value);
            return this;
        }

        public ExporterOpenTelemetryProperties build() {
            return new ExporterOpenTelemetryProperties(protocol, endpoint, headers, intervalSeconds, timeoutSeconds, serviceName, serviceNamespace, serviceInstanceId, serviceVersion, resourceAttributes);
        }
    }
}
