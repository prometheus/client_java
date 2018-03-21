package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 *   jvm_gc_collection_count_sum{gc="PS1"} 200
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
    GaugeMetricFamily gcCollectionCount = new GaugeMetricFamily(
        "jvm_gc_collection_count_sum",
        "Total number of collections that have occurred for JVM garbage collectors.",
        Collections.singletonList("gc"));
    GaugeMetricFamily gcCollectionTime = new GaugeMetricFamily(
        "jvm_gc_collection_seconds_sum",
        "Approximate accumulated collection elapsed time in seconds for JVM garbage collectors.",
        Collections.singletonList("gc"));
    for (final GarbageCollectorMXBean gc : garbageCollectors) {
        gcCollectionCount.addMetric(
            Collections.singletonList(gc.getName()),
            gc.getCollectionCount());
        gcCollectionTime.addMetric(
            Collections.singletonList(gc.getName()),
            gc.getCollectionTime() / MILLISECONDS_PER_SECOND);
    }
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    mfs.add(gcCollectionCount);
    mfs.add(gcCollectionTime);
    return mfs;
  }
}
