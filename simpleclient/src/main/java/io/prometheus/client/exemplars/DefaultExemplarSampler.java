package io.prometheus.client.exemplars;

import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;

/**
 * Default Exemplar sampler.
 * <p>
 * Keeps each Exemplar for a minimum of ~7 seconds, then samples a new one.
 */
public class DefaultExemplarSampler implements ExemplarSampler {

  private static final String SPAN_ID = "span_id";
  private static final String TRACE_ID = "trace_id";

  private final SpanContextSupplier spanContextSupplier;
  // Choosing a prime number for the retention interval makes behavior more predictable,
  // because it is unlikely that retention happens at the exact same time as a Prometheus scrape.
  private final long minRetentionIntervalMs = 7109;
  private final Clock clock;

  public DefaultExemplarSampler(SpanContextSupplier spanContextSupplier) {
    this.spanContextSupplier = spanContextSupplier;
    this.clock = new SystemClock();
  }

  // for unit tests only
  DefaultExemplarSampler(SpanContextSupplier spanContextSupplier, Clock clock) {
    this.spanContextSupplier = spanContextSupplier;
    this.clock = clock;
  }

  @Override
  public Exemplar sample(double increment, Exemplar previous) {
    return doSample(increment, previous);
  }

  @Override
  public Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous) {
    return doSample(value, previous);
  }

  private Exemplar doSample(double value, Exemplar previous) {
    long timestampMs = clock.currentTimeMillis();
    if (previous == null || previous.getTimestampMs() == null
        || timestampMs - previous.getTimestampMs() > minRetentionIntervalMs) {
      String spanId = spanContextSupplier.getSpanId();
      String traceId = spanContextSupplier.getTraceId();
      if (traceId != null && spanId != null) {
        return new Exemplar(value, timestampMs, SPAN_ID, spanId, TRACE_ID, traceId);
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