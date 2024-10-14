package io.prometheus.metrics.exporter.opentelemetry;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.ResourceConfiguration;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.metrics.config.ExporterOpenTelemetryProperties;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

public class OtelAutoConfig {
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

    MetricReader reader = readerRef.get();
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
        .merge(ResourceAttributesFromOtelAgent.get(instrumentationScopeInfo.getName()));
  }

  private static Resource getResourceField(AutoConfiguredOpenTelemetrySdk sdk) {
    try {
      Method method = AutoConfiguredOpenTelemetrySdk.class.getDeclaredMethod("getResource");
      method.setAccessible(true);
      return (Resource) method.invoke(sdk);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
