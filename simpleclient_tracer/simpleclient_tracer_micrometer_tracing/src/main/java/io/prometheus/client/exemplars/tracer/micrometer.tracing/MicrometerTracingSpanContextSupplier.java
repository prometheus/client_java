package io.prometheus.client.exemplars.tracer.micrometer.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;

public class MicrometerTracingSpanContextSupplier implements SpanContextSupplier {
	private final Tracer tracer;

	public MicrometerTracingSpanContextSupplier(Tracer tracer) {
		this.tracer = tracer;
	}

	@Override
	public String getTraceId() {
		// assuming isSampled is called before calling this
		return tracer.currentSpan().context().traceId();
	}

	@Override
	public String getSpanId() {
		// assuming isSampled is called before calling this
		return tracer.currentSpan().context().spanId();
	}

	@Override
	public boolean isSampled() {
		Span span = tracer.currentSpan();
		return span != null ? span.context().sampled() : false;
	}
}
