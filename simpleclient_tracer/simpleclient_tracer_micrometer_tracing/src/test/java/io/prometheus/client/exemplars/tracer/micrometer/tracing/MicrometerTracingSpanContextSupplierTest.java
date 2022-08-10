package io.prometheus.client.exemplars.tracer.micrometer.tracing;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MicrometerTracingSpanContextSupplierTest {

	@Mock private Tracer tracer;
	@Mock private Span span;
	@Mock private TraceContext traceContext;
	@InjectMocks private MicrometerTracingSpanContextSupplier spanContextSupplier;

	@Test
	public void nullSpanShouldBeReportedAsNotSampled() {
		when(tracer.currentSpan()).thenReturn(null);
		assertThat(spanContextSupplier.isSampled()).isFalse();
	}

	@Test
	public void notSampledSpanShouldBeReportedAsNotSampled() {
		when(tracer.currentSpan()).thenReturn(span);
		when(span.context()).thenReturn(traceContext);
		when(traceContext.sampled()).thenReturn(false);

		assertThat(spanContextSupplier.isSampled()).isFalse();
	}

	@Test
	public void sampledSpanShouldBeReportedAsSampled() {
		when(tracer.currentSpan()).thenReturn(span);
		when(span.context()).thenReturn(traceContext);
		when(traceContext.sampled()).thenReturn(true);

		assertThat(spanContextSupplier.isSampled()).isTrue();
	}

	@Test
	public void traceIdAndSpanIdShouldBeFetchedFromTheSpan() {
		String traceId = "9f013d8df008f901";
		String spanId = "04f1747a5f4cde87";

		when(tracer.currentSpan()).thenReturn(span);
		when(span.context()).thenReturn(traceContext);
		when(traceContext.traceId()).thenReturn(traceId);
		when(traceContext.spanId()).thenReturn(spanId);

		assertThat(spanContextSupplier.getTraceId()).isSameAs(traceId);
		assertThat(spanContextSupplier.getSpanId()).isSameAs(spanId);
	}
}
