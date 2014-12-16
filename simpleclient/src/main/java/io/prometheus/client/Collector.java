
package io.prometheus.client;

import java.util.Arrays;
import java.util.List;

/**
 * A collector for a set of metrics.
 * <p>
 * Normal users should use {@link Gauge}, {@link Counter} and {@link Summary}.
 * Subclasssing Collector is for advanced uses, such as proxying metrics from another monitoring system.
 */
public abstract class Collector {
  /**
   * Return all of the metrics of this Collector.
   */
  public abstract List<MetricFamilySamples> collect();
  public static enum Type {
    COUNTER,
    GAUGE,
    SUMMARY
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
      public final List<String> labelNames;
      public final List<String> labelValues;  // Must have same length as labelNames.
      public final double value;

      public Sample(String name, List<String> labelNames, List<String> labelValues, double value) {
        this.name = name;
        this.labelNames = labelNames;
        this.labelValues = labelValues;
        this.value = value;
      }

      @Override
      public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Sample)) {
          return false;
        }
        Sample other = (Sample) obj;
        return other.name.equals(name) && other.labelNames.equals(labelNames)
          && other.labelValues.equals(labelValues) && other.value == value;
      }

      @Override
      public int hashCode() {
        int hash = 1;
        hash = 37 * hash + name.hashCode();
        hash = 37 * hash + labelNames.hashCode();
        hash = 37 * hash + labelValues.hashCode();
        long d = Double.doubleToLongBits(value);
        hash = 37 * hash + (int)(d ^ (d >>> 32));
        return hash;
      }

      @Override
      public String toString() {
        return "Name: " + name + " LabelNames: " + Arrays.asList(labelNames) + " labelValues: " + labelValues + 
          " Value: " + value;
      }
    }
  }

  /**
   * Number of nanoseconds in a second.
   */
  public static final double NANOSECONDS_PER_SECOND = 1E9;
  /**
   * Number of milliseconds in a second.
   */
  public static final double MILLISECONDS_PER_SECOND = 1E3;

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
}
