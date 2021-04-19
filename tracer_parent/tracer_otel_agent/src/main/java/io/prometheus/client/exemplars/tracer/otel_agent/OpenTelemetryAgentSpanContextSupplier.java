package io.prometheus.client.exemplars.tracer.otel_agent;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;

/**
 * This is exactly the same as the {@code OpenTelemetrySpanContextSupplier}.
 * However, the {@code io.opentelemetry.api} package is relocated to
 * {@code io.opentelemetry.javaagent.shaded.io.opentelemetry.api} in the OpenTelemetry agent.
 */
public class OpenTelemetryAgentSpanContextSupplier implements SpanContextSupplier {

  public static boolean isAvailable() {
    try {
      OpenTelemetryAgentSpanContextSupplier test = new OpenTelemetryAgentSpanContextSupplier();
      test.getSpanId();
      test.getSpanId();
      return true;
    } catch (NoClassDefFoundError e) {
      return false;
    }
  }

  @Override
  public String getTraceId() {
    String traceId = Span.current().getSpanContext().getTraceId();
    return TraceId.isValid(traceId) ? traceId : null;
  }

  @Override
  public String getSpanId() {
    String spanId = Span.current().getSpanContext().getSpanId();
    return SpanId.isValid(spanId) ? spanId : null;
  }
}
