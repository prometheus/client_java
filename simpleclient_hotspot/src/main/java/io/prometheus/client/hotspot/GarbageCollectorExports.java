package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Exports metrics about JVM garbage collectors.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new GarbageCollectorExports().register();
 * }
 * </pre>
 * Example metrics being exported:
 * <pre>
 *   jvm_gc_collections{gc="PS1} 200
 *   jvm_gc_collections_time{gc="PS1} 6.7
 * </pre>
 */
public class GarbageCollectorExports extends Collector {
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
  static final String COLLECTIONS_COUNT_METRIC = "jvm_gc_collections";
  static final String COLLECTIONS_TIME_METRIC = "jvm_gc_collections_time";
  static final List<String> LABEL_NAMES = Arrays.asList("gc");
  private static final List<String> DEFAULT_LABEL = Arrays.asList("unknown");

  private final HashMap<GarbageCollectorMXBean, List<String>> labelValues =
      new HashMap<GarbageCollectorMXBean, List<String>>();
  private final List<GarbageCollectorMXBean> garbageCollectors;

  public GarbageCollectorExports() {
    this(ManagementFactory.getGarbageCollectorMXBeans());
  }

  GarbageCollectorExports(List<GarbageCollectorMXBean> garbageCollectors) {
    this.garbageCollectors = garbageCollectors;
    for (final GarbageCollectorMXBean gc : garbageCollectors) {
      if (!labelValues.containsKey(gc)) {
        String gcName = WHITESPACE.matcher(gc.getName()).replaceAll("-");
        labelValues.put(gc, Arrays.asList(gcName));
      }
    }
  }

  MetricFamilySamples collectorCountMetric() {
    ArrayList<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
    for (final GarbageCollectorMXBean gc : garbageCollectors) {
      samples.add(
          new MetricFamilySamples.Sample(
              COLLECTIONS_COUNT_METRIC,
              LABEL_NAMES,
              labelValues.getOrDefault(gc, DEFAULT_LABEL),
              gc.getCollectionCount()));
    }
    return new MetricFamilySamples(
        COLLECTIONS_COUNT_METRIC,
        Type.COUNTER,
        "Number of collections of a given JVM garbage collector.",
        samples);
  }

  MetricFamilySamples collectorTimeMetric() {
    ArrayList<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
    for (final GarbageCollectorMXBean gc : garbageCollectors) {
      samples.add(
          new MetricFamilySamples.Sample(
              COLLECTIONS_TIME_METRIC,
              LABEL_NAMES,
              labelValues.getOrDefault(gc, DEFAULT_LABEL),
              gc.getCollectionTime() / MILLISECONDS_PER_SECOND));
    }
    return new MetricFamilySamples(
        COLLECTIONS_TIME_METRIC,
        Type.COUNTER,
        "Accumulated time (s) spent in a given JVM garbage collector.",
        samples);
  }

  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    mfs.add(collectorCountMetric());
    mfs.add(collectorTimeMetric());

    return mfs;
  }
}
