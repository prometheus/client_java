package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 *   jvm_gc_collection_seconds_count{gc="PS1"} 200
 *   jvm_gc_collection_seconds_sum{gc="PS1"} 6.7
 * </pre>
 */
public class GarbageCollectorExports extends Collector {
  private final List<GarbageCollectorMXBean> garbageCollectors;

  public GarbageCollectorExports() {
    this(ManagementFactory.getGarbageCollectorMXBeans());
  }

  GarbageCollectorExports(List<GarbageCollectorMXBean> garbageCollectors) {
    this.garbageCollectors = garbageCollectors;
  }

  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples.Sample> samples = new ArrayList<MetricFamilySamples.Sample>();
    for (final GarbageCollectorMXBean gc : garbageCollectors) {
      Map<String, String> labelsGcName = Collections.singletonMap("gc", gc.getName());
      samples.add(
          new MetricFamilySamples.Sample(
              "jvm_gc_collection_seconds_sum",
              labelsGcName,
              gc.getCollectionTime() / MILLISECONDS_PER_SECOND));
      samples.add(
          new MetricFamilySamples.Sample(
              "jvm_gc_collection_seconds_count",
              labelsGcName,
              gc.getCollectionCount()));
    }
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    mfs.add(new MetricFamilySamples(
        "jvm_gc_collection_seconds",
        Type.SUMMARY,
        "Time spent in a given JVM garbage collector in seconds.",
        samples));

    return mfs;
  }
}
