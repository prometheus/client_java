package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

  void addMemoryAreaMetrics(List<MetricFamilySamples> sampleFamilies) {
    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
    MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

    GaugeMetricFamily finalizer = new GaugeMetricFamily(
        "jvm_objects_pending_finalization",
        "The number of objects waiting in the finalizer queue.",
        memoryBean.getObjectPendingFinalizationCount());
    sampleFamilies.add(finalizer);

    GaugeMetricFamily used = new GaugeMetricFamily(
        "jvm_memory_bytes_used",
        "Used bytes of a given JVM memory area.",
        Collections.singletonList("area"));
    used.addMetric(Collections.singletonList("heap"), heapUsage.getUsed());
    used.addMetric(Collections.singletonList("nonheap"), nonHeapUsage.getUsed());
    sampleFamilies.add(used);

    GaugeMetricFamily committed = new GaugeMetricFamily(
        "jvm_memory_bytes_committed",
        "Committed (bytes) of a given JVM memory area.",
        Collections.singletonList("area"));
    committed.addMetric(Collections.singletonList("heap"), heapUsage.getCommitted());
    committed.addMetric(Collections.singletonList("nonheap"), nonHeapUsage.getCommitted());
    sampleFamilies.add(committed);

    GaugeMetricFamily max = new GaugeMetricFamily(
        "jvm_memory_bytes_max",
        "Max (bytes) of a given JVM memory area.",
        Collections.singletonList("area"));
    max.addMetric(Collections.singletonList("heap"), heapUsage.getMax());
    max.addMetric(Collections.singletonList("nonheap"), nonHeapUsage.getMax());
    sampleFamilies.add(max);

    GaugeMetricFamily init = new GaugeMetricFamily(
        "jvm_memory_bytes_init",
        "Initial bytes of a given JVM memory area.",
        Collections.singletonList("area"));
    init.addMetric(Collections.singletonList("heap"), heapUsage.getInit());
    init.addMetric(Collections.singletonList("nonheap"), nonHeapUsage.getInit());
    sampleFamilies.add(init);
  }

  void addMemoryPoolMetrics(List<MetricFamilySamples> sampleFamilies) {
    GaugeMetricFamily used = new GaugeMetricFamily(
        "jvm_memory_pool_bytes_used",
        "Used bytes of a given JVM memory pool.",
        Collections.singletonList("pool"));
    sampleFamilies.add(used);
    GaugeMetricFamily committed = new GaugeMetricFamily(
        "jvm_memory_pool_bytes_committed",
        "Committed bytes of a given JVM memory pool.",
        Collections.singletonList("pool"));
    sampleFamilies.add(committed);
    GaugeMetricFamily max = new GaugeMetricFamily(
        "jvm_memory_pool_bytes_max",
        "Max bytes of a given JVM memory pool.",
        Collections.singletonList("pool"));
    sampleFamilies.add(max);
    GaugeMetricFamily init = new GaugeMetricFamily(
        "jvm_memory_pool_bytes_init",
        "Initial bytes of a given JVM memory pool.",
        Collections.singletonList("pool"));
    sampleFamilies.add(init);
    GaugeMetricFamily collectionUsed = new GaugeMetricFamily(
        "jvm_memory_pool_collection_bytes_used",
        "Used bytes after last collection of a given JVM memory pool.",
        Collections.singletonList("pool"));
    sampleFamilies.add(collectionUsed);
    GaugeMetricFamily collectionCommitted = new GaugeMetricFamily(
        "jvm_memory_pool_collection_bytes_committed",
        "Committed after last collection bytes of a given JVM memory pool.",
        Collections.singletonList("pool"));
    sampleFamilies.add(collectionCommitted);
    GaugeMetricFamily collectionMax = new GaugeMetricFamily(
        "jvm_memory_pool_collection_bytes_max",
        "Max bytes after last collection of a given JVM memory pool.",
        Collections.singletonList("pool"));
    sampleFamilies.add(collectionMax);
    GaugeMetricFamily collectionInit = new GaugeMetricFamily(
        "jvm_memory_pool_collection_bytes_init",
        "Initial after last collection bytes of a given JVM memory pool.",
        Collections.singletonList("pool"));
    sampleFamilies.add(collectionInit);
    for (final MemoryPoolMXBean pool : poolBeans) {
      MemoryUsage poolUsage = pool.getUsage();
      used.addMetric(
          Collections.singletonList(pool.getName()),
          poolUsage.getUsed());
      committed.addMetric(
          Collections.singletonList(pool.getName()),
          poolUsage.getCommitted());
      max.addMetric(
          Collections.singletonList(pool.getName()),
          poolUsage.getMax());
      init.addMetric(
          Collections.singletonList(pool.getName()),
          poolUsage.getInit());
      MemoryUsage collectionPoolUsage = pool.getCollectionUsage();
      if (collectionPoolUsage != null) {
          collectionUsed.addMetric(
              Collections.singletonList(pool.getName()),
              collectionPoolUsage.getUsed());
          collectionCommitted.addMetric(
              Collections.singletonList(pool.getName()),
              collectionPoolUsage.getCommitted());
          collectionMax.addMetric(
              Collections.singletonList(pool.getName()),
              collectionPoolUsage.getMax());
          collectionInit.addMetric(
              Collections.singletonList(pool.getName()),
              collectionPoolUsage.getInit());
      }
    }
  }

  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addMemoryAreaMetrics(mfs);
    addMemoryPoolMetrics(mfs);
    return mfs;
  }
}
