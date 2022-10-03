
package io.prometheus.metrics;

import io.prometheus.metrics.exemplars.Exemplar;

import java.util.*;
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
   * Return all metrics of this Collector.
   */
  public abstract List<MetricFamilySamples> collect();

  /**
   * Like {@link #collect()}, but the result should only contain {@code MetricFamilySamples} where
   * {@code sampleNameFilter.test(name)} is {@code true} for at least one Sample name.
   * <p>
   * The default implementation first collects all {@code MetricFamilySamples} and then discards the ones
   * where {@code sampleNameFilter.test(name)} returns {@code false} for all names in
   * {@link MetricFamilySamples#getNames()}.
   * To improve performance, collector implementations should override this method to prevent
   * {@code MetricFamilySamples} from being collected if they will be discarded anyways.
   * See {@code ThreadExports} for an example.
   * <p>
   * Note that the resulting List may contain {@code MetricFamilySamples} where some Sample names return
   * {@code true} for {@code sampleNameFilter.test(name)} but some Sample names return {@code false}.
   * This is ok, because before we produce the output format we will call
   * {@link MetricFamilySamples#filter(Predicate)} to strip all Samples where {@code sampleNameFilter.test(name)}
   * returns {@code false}.
   *
   * @param sampleNameFilter may be {@code null}, indicating that all metrics should be collected.
   */
  public List<MetricFamilySamples> collect(Predicate<String> sampleNameFilter) {
    List<MetricFamilySamples> all = collect();
    if (sampleNameFilter == null) {
      return all;
    }
    List<MetricFamilySamples> remaining = new ArrayList<MetricFamilySamples>(all.size());
    for (MetricFamilySamples mfs : all) {
      for (String name : mfs.getNames()) {
        if (sampleNameFilter.test(name)) {
          remaining.add(mfs);
          break;
        }
      }
    }
    return remaining;
  }

  public enum Type {
    UNKNOWN, // This is untyped in Prometheus text format.
    COUNTER,
    GAUGE,
    STATE_SET,
    INFO,
    HISTOGRAM,
    GAUGE_HISTOGRAM,
    SUMMARY,
  }

  /**
   * A metric, and all of its samples.
   */
  static public class MetricFamilySamples {
    public final String name;
    public final String unit;
    public final Type type;
    public final String help;
    public final List<Sample> samples; // this list is modified when samples are added/removed.

    public MetricFamilySamples(String name, Type type, String help, List<Sample> samples) {
      this(name, "", type, help, samples);
    }

    public MetricFamilySamples(String name, String unit, Type type, String help, List<Sample> samples) {
      if (!unit.isEmpty() && !name.endsWith("_" + unit)) {
        throw new IllegalArgumentException("Metric's unit is not the suffix of the metric name: " + name);
      }
      if ((type == Type.INFO || type == Type.STATE_SET) && !unit.isEmpty()) {
        throw new IllegalArgumentException("Metric is of a type that cannot have a unit: " + name);
      }
      List<Sample> mungedSamples = samples;
      // Deal with _total from pre-OM automatically.
      if (type == Type.COUNTER) {
        if (name.endsWith("_total")) {
          name = name.substring(0, name.length() - 6);
        }
        String withTotal = name + "_total";
        mungedSamples = new ArrayList<Sample>(samples.size());
        for (Sample s: samples) {
          String n = s.name;
          if (name.equals(n)) {
            n = withTotal;
          }
          mungedSamples.add(new Sample(n, s.labelNames, s.labelValues, s.value, s.exemplar, s.timestampMs));
        }
      }
      this.name = name;
      this.unit = unit;
      this.type = type;
      this.help = help;
      this.samples = mungedSamples;
    }

    /**
     * @param sampleNameFilter may be {@code null} indicating that the result contains the complete list of samples.
     * @return A new MetricFamilySamples containing only the Samples matching the {@code sampleNameFilter},
     *         or {@code null} if no Sample matches.
     */
    public MetricFamilySamples filter(Predicate<String> sampleNameFilter) {
      if (sampleNameFilter == null) {
        return this;
      }
      List<Sample> remainingSamples = new ArrayList<Sample>(samples.size());
      for (Sample sample : samples) {
        if (sampleNameFilter.test(sample.name)) {
          remainingSamples.add(sample);
        }
      }
      if (remainingSamples.isEmpty()) {
        return null;
      }
      return new MetricFamilySamples(name, unit, type, help, remainingSamples);
    }

    /**
     * List of names that are reserved for Samples in these MetricsFamilySamples.
     * <p>
     * This is used in two places:
     * <ol>
     *     <li>To check potential name collisions in {@link CollectorRegistry#register(Collector)}.
     *     <li>To check if a collector may contain metrics matching the metric name filter
     *         in {@link Collector#collect(Predicate)}.
     * </ol>
     * Note that {@code getNames()} always includes the name without suffix, even though some
     * metrics types (like Counter) will not have a Sample with that name.
     * The reason is that the name without suffix is used in the metadata comments ({@code # TYPE}, {@code # UNIT},
     * {@code # HELP}), and as this name <a href="https://github.com/prometheus/common/issues/319">must be unique</a>
     * we include the name without suffix here as well.
     */
    public String[] getNames() {
      switch (type) {
        case COUNTER:
          return new String[]{
                  name + "_total",
                  name + "_created",
                  name
          };
        case SUMMARY:
          return new String[]{
                  name + "_count",
                  name + "_sum",
                  name + "_created",
                  name
          };
        case HISTOGRAM:
          return new String[]{
                  name + "_count",
                  name + "_sum",
                  name + "_bucket",
                  name + "_created",
                  name
          };
        case GAUGE_HISTOGRAM:
          return new String[]{
                  name + "_gcount",
                  name + "_gsum",
                  name + "_bucket",
                  name
          };
        case INFO:
          return new String[]{
                  name + "_info",
                  name
          };
        default:
          return new String[]{name};
      }
    }


    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof MetricFamilySamples)) {
        return false;
      }
      MetricFamilySamples other = (MetricFamilySamples) obj;
      
      return other.name.equals(name)
        && other.unit.equals(unit)
        && other.type.equals(type)
        && other.help.equals(help)
        && other.samples.equals(samples);
    }

    @Override
    public int hashCode() {
      int hash = 1;
      hash = 37 * hash + name.hashCode();
      hash = 37 * hash + unit.hashCode();
      hash = 37 * hash + type.hashCode();
      hash = 37 * hash + help.hashCode();
      hash = 37 * hash + samples.hashCode();
      return hash;
    }

    @Override
    public String toString() {
      return "Name: " + name + " Unit:" + unit + " Type: " + type + " Help: " + help +
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
      public final Exemplar exemplar;
      public final Long timestampMs;  // It's an epoch format with milliseconds value included (this field is subject to change).

      public Sample(String name, List<String> labelNames, List<String> labelValues, double value, Exemplar exemplar, Long timestampMs) {
        this.name = name;
        this.labelNames = labelNames;
        this.labelValues = labelValues;
        this.value = value;
        this.exemplar = exemplar;
        this.timestampMs = timestampMs;
      }

      public Sample(String name, List<String> labelNames, List<String> labelValues, double value, Long timestampMs) {
        this(name, labelNames, labelValues, value, null, timestampMs);
      }

      public Sample(String name, List<String> labelNames, List<String> labelValues, double value, Exemplar exemplar) {
        this(name, labelNames, labelValues, value, exemplar, null);
      }

      public Sample(String name, List<String> labelNames, List<String> labelValues, double value) {
        this(name, labelNames, labelValues, value, null, null);
      }

      @Override
      public boolean equals(Object obj) {
        if (!(obj instanceof Sample)) {
          return false;
        }
        Sample other = (Sample) obj;

        return other.name.equals(name) &&
            other.labelNames.equals(labelNames) &&
            other.labelValues.equals(labelValues) &&
            other.value == value &&
            (exemplar == null && other.exemplar == null || other.exemplar != null && other.exemplar.equals(exemplar)) &&
            (timestampMs == null && other.timestampMs == null || other.timestampMs != null && other.timestampMs.equals(timestampMs));
      }

      @Override
      public int hashCode() {
        int hash = 1;
        hash = 37 * hash + name.hashCode();
        hash = 37 * hash + labelNames.hashCode();
        hash = 37 * hash + labelValues.hashCode();
        long d = Double.doubleToLongBits(value);
        hash = 37 * hash + (int)(d ^ (d >>> 32));
        if (timestampMs != null) {
          hash = 37 * hash + timestampMs.hashCode();
        }
        if (exemplar != null) {
          hash = 37 * exemplar.hashCode();
        }
        return hash;
      }

      @Override
      public String toString() {
        return "Name: " + name + " LabelNames: " + labelNames + " labelValues: " + labelValues +
          " Value: " + value + " TimestampMs: " + timestampMs;
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

  public interface Describable {
    /**
     *  Provide a list of metric families this Collector is expected to return.
     *
     *  These should exclude the samples. This is used by the registry to
     *  detect collisions and duplicate registrations.
     *
     *  Usually custom collectors do not have to implement Describable. If
     *  Describable is not implemented and the CollectorRegistry was created
     *  with auto describe enabled (which is the case for the default registry)
     *  then {@link #collect} will be called at registration time instead of
     *  describe. If this could cause problems, either implement a proper
     *  describe, or if that's not practical have describe return an empty
     *  list.
     */
    List<MetricFamilySamples> describe();
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
   * Sanitize metric name
   */
  public static String sanitizeMetricName(String metricName) {
    int length = metricName.length();
    char[] sanitized = new char[length];
    for(int i = 0; i < length; i++) {
      char ch = metricName.charAt(i);
      if(ch == ':' ||
          (ch >= 'a' && ch <= 'z') ||
          (ch >= 'A' && ch <= 'Z') ||
          (i > 0 && ch >= '0' && ch <= '9')) {
        sanitized[i] = ch;
      } else {
        sanitized[i] = '_';
      }
    }
    return new String(sanitized);
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
   * Convert a double to its string representation in Go.
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
