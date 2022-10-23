package io.prometheus.metrics.exemplars;

import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel.OpenTelemetrySpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel_agent.OpenTelemetryAgentSpanContextSupplier;

class Tracer {

  ExemplarSampler initExemplarSampler() {
    try {
      Object spanContextSupplier = findSpanContextSupplier();
      if (spanContextSupplier != null) {
        return new DefaultExemplarSampler((SpanContextSupplier) spanContextSupplier);
      }
    } catch (NoClassDefFoundError ignored) {
      // simpleclient_tracer_common dependency not found
    }
    return null;
  }

  // Avoid SpanContextSupplier in the method signature so that we can handle the NoClassDefFoundError
  // even if the user excluded simpleclient_tracer_common from the classpath.
  private Object findSpanContextSupplier() {
    try {
      if (OpenTelemetrySpanContextSupplier.isAvailable()) {
        return new OpenTelemetrySpanContextSupplier();
      }
    } catch (NoClassDefFoundError ignored) {
      // tracer_otel dependency not found
    } catch (UnsupportedClassVersionError ignored) {
      // OpenTelemetry requires Java 8, but client_java might run on Java 6.
    }
    try {
      if (OpenTelemetryAgentSpanContextSupplier.isAvailable()) {
        return new OpenTelemetryAgentSpanContextSupplier();
      }
    } catch (NoClassDefFoundError ignored) {
      // tracer_otel_agent dependency not found
    } catch (UnsupportedClassVersionError ignored) {
      // OpenTelemetry requires Java 8, but client_java might run on Java 6.
    }
    return null;
  }
}
