package io.prometheus.metrics.exporter.opentelemetry;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
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
        createAutoConfiguredOpenTelemetrySdk(builder, config, readerRef, instrumentationScopeInfo);

    MetricReader reader = readerRef.get();
    reader.register(
        new PrometheusMetricProducer(registry, instrumentationScopeInfo, getResourceField(sdk)));
    return reader;
  }

  static AutoConfiguredOpenTelemetrySdk createAutoConfiguredOpenTelemetrySdk(
      OpenTelemetryExporter.Builder builder,
      PrometheusProperties config,
      AtomicReference<MetricReader> readerRef,
      InstrumentationScopeInfo instrumentationScopeInfo) {
    PropertyMapper propertyMapper =
        PropertyMapper.create(config.getExporterOpenTelemetryProperties(), builder);

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
            (resource, unused) -> getResource(builder, config, resource, instrumentationScopeInfo))
        .build();
  }

  private static Resource getResource(
      OpenTelemetryExporter.Builder builder,
      PrometheusProperties config,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo) {
    ExporterOpenTelemetryProperties properties = config.getExporterOpenTelemetryProperties();
    return resource
        .merge(
            PropertiesResourceProvider.mergeResource(
                builder.resourceAttributes,
                builder.serviceName,
                builder.serviceNamespace,
                builder.serviceInstanceId,
                builder.serviceVersion))
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
