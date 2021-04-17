package io.prometheus.client.exemplars.api;

import java.util.Arrays;

/**
 * Immutable data class holding an Exemplar.
 */
public class Exemplar {

  public static final String TRACE_ID = "trace_id";
  public static final String SPAN_ID = "span_id";

  private final String[] labels;
  private final double value;
  private final long timestampMs;

  /**
   * Create an Exemplar without a timestamp
   *
   * @param value  the observed value
   * @param labels name/value pairs. Expecting an even number of strings. The combined length of the label names and
   *               values must not exceed 128 UTF-8 characters. Neither a label name nor a label value may be null.
   */
  public Exemplar(double value, String... labels) {
    this(value, 0, labels);
  }

  /**
   * Create an Exemplar
   *
   * @param value       the observed value
   * @param timestampMs as in {@link System#currentTimeMillis()}
   * @param labels      name/value pairs. Expecting an even number of strings. The combined length of the
   *                    label names and values must not exceed 128 UTF-8 characters. Neither a label name
   *                    nor a label value may be null.
   */
  public Exemplar(double value, long timestampMs, String... labels) {
    validateLabels(labels);
    this.labels = Arrays.copyOf(labels, labels.length);
    this.value = value;
    this.timestampMs = timestampMs;
  }

  public int getNumberOfLabels() {
    return labels.length / 2;
  }

  public String getLabelName(int i) {
    return labels[2 * i];
  }

  public String getLabelValue(int i) {
    return labels[2 * i + 1];
  }

  public double getValue() {
    return value;
  }

  /**
   * @return 0 means no timestamp.
   */
  public long getTimestampMs() {
    return timestampMs;
  }

  private void validateLabels(String... labels) {
    if (labels.length % 2 != 0) {
      throw new IllegalArgumentException("labels are name/value pairs, expecting an even number");
    }
    int charsTotal = 0;
    for (int i = 0; i < labels.length; i++) {
      if (labels[i] == null) {
        throw new IllegalArgumentException("labels[" + i + "] is null");
      }
      charsTotal += labels[i].length();
    }
    if (charsTotal > 128) {
      throw new IllegalArgumentException(
          "the combined length of the label names and values must not exceed 128 UTF-8 characters");
    }
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
    return Arrays.equals(this.labels, other.labels) &&
        Double.compare(other.value, value) == 0 &&
        timestampMs == other.timestampMs;
  }

  @Override
  public int hashCode() {
    int hash = Arrays.hashCode(labels);
    long d = Double.doubleToLongBits(value);
    hash = 37 * hash + (int) (d ^ (d >>> 32));
    hash = 37 * hash + (int) timestampMs;
    return hash;
  }
}
