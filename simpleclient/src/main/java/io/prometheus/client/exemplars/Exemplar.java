package io.prometheus.client.exemplars;

import java.util.Arrays;
import java.util.Map;

/**
 * Immutable data class holding an Exemplar.
 */
public class Exemplar {

  public static final String TRACE_ID = "trace_id";
  public static final String SPAN_ID = "span_id";

  private final String[] labels;
  private final double value;
  private final Long timestampMs;

  /**
   * Create an Exemplar without a timestamp
   *
   * @param value  the observed value
   * @param labels name/value pairs. Expecting an even number of strings. The combined length of the label names and
   *               values must not exceed 128 UTF-8 characters. Neither a label name nor a label value may be null.
   */
  public Exemplar(double value, String... labels) {
    this(value, null, labels);
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
  public Exemplar(double value, Long timestampMs, String... labels) {
    validateLabels(labels);
    this.labels = Arrays.copyOf(labels, labels.length);
    this.value = value;
    this.timestampMs = timestampMs;
  }

  /**
   * Create an Exemplar
   *
   * @param value  the observed value
   * @param labels the labels. Must not be null. The combined length of the label names and values must not exceed
   *               128 UTF-8 characters. Neither a label name nor a label value may be null.
   */
  public Exemplar(double value, Map<String, String> labels) {
    this(value, null, mapToArray(labels));
  }

  /**
   * Create an Exemplar
   *
   * @param value       the observed value
   * @param timestampMs as in {@link System#currentTimeMillis()}
   * @param labels      the labels. Must not be null. The combined length of the label names and values must not exceed
   *                    128 UTF-8 characters. Neither a label name nor a label value may be null.
   */
  public Exemplar(double value, Long timestampMs, Map<String, String> labels) {
    this(value, timestampMs, mapToArray(labels));
  }

  public int getNumberOfLabels() {
    return labels.length / 2;
  }

  /**
   * Get the label name at index {@code i}.
   * @param i the index, must be >= 0 and < {@link #getNumberOfLabels()}.
   * @return the label name at index {@code i}
   */
  public String getLabelName(int i) {
    return labels[2 * i];
  }

  /**
   * Get the label value at index {@code i}.
   * @param i the index, must be >= 0 and < {@link #getNumberOfLabels()}.
   * @return the label value at index {@code i}
   */
  public String getLabelValue(int i) {
    return labels[2 * i + 1];
  }

  public double getValue() {
    return value;
  }

  /**
   * @return Unix timestamp or {@code null} if no timestamp is present.
   */
  public Long getTimestampMs() {
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
      if (i % 2 == 0) { // label names should be unique
        int j=i+2;
        while (j<labels.length) {
          if (labels[i].equals(labels[j])) {
            throw new IllegalArgumentException(labels[i] + ": label name is not unique");
          }
          j+=2;
        }
      }
      charsTotal += labels[i].length();
    }
    if (charsTotal > 128) {
      throw new IllegalArgumentException(
          "the combined length of the label names and values must not exceed 128 UTF-8 characters");
    }
  }

  private static String[] mapToArray(Map<String, String> labelMap) {
    String[] result = new String[2 * labelMap.size()];
    int i = 0;
    for (Map.Entry<String, String> entry : labelMap.entrySet()) {
      result[i] = entry.getKey();
      result[i + 1] = entry.getValue();
      i += 2;
    }
    return result;
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
        (timestampMs == null && other.timestampMs == null
            || timestampMs != null && timestampMs.equals(other.timestampMs));
  }

  @Override
  public int hashCode() {
    int hash = Arrays.hashCode(labels);
    long d = Double.doubleToLongBits(value);
    hash = 37 * hash + (int) (d ^ (d >>> 32));
    if (timestampMs != null) {
      hash = 37 * hash + timestampMs.intValue();
    }
    return hash;
  }
}
