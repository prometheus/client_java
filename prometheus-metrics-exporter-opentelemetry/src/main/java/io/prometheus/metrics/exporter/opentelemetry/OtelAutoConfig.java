package io.prometheus.metrics.exporter.opentelemetry;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.metrics.config.ExporterOpenTelemetryProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.otelagent.ResourceAttributesFromOtelAgent;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class OtelAutoConfig {

  private static final String SERVICE_INSTANCE_ID = "service.instance.id";

  static MetricReader createReader(
      OpenTelemetryExporter.Builder builder,
      PrometheusProperties config,
      PrometheusRegistry registry) {
    AtomicReference<MetricReader> readerRef = new AtomicReference<>();
    InstrumentationScopeInfo instrumentationScopeInfo =
        PrometheusInstrumentationScope.loadInstrumentationScopeInfo();

    AutoConfiguredOpenTelemetrySdk sdk =
        createAutoConfiguredOpenTelemetrySdk(
            builder,
            readerRef,
            config.getExporterOpenTelemetryProperties(),
            instrumentationScopeInfo);

    MetricReader reader = requireNonNull(readerRef.get());
    reader.register(
        new PrometheusMetricProducer(registry, instrumentationScopeInfo, getResourceField(sdk)));
    return reader;
  }

  static AutoConfiguredOpenTelemetrySdk createAutoConfiguredOpenTelemetrySdk(
      OpenTelemetryExporter.Builder builder,
      AtomicReference<MetricReader> readerRef,
      ExporterOpenTelemetryProperties properties,
      InstrumentationScopeInfo instrumentationScopeInfo) {
    PropertyMapper propertyMapper = PropertyMapper.create(properties, builder);

    return AutoConfiguredOpenTelemetrySdk.builder()
        .addPropertiesSupplier(() -> propertyMapper.configLowPriority)
        .addPropertiesCustomizer(
            c -> PropertyMapper.customizeProperties(propertyMapper.configHighPriority, c))
        .addMetricReaderCustomizer(
            (reader, unused) -> {
              readerRef.set(reader);
              return reader;
            })
        .addResourceCustomizer(
            (resource, c) ->
                getResource(builder, resource, instrumentationScopeInfo, c, properties))
        .build();
  }

  private static Resource getResource(
      OpenTelemetryExporter.Builder builder,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      ConfigProperties configProperties,
      ExporterOpenTelemetryProperties properties) {
    return resource
        .merge(
            PropertiesResourceProvider.mergeResource(
                builder.resourceAttributes,
                builder.serviceName,
                builder.serviceNamespace,
                builder.serviceInstanceId,
                builder.serviceVersion))
        .merge(ResourceConfiguration.createEnvironmentResource(configProperties))
        .merge(
            PropertiesResourceProvider.mergeResource(
                properties.getResourceAttributes(),
                properties.getServiceName(),
                properties.getServiceNamespace(),
                properties.getServiceInstanceId(),
                properties.getServiceVersion()))
        .merge(Resource.create(otelResourceAttributes(instrumentationScopeInfo)));
  }

  /**
   * Only copy the service instance id from the Otel agent resource attributes.
   *
   * <p>All other attributes are calculated from the configuration using OTel SDK AutoConfig.
   */
  private static Attributes otelResourceAttributes(
      InstrumentationScopeInfo instrumentationScopeInfo) {
    AttributesBuilder builder = Attributes.builder();
    Map<String, String> attributes =
        ResourceAttributesFromOtelAgent.getResourceAttributes(instrumentationScopeInfo.getName());
    String id = attributes.get(SERVICE_INSTANCE_ID);
    if (id != null) {
      builder.put(SERVICE_INSTANCE_ID, id);
    }
    return builder.build();
  }

  static Resource getResourceField(AutoConfiguredOpenTelemetrySdk sdk) {
    try {
      Method method = AutoConfiguredOpenTelemetrySdk.class.getDeclaredMethod("getResource");
      method.setAccessible(true);
      return (Resource) method.invoke(sdk);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
