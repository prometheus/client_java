package io.prometheus.client.exemplars.tracer.common;

public class NoopSpanContext implements SpanContext {

  @Override
  public String getTraceId() {
    return null;
  }

  @Override
  public String getSpanId() {
    return null;
  }
}
