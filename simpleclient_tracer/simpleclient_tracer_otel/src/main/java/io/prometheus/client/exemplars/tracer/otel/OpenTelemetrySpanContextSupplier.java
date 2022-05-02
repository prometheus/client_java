package io.prometheus.client.exemplars.tracer.otel;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanId;
import io.opentelemetry.api.trace.TraceId;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;

public class OpenTelemetrySpanContextSupplier implements SpanContextSupplier {

	public static boolean isAvailable() {
    try {
      if ("inactive".equalsIgnoreCase(System.getProperties().getProperty("io.prometheus.otelExemplars"))) {
        return false;
      }
      OpenTelemetrySpanContextSupplier test = new OpenTelemetrySpanContextSupplier();
      test.getSpanId();
      test.getTraceId();
      test.isSampled();
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
	public String getTraceId() {
		String traceId = Span.current().getSpanContext().getTraceId();
		return TraceId.isValid(traceId) ? traceId : null;
	}

	@Override
	public String getSpanId() {
		String spanId = Span.current().getSpanContext().getSpanId();
		return SpanId.isValid(spanId) ? spanId : null;
	}

	@Override
	public boolean isSampled() {
		return Span.current().getSpanContext().isSampled();
	}
}
