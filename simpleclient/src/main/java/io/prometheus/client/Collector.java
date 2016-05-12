
package io.prometheus.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A collector for a set of metrics.
 * <p>
 * Normal users should use {@link Gauge}, {@link Counter}, {@link Summary} and {@link Histogram}.
 * <p>
 * Subclasssing Collector is for advanced uses, such as proxying metrics from another monitoring system.
 * It is it the responsibility of subclasses to ensure they produce valid metrics.
 * @see <a href="http://prometheus.io/docs/instrumenting/exposition_formats/">Exposition formats</a>.
 */
public abstract class Collector {
  /**
   * Return all of the metrics of this Collector.
   */
  public abstract List<MetricFamilySamples> collect();
  public enum Type {
    COUNTER,
    GAUGE,
    SUMMARY,
    HISTOGRAM,
  }

  /**
   * A metric, and all of it's samples.
   */
  static public class MetricFamilySamples {
    public final String name;
    public final Type type;
    public final String help;
    public final List<Sample> samples;

    public MetricFamilySamples(String name, Type type, String help, List<Sample> samples) {
      this.name = name;
      this.type = type;
      this.help = help;
      this.samples = samples;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null || !(obj instanceof MetricFamilySamples)) {
        return false;
      }
      MetricFamilySamples other = (MetricFamilySamples) obj;
      
      return other.name.equals(name) && other.type.equals(type)
        && other.help.equals(help) && other.samples.equals(samples) ;
    }

    @Override
    public int hashCode() {
      int hash = 1;
      hash = 37 * hash + name.hashCode();
      hash = 37 * hash + type.hashCode();
      hash = 37 * hash + help.hashCode();
      hash = 37 * hash + samples.hashCode();
      return hash;
    }

    @Override
    public String toString() {
      return "Name: " + name + " Type: " + type + " Help: " + help + 
        " Samples: " + samples;
    }

  /**
   * A single Sample, with a unique name and set of labels.
   */
    public static class Sample {
      public final String name;
      public final Map<String, String> labels;
      public final double value;

      public Sample(String name, double value) {
        this.name = name;
        this.labels = Collections.EMPTY_MAP;
        this.value = value;
      }

      public Sample(String name, Map<String, String> labels, double value) {
        this.name = name;
        this.labels = labels;
        this.value = value;
      }

      @Override
      public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Sample)) {
          return false;
        }
        Sample other = (Sample) obj;
        return other.name.equals(name) && other.labels.equals(labels) && other.value == value;
      }

      @Override
      public int hashCode() {
        int hash = 1;
        hash = 37 * hash + name.hashCode();
        hash = 37 * hash + labels.hashCode();
        long d = Double.doubleToLongBits(value);
        hash = 37 * hash + (int)(d ^ (d >>> 32));
        return hash;
      }

      @Override
      public String toString() {
        return "Name: " + name + " LabelNames: " + labels.keySet() + " labelValues: " + labels.values() +
          " Value: " + value;
      }
    }
  }

  /**
   * Register the Collector with the default registry.
   */
  public <T extends Collector> T register() {
    return register(CollectorRegistry.defaultRegistry);
  }

  /**
   * Register the Collector with the given registry.
   */
  public <T extends Collector> T register(CollectorRegistry registry) {
    registry.register(this);
    return (T)this;
  }

  /* Various utility functions for implementing Collectors. */

  /**
   * Number of nanoseconds in a second.
   */
  public static final double NANOSECONDS_PER_SECOND = 1E9;
  /**
   * Number of milliseconds in a second.
   */
  public static final double MILLISECONDS_PER_SECOND = 1E3;

  private static final Pattern METRIC_NAME_RE = Pattern.compile("[a-zA-Z_:][a-zA-Z0-9_:]*");
  private static final Pattern METRIC_LABEL_NAME_RE = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
  private static final Pattern RESERVED_METRIC_LABEL_NAME_RE = Pattern.compile("__.*");

  /**
   * Throw an exception if the metric name is invalid.
   */
  protected static void checkMetricName(String name) {
    if (!METRIC_NAME_RE.matcher(name).matches()) {
      throw new IllegalArgumentException("Invalid metric name: " + name);
    }
  }

  /**
   * Throw an exception if the metric label name is invalid.
   */
  protected static void checkMetricLabelName(String name) {
    if (!METRIC_LABEL_NAME_RE.matcher(name).matches()) {
      throw new IllegalArgumentException("Invalid metric label name: " + name);
    }
    if (RESERVED_METRIC_LABEL_NAME_RE.matcher(name).matches()) {
      throw new IllegalArgumentException("Invalid metric label name, reserved for internal use: " + name);
    }
  }

  /**
   * Convert a double to it's string representation in Go.
   */
  public static String doubleToGoString(double d) {
    if (d == Double.POSITIVE_INFINITY) {
      return "+Inf";
    } 
    if (d == Double.NEGATIVE_INFINITY) {
      return "-Inf";
    }
    return Double.toString(d);
  }
}
