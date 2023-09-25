package io.prometheus.client.exemplars.tracer.common;

public interface SpanContextSupplier {

  /**
   * @return the current trace id, or {@code null} if this call is not happening within a span context.
   */
  String getTraceId();

  /**
   * @return the current span id, or {@code null} if this call is not happening within a span context.
   */
  String getSpanId();
  
  /**
   * @return the state of the current Span. If this value is false a component before in the chain take the decision to not record it. Subsequent calling service have
   * to respect this value in order not to have partial TraceID with only some Span in it. This value is important to be sure to choose a recorded Trace in Examplar
   * sampling process
   */
  boolean isSampled();
}