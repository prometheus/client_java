package io.prometheus.client.exemplars.tracer.otel_agent;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.prometheus.client.exemplars.tracer.common.SpanContext;

/**
 * This is exactly the same as the OpenTelemetrySpanContext. However, the {@code io.opentelemetry.api} package
 * is relocated to {@code io.opentelemetry.javaagent.shaded.io.opentelemetry.api} in the OpenTelemetry agent.
 */
public class OpenTelemetryAgentSpanContext implements SpanContext {

  public static boolean isAvailable() {
    try {
      OpenTelemetryAgentSpanContext testContext = new OpenTelemetryAgentSpanContext();
      testContext.getSpanId();
      testContext.getSpanId();
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
