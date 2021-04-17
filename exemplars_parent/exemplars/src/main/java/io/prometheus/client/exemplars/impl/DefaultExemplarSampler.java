package io.prometheus.client.exemplars.impl;

import io.prometheus.client.exemplars.api.Exemplar;
import io.prometheus.client.exemplars.api.ExemplarSampler;
import io.prometheus.client.exemplars.api.Value;
import io.prometheus.client.exemplars.tracer.common.SpanContext;

import static io.prometheus.client.exemplars.api.Exemplar.SPAN_ID;
import static io.prometheus.client.exemplars.api.Exemplar.TRACE_ID;

/**
 * Default Exemplar sampler.
 * <p/>
 * Keeps each Exemplar for a minimum of ~7 seconds, then samples a new one.
 */
public class DefaultExemplarSampler implements ExemplarSampler {

  private final SpanContext spanContext;
  // Choosing a prime number for the retention interval makes behavior more predictable,
  // because it is unlikely that retention happens at the exact same time as a Prometheus scrape.
  private final long minRetentionIntervalMs = 7109;
  private final Clock clock;

  public DefaultExemplarSampler(SpanContext spanContext) {
    this.spanContext = spanContext;
    this.clock = new SystemClock();
  }

  // for unit tests only
  DefaultExemplarSampler(SpanContext spanContext, Clock clock) {
    this.spanContext = spanContext;
    this.clock = clock;
  }

  @Override
  public Exemplar sample(double increment, Value newTotalValue, Exemplar previous) {
    return doSample(increment, previous);
  }

  @Override
  public Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous) {
    return doSample(value, previous);
  }

  private Exemplar doSample(double value, Exemplar previous) {
    long timestampMs = clock.currentTimeMillis();
    if (previous == null || timestampMs - previous.getTimestampMs() > minRetentionIntervalMs) {
      String traceId = spanContext.getTraceId();
      String spanId = spanContext.getSpanId();
      if (traceId != null && spanId != null) {
        return new Exemplar(value, timestampMs, TRACE_ID, traceId, SPAN_ID, spanId);
      }
    }
    return null;
  }

  interface Clock {
    long currentTimeMillis();
  }

  static class SystemClock implements Clock {
    @Override
    public long currentTimeMillis() {
      return System.currentTimeMillis();
    }
  }
}