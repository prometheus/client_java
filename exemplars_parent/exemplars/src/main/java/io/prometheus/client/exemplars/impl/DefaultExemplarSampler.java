package io.prometheus.client.exemplars.impl;

import io.prometheus.client.exemplars.api.CounterExemplarSampler;
import io.prometheus.client.exemplars.api.Exemplar;
import io.prometheus.client.exemplars.api.GaugeExemplarSampler;
import io.prometheus.client.exemplars.api.HistogramExemplarSampler;
import io.prometheus.client.exemplars.api.SummaryExemplarSampler;
import io.prometheus.client.exemplars.api.Value;
import io.prometheus.client.exemplars.tracer.common.SpanContext;

/**
 * Default Exemplar sampler.
 * <p/>
 * Keeps each Exemplar for a minimum of ~7 seconds, then samples a new one.
 */
public class DefaultExemplarSampler
    implements CounterExemplarSampler, GaugeExemplarSampler, HistogramExemplarSampler, SummaryExemplarSampler {

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
  public Exemplar sample(Value value, Exemplar previous) {
    long timestampMs = clock.currentTimeMillis();
    if (previous == null || timestampMs - previous.getTimestampMs() > minRetentionIntervalMs) {
      String traceId = spanContext.getTraceId();
      String spanId = spanContext.getSpanId();
      if (traceId != null && spanId != null) {
        return new Exemplar(traceId, spanId, value.get(), timestampMs);
      }
    }
    return null;
  }

  @Override
  public Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous) {
    // As this implementation ignores the buckets, it's almost identical to the Counter and Gauge version,
    // except that the value is passed directly and not via the Value wrapper.
    long timestampMs = clock.currentTimeMillis();
    if (previous == null || timestampMs - previous.getTimestampMs() > minRetentionIntervalMs) {
      String traceId = spanContext.getTraceId();
      String spanId = spanContext.getSpanId();
      if (traceId != null && spanId != null) {
        return new Exemplar(traceId, spanId, value, timestampMs);
      }
    }
    return null;
  }

  @Override
  public Exemplar sample(double value) {
    String traceId = spanContext.getTraceId();
    String spanId = spanContext.getSpanId();
    if (traceId != null && spanId != null) {
      return new Exemplar(traceId, spanId, value, clock.currentTimeMillis());
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