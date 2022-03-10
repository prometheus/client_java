package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.Predicate;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.prometheus.client.SampleNameFilter.ALLOW_ALL;

/**
 * Exports metrics about JVM memory areas.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new MemoryPoolsExports().register();
 * }
 * </pre>
 * Example metrics being exported:
 * <pre>
 *   jvm_memory_bytes_used{area="heap"} 2000000
 *   jvm_memory_bytes_committed{area="nonheap"} 200000
 *   jvm_memory_bytes_max{area="nonheap"} 2000000
 *   jvm_memory_pool_bytes_used{pool="PS Eden Space"} 2000
 * </pre>
 */
public class MemoryPoolsExports extends Collector {

  private static final String JVM_MEMORY_OBJECTS_PENDING_FINALIZATION = "jvm_memory_objects_pending_finalization";
  private static final String JVM_MEMORY_BYTES_USED = "jvm_memory_bytes_used";
  private static final String JVM_MEMORY_BYTES_COMMITTED = "jvm_memory_bytes_committed";
  private static final String JVM_MEMORY_BYTES_MAX = "jvm_memory_bytes_max";
  private static final String JVM_MEMORY_BYTES_INIT = "jvm_memory_bytes_init";

  // Note: The Prometheus naming convention is that units belong at the end of the metric name.
  // For new metrics like jvm_memory_pool_collection_used_bytes we follow that convention.
  // For old metrics like jvm_memory_pool_bytes_used we keep the names as they are to avoid a breaking change.

  private static final String JVM_MEMORY_POOL_BYTES_USED = "jvm_memory_pool_bytes_used";
  private static final String JVM_MEMORY_POOL_BYTES_COMMITTED = "jvm_memory_pool_bytes_committed";
  private static final String JVM_MEMORY_POOL_BYTES_MAX = "jvm_memory_pool_bytes_max";
  private static final String JVM_MEMORY_POOL_BYTES_INIT = "jvm_memory_pool_bytes_init";
  private static final String JVM_MEMORY_POOL_COLLECTION_USED_BYTES = "jvm_memory_pool_collection_used_bytes";
  private static final String JVM_MEMORY_POOL_COLLECTION_COMMITTED_BYTES = "jvm_memory_pool_collection_committed_bytes";
  private static final String JVM_MEMORY_POOL_COLLECTION_MAX_BYTES = "jvm_memory_pool_collection_max_bytes";
  private static final String JVM_MEMORY_POOL_COLLECTION_INIT_BYTES = "jvm_memory_pool_collection_init_bytes";

  private final MemoryMXBean memoryBean;
  private final List<MemoryPoolMXBean> poolBeans;

  public MemoryPoolsExports() {
    this(
        ManagementFactory.getMemoryMXBean(),
        ManagementFactory.getMemoryPoolMXBeans());
  }

  public MemoryPoolsExports(MemoryMXBean memoryBean,
                             List<MemoryPoolMXBean> poolBeans) {
    this.memoryBean = memoryBean;
    this.poolBeans = poolBeans;
  }

  void addMemoryAreaMetrics(List<MetricFamilySamples> sampleFamilies, Predicate<String> nameFilter) {
    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
    MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

    if (nameFilter.test(JVM_MEMORY_OBJECTS_PENDING_FINALIZATION)) {
      GaugeMetricFamily finalizer = new GaugeMetricFamily(
              JVM_MEMORY_OBJECTS_PENDING_FINALIZATION,
              "The number of objects waiting in the finalizer queue.",
              memoryBean.getObjectPendingFinalizationCount());
      sampleFamilies.add(finalizer);
    }

    if (nameFilter.test(JVM_MEMORY_BYTES_USED)) {
      GaugeMetricFamily used = new GaugeMetricFamily(
              JVM_MEMORY_BYTES_USED,
              "Used bytes of a given JVM memory area.",
              Collections.singletonList("area"));
      used.addMetric(Collections.singletonList("heap"), heapUsage.getUsed());
      used.addMetric(Collections.singletonList("nonheap"), nonHeapUsage.getUsed());
      sampleFamilies.add(used);
    }

    if (nameFilter.test(JVM_MEMORY_BYTES_COMMITTED)) {
      GaugeMetricFamily committed = new GaugeMetricFamily(
              JVM_MEMORY_BYTES_COMMITTED,
              "Committed (bytes) of a given JVM memory area.",
              Collections.singletonList("area"));
      committed.addMetric(Collections.singletonList("heap"), heapUsage.getCommitted());
      committed.addMetric(Collections.singletonList("nonheap"), nonHeapUsage.getCommitted());
      sampleFamilies.add(committed);
    }

    if (nameFilter.test(JVM_MEMORY_BYTES_MAX)) {
      GaugeMetricFamily max = new GaugeMetricFamily(
              JVM_MEMORY_BYTES_MAX,
              "Max (bytes) of a given JVM memory area.",
              Collections.singletonList("area"));
      max.addMetric(Collections.singletonList("heap"), heapUsage.getMax());
      max.addMetric(Collections.singletonList("nonheap"), nonHeapUsage.getMax());
      sampleFamilies.add(max);
    }

    if (nameFilter.test(JVM_MEMORY_BYTES_INIT)) {
      GaugeMetricFamily init = new GaugeMetricFamily(
              JVM_MEMORY_BYTES_INIT,
              "Initial bytes of a given JVM memory area.",
              Collections.singletonList("area"));
      init.addMetric(Collections.singletonList("heap"), heapUsage.getInit());
      init.addMetric(Collections.singletonList("nonheap"), nonHeapUsage.getInit());
      sampleFamilies.add(init);
    }
  }

  void addMemoryPoolMetrics(List<MetricFamilySamples> sampleFamilies, Predicate<String> nameFilter) {

    boolean anyPoolMetricPassesFilter = false;

    GaugeMetricFamily used = null;
    if (nameFilter.test(JVM_MEMORY_POOL_BYTES_USED)) {
      used = new GaugeMetricFamily(
              JVM_MEMORY_POOL_BYTES_USED,
              "Used bytes of a given JVM memory pool.",
              Collections.singletonList("pool"));
      sampleFamilies.add(used);
      anyPoolMetricPassesFilter = true;
    }
    GaugeMetricFamily committed = null;
    if (nameFilter.test(JVM_MEMORY_POOL_BYTES_COMMITTED)) {
      committed = new GaugeMetricFamily(
              JVM_MEMORY_POOL_BYTES_COMMITTED,
              "Committed bytes of a given JVM memory pool.",
              Collections.singletonList("pool"));
      sampleFamilies.add(committed);
      anyPoolMetricPassesFilter = true;
    }
    GaugeMetricFamily max = null;
    if (nameFilter.test(JVM_MEMORY_POOL_BYTES_MAX)) {
      max = new GaugeMetricFamily(
              JVM_MEMORY_POOL_BYTES_MAX,
              "Max bytes of a given JVM memory pool.",
              Collections.singletonList("pool"));
      sampleFamilies.add(max);
      anyPoolMetricPassesFilter = true;
    }
    GaugeMetricFamily init = null;
    if (nameFilter.test(JVM_MEMORY_POOL_BYTES_INIT)) {
      init = new GaugeMetricFamily(
              JVM_MEMORY_POOL_BYTES_INIT,
              "Initial bytes of a given JVM memory pool.",
              Collections.singletonList("pool"));
      sampleFamilies.add(init);
      anyPoolMetricPassesFilter = true;
    }
    GaugeMetricFamily collectionUsed = null;
    if (nameFilter.test(JVM_MEMORY_POOL_COLLECTION_USED_BYTES)) {
      collectionUsed = new GaugeMetricFamily(
              JVM_MEMORY_POOL_COLLECTION_USED_BYTES,
              "Used bytes after last collection of a given JVM memory pool.",
              Collections.singletonList("pool"));
      sampleFamilies.add(collectionUsed);
      anyPoolMetricPassesFilter = true;
    }
    GaugeMetricFamily collectionCommitted = null;
    if (nameFilter.test(JVM_MEMORY_POOL_COLLECTION_COMMITTED_BYTES)) {
      collectionCommitted = new GaugeMetricFamily(
              JVM_MEMORY_POOL_COLLECTION_COMMITTED_BYTES,
              "Committed after last collection bytes of a given JVM memory pool.",
              Collections.singletonList("pool"));
      sampleFamilies.add(collectionCommitted);
      anyPoolMetricPassesFilter = true;
    }
    GaugeMetricFamily collectionMax = null;
    if (nameFilter.test(JVM_MEMORY_POOL_COLLECTION_MAX_BYTES)) {
      collectionMax = new GaugeMetricFamily(
              JVM_MEMORY_POOL_COLLECTION_MAX_BYTES,
              "Max bytes after last collection of a given JVM memory pool.",
              Collections.singletonList("pool"));
      sampleFamilies.add(collectionMax);
      anyPoolMetricPassesFilter = true;
    }
    GaugeMetricFamily collectionInit = null;
    if (nameFilter.test(JVM_MEMORY_POOL_COLLECTION_INIT_BYTES)) {
      collectionInit = new GaugeMetricFamily(
              JVM_MEMORY_POOL_COLLECTION_INIT_BYTES,
              "Initial after last collection bytes of a given JVM memory pool.",
              Collections.singletonList("pool"));
      sampleFamilies.add(collectionInit);
      anyPoolMetricPassesFilter = true;
    }
    if (anyPoolMetricPassesFilter) {
      for (final MemoryPoolMXBean pool : poolBeans) {
        MemoryUsage poolUsage = pool.getUsage();
        if (poolUsage != null) {
          addPoolMetrics(used, committed, max, init, pool.getName(), poolUsage);
        }
        MemoryUsage collectionPoolUsage = pool.getCollectionUsage();
        if (collectionPoolUsage != null) {
          addPoolMetrics(collectionUsed, collectionCommitted, collectionMax, collectionInit, pool.getName(), collectionPoolUsage);
        }
      }
    }
  }

  private void addPoolMetrics(GaugeMetricFamily used, GaugeMetricFamily committed, GaugeMetricFamily max, GaugeMetricFamily init, String poolName, MemoryUsage poolUsage) {
    if (used != null) {
      used.addMetric(Collections.singletonList(poolName), poolUsage.getUsed());
    }
    if (committed != null) {
      committed.addMetric(Collections.singletonList(poolName), poolUsage.getCommitted());
    }
    if (max != null) {
      max.addMetric(Collections.singletonList(poolName), poolUsage.getMax());
    }
    if (init != null) {
      init.addMetric(Collections.singletonList(poolName), poolUsage.getInit());
    }
  }

  @Override
  public List<MetricFamilySamples> collect() {
    return collect(null);
  }

  @Override
  public List<MetricFamilySamples> collect(Predicate<String> nameFilter) {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addMemoryAreaMetrics(mfs, nameFilter == null ? ALLOW_ALL : nameFilter);
    addMemoryPoolMetrics(mfs, nameFilter == null ? ALLOW_ALL : nameFilter);
    return mfs;
  }
}
