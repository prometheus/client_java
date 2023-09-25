package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.Predicate;
import io.prometheus.client.SummaryMetricFamily;

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
 *   jvm_gc_collection_seconds_count{gc="PS1"} 200
 *   jvm_gc_collection_seconds_sum{gc="PS1"} 6.7
 * </pre>
 */
public class GarbageCollectorExports extends Collector {

  private static final String JVM_GC_COLLECTION_SECONDS = "jvm_gc_collection_seconds";

  private final List<GarbageCollectorMXBean> garbageCollectors;

  public GarbageCollectorExports() {
    this(ManagementFactory.getGarbageCollectorMXBeans());
  }

  GarbageCollectorExports(List<GarbageCollectorMXBean> garbageCollectors) {
    this.garbageCollectors = garbageCollectors;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    return collect(null);
  }

  @Override
  public List<MetricFamilySamples> collect(Predicate<String> nameFilter) {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    if (nameFilter == null || nameFilter.test(JVM_GC_COLLECTION_SECONDS)) {
      SummaryMetricFamily gcCollection = new SummaryMetricFamily(
              JVM_GC_COLLECTION_SECONDS,
              "Time spent in a given JVM garbage collector in seconds.",
              Collections.singletonList("gc"));
      for (final GarbageCollectorMXBean gc : garbageCollectors) {
        gcCollection.addMetric(
                Collections.singletonList(gc.getName()),
                gc.getCollectionCount(),
                gc.getCollectionTime() / MILLISECONDS_PER_SECOND);
      }
      mfs.add(gcCollection);
    }
    return mfs;
  }
}
