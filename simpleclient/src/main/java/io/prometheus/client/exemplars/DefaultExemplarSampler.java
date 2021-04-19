package io.prometheus.client.exemplars;

import io.prometheus.client.exemplars.Exemplar;
import io.prometheus.client.exemplars.ExemplarSampler;
import io.prometheus.client.exemplars.Value;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;

import static io.prometheus.client.exemplars.Exemplar.SPAN_ID;
import static io.prometheus.client.exemplars.Exemplar.TRACE_ID;

/**
 * Default Exemplar sampler.
 * <p/>
 * Keeps each Exemplar for a minimum of ~7 seconds, then samples a new one.
 */
public class DefaultExemplarSampler implements ExemplarSampler {

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
  public Exemplar sample(double increment, Value newTotalValue, Exemplar previous) {
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
      String traceId = spanContextSupplier.getTraceId();
      String spanId = spanContextSupplier.getSpanId();
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