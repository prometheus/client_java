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
}