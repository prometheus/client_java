package io.prometheus.client.exemplars.tracer.otel;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;

public class OpenTelemetrySpanContextSupplier implements SpanContextSupplier {

  public static boolean isAvailable() {
    try {
      OpenTelemetrySpanContextSupplier test = new OpenTelemetrySpanContextSupplier();
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
