package io.prometheus.client.exemplars.api;

/**
 * Immutable data class holding an Exemplar.
 */
public class Exemplar {

  private final String traceId;
  private final String spanId;
  private final double value;
  private final long timestampMs;

  public Exemplar(String traceId, String spanId, double value, long timestampMs) {
    if (traceId == null) {
      throw new NullPointerException("traceId");
    }
    if (spanId == null) {
      throw new NullPointerException("spanId");
    }
    this.traceId = traceId;
    this.spanId = spanId;
    this.value = value;
    this.timestampMs = timestampMs;
  }

  public Exemplar(String traceId, String spanId, double value) {
    this(traceId, spanId, value, System.currentTimeMillis());
  }

  public String getTraceId() {
    return traceId;
  }

  public String getSpanId() {
    return spanId;
  }

  public double getValue() {
    return value;
  }

  public long getTimestampMs() {
    return timestampMs;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Exemplar)) {
      return false;
    }
    Exemplar other = (Exemplar) obj;
    return traceId.equals(other.traceId) &&
        spanId.equals(other.spanId) &&
        Double.compare(other.value, value) == 0 &&
        timestampMs == other.timestampMs;
  }

  @Override
  public int hashCode() {
    int hash = traceId.hashCode();
    hash = 37 * hash + spanId.hashCode();
    long d = Double.doubleToLongBits(value);
    hash = 37 * hash + (int) (d ^ (d >>> 32));
    hash = 37 * hash + (int) timestampMs;
    return hash;
  }
}
