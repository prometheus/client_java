package io.prometheus.metrics.tracer.otel;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.prometheus.metrics.tracer.common.SpanContext;

public class OpenTelemetrySpanContext implements SpanContext {

	public static boolean isAvailable() {
    try {
      OpenTelemetrySpanContext test = new OpenTelemetrySpanContext();
      test.getCurrentSpanId();
      test.getCurrentTraceId();
      test.isCurrentSpanSampled();
      return true;
    } catch (LinkageError ignored) {
      // NoClassDefFoundError:
      //   Either OpenTelemetry is not present, or it is version 0.9.1 or older when io.opentelemetry.api.trace.Span did not exist.
      // IncompatibleClassChangeError:
      //   The application uses an OpenTelemetry version between 0.10.0 and 0.15.0 when SpanContext was a class, and not an interface.
      return false;
    }
  }

	@Override
	public String getCurrentTraceId() {
		String traceId = Span.current().getSpanContext().getTraceId();
		return TraceId.isValid(traceId) ? traceId : null;
	}

	@Override
	public String getCurrentSpanId() {
		String spanId = Span.current().getSpanContext().getSpanId();
		return SpanId.isValid(spanId) ? spanId : null;
	}

	@Override
	public boolean isCurrentSpanSampled() {
		return Span.current().getSpanContext().isSampled();
	}

	@Override
	public void markCurrentSpanAsExemplar() {
		Span.current().setAttribute(EXEMPLAR_ATTRIBUTE_NAME, EXEMPLAR_ATTRIBUTE_VALUE);
	}
}
