package io.prometheus.metrics.exporter.opentelemetry;

import io.prometheus.metrics.config.ExporterOpenTelemetryProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.exporter.otlp.http.metrics.OtlpHttpMetricExporter;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.exporter.otlp.http.metrics.OtlpHttpMetricExporterBuilder;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.common.InstrumentationScopeInfo;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.export.MetricExporter;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.metrics.export.PeriodicMetricReader;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.resources.Resource;
import io.prometheus.metrics.shaded.io_opentelemetry_1_38_0.sdk.resources.ResourceBuilder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OpenTelemetryExporter implements AutoCloseable {
    private final PeriodicMetricReader reader;

    private OpenTelemetryExporter(Builder builder, PrometheusProperties config, PrometheusRegistry registry) {
        InstrumentationScopeInfo instrumentationScopeInfo = PrometheusInstrumentationScope.loadInstrumentationScopeInfo();
        ExporterOpenTelemetryProperties properties = config.getExporterOpenTelemetryProperties();
        Resource resource = initResourceAttributes(builder, properties, instrumentationScopeInfo);
        MetricExporter exporter;
        if (ConfigHelper.getProtocol(builder, properties).equals("grpc")) {
            OtlpGrpcMetricExporterBuilder exporterBuilder = OtlpGrpcMetricExporter.builder()
                    .setTimeout(Duration.ofSeconds(ConfigHelper.getTimeoutSeconds(builder, properties)))
                    .setEndpoint(ConfigHelper.getEndpoint(builder, properties));
            for (Map.Entry<String, String> header : ConfigHelper.getHeaders(builder, properties).entrySet()) {
                exporterBuilder.addHeader(header.getKey(), header.getValue());
            }
            exporter = exporterBuilder.build();
        } else {
            OtlpHttpMetricExporterBuilder exporterBuilder = OtlpHttpMetricExporter.builder()
                    .setTimeout(Duration.ofSeconds(ConfigHelper.getTimeoutSeconds(builder, properties)))
                    .setEndpoint(ConfigHelper.getEndpoint(builder, properties));
            for (Map.Entry<String, String> header : ConfigHelper.getHeaders(builder, properties).entrySet()) {
                exporterBuilder.addHeader(header.getKey(), header.getValue());
            }
            exporter = exporterBuilder.build();
        }
        reader = PeriodicMetricReader.builder(exporter)
                .setInterval(Duration.ofSeconds(ConfigHelper.getIntervalSeconds(builder, properties)))
                .build();

        PrometheusMetricProducer prometheusMetricProducer = new PrometheusMetricProducer(registry, instrumentationScopeInfo, resource);
        reader.register(prometheusMetricProducer);
    }

    public void close() {
        reader.shutdown();
    }

    private Resource initResourceAttributes(Builder builder, ExporterOpenTelemetryProperties properties, InstrumentationScopeInfo instrumentationScopeInfo) {
        String serviceName = ConfigHelper.getServiceName(builder, properties);
        String serviceNamespace = ConfigHelper.getServiceNamespace(builder, properties);
        String serviceInstanceId = ConfigHelper.getServiceInstanceId(builder, properties);
        String serviceVersion = ConfigHelper.getServiceVersion(builder, properties);
        Map<String, String> resourceAttributes = ResourceAttributes.get(instrumentationScopeInfo.getName(), serviceName, serviceNamespace, serviceInstanceId, serviceVersion, ConfigHelper.getResourceAttributes(builder, properties));
        ResourceBuilder resourceBuilder = Resource.builder();
        for (Map.Entry<String, String> entry : resourceAttributes.entrySet()) {
            resourceBuilder.put(entry.getKey(), entry.getValue());
        }
        return resourceBuilder.build();
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder {

        private final PrometheusProperties config;
        private PrometheusRegistry registry = null;
        private String protocol;
        private String endpoint;
        private final Map<String, String> headers = new HashMap<>();
        private Integer intervalSeconds;
        private Integer timeoutSeconds;
        private String serviceName;
        private String serviceNamespace;
        private String serviceInstanceId;
        private String serviceVersion;
        private final Map<String, String> resourceAttributes = new HashMap<>();

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        public Builder registry(PrometheusRegistry registry) {
            this.registry = registry;
            return this;
        }

        /**
         * Specifies the OTLP transport protocol to be used when exporting metrics.
         * <p>
         * Supported values are {@code "grpc"} and {@code "http/protobuf"}. Default is {@code "grpc"}.
         * <p>
         * See OpenTelemetry's <a href="https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/#otel_exporter_otlp_protocol">OTEL_EXPORTER_OTLP_PROTOCOL</a>.
         */
        public Builder protocol(String protocol) {
            if (!protocol.equals("grpc") && !protocol.equals("http/protobuf")) {
                throw new IllegalArgumentException(protocol + ": Unsupported protocol. Expecting grpc or http/protobuf");
            }
            this.protocol = protocol;
            return this;
        }

        /**
         * The OTLP endpoint to send metric data to.
         * <p>
         * The default depends on the protocol:
         * <ul>
         * <li>{@code "grpc"}: {@code "http://localhost:4317"}</li>
         * <li>{@code "http/protobuf"}: {@code "http://localhost:4318/v1/metrics"}</li>
         * </ul>
         * If the protocol is {@code "http/protobuf"} and the endpoint does not have the {@code "/v1/metrics"} suffix,
         * the {@code "/v1/metrics"} suffix will automatically be appended.
         * <p>
         * See OpenTelemetry's <a href="https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/#otel_exporter_otlp_metrics_endpoint">OTEL_EXPORTER_OTLP_METRICS_ENDPOINT</a>.
         */
        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Add an HTTP header to be applied to outgoing requests.
         * Call multiple times to add multiple headers.
         * <p>
         * See OpenTelemetry's <a href="https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/#otel_exporter_otlp_headers">OTEL_EXPORTER_OTLP_HEADERS</a>.
         */
        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        /**
         * The interval between the start of two export attempts. Default is 60000.
         * <p>
         * Like OpenTelemetry's <a href="https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#periodic-metric-reader">OTEL_METRIC_EXPORT_INTERVAL</a>,
         * but in seconds rather than milliseconds.
         */
        public Builder intervalSeconds(int intervalSeconds) {
            if (intervalSeconds <= 0) {
                throw new IllegalStateException(intervalSeconds + ": expecting a push interval > 0s");
            }
            this.intervalSeconds = intervalSeconds;
            return this;
        }

        /**
         * The timeout for outgoing requests. Default is 10.
         * <p>
         * Like OpenTelemetry's <a href="https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/#otel_exporter_otlp_metrics_timeout">OTEL_EXPORTER_OTLP_METRICS_TIMEOUT</a>,
         * but in seconds rather than milliseconds.
         */
        public Builder timeoutSeconds(int timeoutSeconds) {
            if (timeoutSeconds <= 0) {
                throw new IllegalStateException(timeoutSeconds + ": expecting a push interval > 0s");
            }
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        /**
         * The {@code service.name} resource attribute.
         * <p>
         * If not explicitly specified, {@code client_java} will try to initialize it with a reasonable default, like the JAR file name.
         * <p>
         * See {@code service.name} in OpenTelemetry's <a href="https://opentelemetry.io/docs/specs/otel/resource/semantic_conventions/#service">Resource Semantic Conventions</a>.
         */
        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        /**
         * The {@code service.namespace} resource attribute.
         * <p>
         * See {@code service.namespace} in OpenTelemetry's <a href="https://opentelemetry.io/docs/specs/otel/resource/semantic_conventions/#service-experimental">Resource Semantic Conventions</a>.
         */
        public Builder serviceNamespace(String serviceNamespace) {
            this.serviceNamespace = serviceNamespace;
            return this;
        }

        /**
         * The {@code service.instance.id} resource attribute.
         * <p>
         * See {@code service.instance.id} in OpenTelemetry's <a href="https://opentelemetry.io/docs/specs/otel/resource/semantic_conventions/#service-experimental">Resource Semantic Conventions</a>.
         */
        public Builder serviceInstanceId(String serviceInstanceId) {
            this.serviceInstanceId = serviceInstanceId;
            return this;
        }

        /**
         * The {@code service.version} resource attribute.
         * <p>
         * See {@code service.version} in OpenTelemetry's <a href="https://opentelemetry.io/docs/specs/otel/resource/semantic_conventions/#service-experimental">Resource Semantic Conventions</a>.
         */
        public Builder serviceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
            return this;
        }

        /**
         * Add a resource attribute. Call multiple times to add multiple resource attributes.
         * <p>
         * See OpenTelemetry's <a href="https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#general-sdk-configuration">OTEL_RESOURCE_ATTRIBUTES</a>.
         */
        public Builder resourceAttribute(String name, String value) {
            this.resourceAttributes.put(name, value);
            return this;
        }

        public OpenTelemetryExporter buildAndStart() {
            if (registry == null) {
                registry = PrometheusRegistry.defaultRegistry;
            }
            return new OpenTelemetryExporter(this, config, registry);
        }
    }

    private static class ConfigHelper {

        private static String getProtocol(OpenTelemetryExporter.Builder builder, ExporterOpenTelemetryProperties config) {
            String protocol = config.getProtocol();
            if (protocol != null) {
                return protocol;
            }
            protocol = getString("otel.exporter.otlp.protocol");
            if (protocol != null) {
                if (!protocol.equals("grpc") && !protocol.equals("http/protobuf")) {
                    throw new IllegalStateException(protocol + ": Unsupported OpenTelemetry exporter protocol. Expecting grpc or http/protobuf.");
                }
                return protocol;
            }
            if (builder.protocol != null) {
                return builder.protocol;
            }
            return "grpc";
        }

        private static String getEndpoint(OpenTelemetryExporter.Builder builder, ExporterOpenTelemetryProperties config) {
            String endpoint = config.getEndpoint();
            if (endpoint == null) {
                endpoint = getString("otel.exporter.otlp.metrics.endpoint");
            }
            if (endpoint == null) {
                endpoint = getString("otel.exporter.otlp.endpoint");
            }
            if (endpoint == null) {
                endpoint = builder.endpoint;
            }
            if (endpoint == null) {
                if (getProtocol(builder, config).equals("grpc")) {
                    endpoint = "http://localhost:4317";
                } else { // http/protobuf
                    endpoint = "http://localhost:4318/v1/metrics";
                }
            }
            if (getProtocol(builder, config).equals("grpc")) {
                return endpoint;
            } else { // http/protobuf
                if (!endpoint.endsWith("v1/metrics")) {
                    if (!endpoint.endsWith("/")) {
                        return endpoint + "/v1/metrics";
                    } else {
                        return endpoint + "v1/metrics";
                    }
                } else {
                    return endpoint;
                }
            }
        }

        private static Map<String, String> getHeaders(OpenTelemetryExporter.Builder builder, ExporterOpenTelemetryProperties config) {
            Map<String, String> headers = config.getHeaders();
            if (!headers.isEmpty()) {
                return headers;
            }
            headers = getMap("otel.exporter.otlp.headers");
            if (!headers.isEmpty()) {
                return headers;
            }
            if (!builder.headers.isEmpty()) {
                return builder.headers;
            }
            return new HashMap<>();
        }

        private static int getIntervalSeconds(OpenTelemetryExporter.Builder builder, ExporterOpenTelemetryProperties config) {
            Integer intervalSeconds = config.getIntervalSeconds();
            if (intervalSeconds != null) {
                return intervalSeconds;
            }
            intervalSeconds = getPositiveInteger("otel.metric.export.interval");
            if (intervalSeconds != null) {
                return (int) TimeUnit.MILLISECONDS.toSeconds(intervalSeconds);
            }
            if (builder.intervalSeconds != null) {
                return builder.intervalSeconds;
            }
            return 60;
        }

        private static int getTimeoutSeconds(OpenTelemetryExporter.Builder builder, ExporterOpenTelemetryProperties config) {
            Integer timeoutSeconds = config.getTimeoutSeconds();
            if (timeoutSeconds != null) {
                return timeoutSeconds;
            }
            Integer timeoutMilliseconds = getPositiveInteger("otel.exporter.otlp.metrics.timeout");
            if (timeoutMilliseconds == null) {
                timeoutMilliseconds = getPositiveInteger("otel.exporter.otlp.timeout");
            }
            if (timeoutMilliseconds != null) {
                return (int) TimeUnit.MILLISECONDS.toSeconds(timeoutMilliseconds);
            }
            if (builder.timeoutSeconds != null) {
                return builder.timeoutSeconds;
            }
            return 10;
        }

        private static String getServiceName(OpenTelemetryExporter.Builder builder, ExporterOpenTelemetryProperties config) {
            String serviceName = config.getServiceName();
            if (serviceName != null) {
                return serviceName;
            }
            serviceName = getString("otel.service.name");
            if (serviceName != null) {
                return serviceName;
            }
            if (builder.serviceName != null) {
                return builder.serviceName;
            }
            return null;
        }

        private static String getServiceNamespace(OpenTelemetryExporter.Builder builder, ExporterOpenTelemetryProperties config) {
            String serviceNamespace = config.getServiceNamespace();
            if (serviceNamespace != null) {
                return serviceNamespace;
            }
            if (builder.serviceNamespace != null) {
                return builder.serviceNamespace;
            }
            return null;
        }

        private static String getServiceInstanceId(OpenTelemetryExporter.Builder builder, ExporterOpenTelemetryProperties config) {
            String serviceInstanceId = config.getServiceInstanceId();
            if (serviceInstanceId != null) {
                return serviceInstanceId;
            }
            if (builder.serviceInstanceId != null) {
                return builder.serviceInstanceId;
            }
            return null;
        }

        private static String getServiceVersion(OpenTelemetryExporter.Builder builder, ExporterOpenTelemetryProperties config) {
            String serviceVersion = config.getServiceVersion();
            if (serviceVersion != null) {
                return serviceVersion;
            }
            if (builder.serviceVersion != null) {
                return builder.serviceVersion;
            }
            return null;
        }

        private static Map<String, String> getResourceAttributes(OpenTelemetryExporter.Builder builder, ExporterOpenTelemetryProperties config) {
            Map<String, String> resourceAttributes = config.getResourceAttributes();
            if (!resourceAttributes.isEmpty()) {
                return resourceAttributes;
            }
            resourceAttributes = getMap("otel.resource.attributes");
            if (!resourceAttributes.isEmpty()) {
                return resourceAttributes;
            }
            if (!builder.resourceAttributes.isEmpty()) {
                return builder.resourceAttributes;
            }
            return new HashMap<>();
        }

        private static String getString(String otelPropertyName) {
            String otelEnvVarName = otelPropertyName.replace(".", "_").replace("-", "_").toUpperCase();
            if (System.getenv(otelEnvVarName) != null) {
                return System.getenv(otelEnvVarName);
            }
            if (System.getProperty(otelPropertyName) != null) {
                return System.getProperty(otelPropertyName);
            }
            return null;
        }

        private static Integer getInteger(String otelPropertyName) {
            String result = getString(otelPropertyName);
            if (result == null) {
                return null;
            } else {
                try {
                    return Integer.parseInt(result);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException(otelPropertyName + "=" + result + " - illegal value.");
                }
            }
        }

        private static Integer getPositiveInteger(String otelPropertyName) {
            Integer result = getInteger(otelPropertyName);
            if (result == null) {
                return null;
            }
            if (result <= 0) {
                throw new IllegalStateException(otelPropertyName + "=" + result + ": Expecting value > 0.");
            }
            return result;
        }

        private static Map<String, String> getMap(String otelPropertyName) {
            Map<String, String> result = new HashMap<>();
            String property = getString(otelPropertyName);
            if (property != null) {
                String[] pairs = property.split(",");
                for (String pair : pairs) {
                    if (pair.contains("=")) {
                        String[] keyValue = pair.split("=", 1);
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim();
                            String value = keyValue[1].trim();
                            if (key.length() > 0 && value.length() > 0) {
                                result.putIfAbsent(key, value);
                            }
                        }
                    }
                }
            }
            return result;
        }
    }
}
