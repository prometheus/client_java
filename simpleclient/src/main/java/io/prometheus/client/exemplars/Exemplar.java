package io.prometheus.client.exemplars;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Immutable data class holding an Exemplar.
 */
public class Exemplar {

  private final String[] labels;
  private final double value;
  private final Long timestampMs;

  private static final Pattern labelNameRegex = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");

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
    this.labels = sortedCopy(labels);
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
   * @param i the index, must be &gt;= 0 and &lt; {@link #getNumberOfLabels()}.
   * @return the label name at index {@code i}
   */
  public String getLabelName(int i) {
    return labels[2 * i];
  }

  /**
   * Get the label value at index {@code i}.
   * @param i the index, must be &gt;= 0 and &lt; {@link #getNumberOfLabels()}.
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

  private String[] sortedCopy(String... labels) {
    if (labels.length % 2 != 0) {
      throw new IllegalArgumentException("labels are name/value pairs, expecting an even number");
    }
    String[] result = new String[labels.length];
    int charsTotal = 0;
    for (int i = 0; i < labels.length; i+=2) {
      if (labels[i] == null) {
        throw new IllegalArgumentException("labels[" + i + "] is null");
      }
      if (labels[i+1] == null) {
        throw new IllegalArgumentException("labels[" + (i+1) + "] is null");
      }
      if (!labelNameRegex.matcher(labels[i]).matches()) {
        throw new IllegalArgumentException(labels[i] + " is not a valid label name");
      }
      result[i] = labels[i]; // name
      result[i+1] = labels[i+1]; // value
      charsTotal += labels[i].length() + labels[i+1].length();
      // Move the current tuple down while the previous name is greater than current name.
      for (int j=i-2; j>=0; j-=2) {
        int compareResult = result[j+2].compareTo(result[j]);
        if (compareResult == 0) {
          throw new IllegalArgumentException(result[j] + ": label name is not unique");
        } else if (compareResult < 0) {
          String tmp = result[j];
          result[j] = result[j+2];
          result[j+2] = tmp;
          tmp = result[j+1];
          result[j+1] = result[j+3];
          result[j+3] = tmp;
        } else {
          break;
        }
      }
    }
    if (charsTotal > 128) {
      throw new IllegalArgumentException(
          "the combined length of the label names and values must not exceed 128 UTF-8 characters");
    }
    return result;
  }

  /**
   * Convert the map to an array {@code [key1, value1, key2, value2, ...]}.
   */
  public static String[] mapToArray(Map<String, String> labelMap) {
    if (labelMap == null) {
      return null;
    }
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Exemplar{value=");
    sb.append(value);
    if (timestampMs != null) {
      sb.append(", ts=");
      sb.append(timestampMs);
    }
    if (labels.length > 0) {
      sb.append(", labels=<");
      for (int i = 0; i < labels.length; i += 2) {
        sb.append(labels[i]);
        sb.append('=');
        sb.append(labels[i + 1]);
        sb.append(' ');
      }
      // Trim trailing space
      sb.setLength(sb.length() - 1);
      sb.append('>');
    }
    sb.append('}');
    return sb.toString();
  }
}
