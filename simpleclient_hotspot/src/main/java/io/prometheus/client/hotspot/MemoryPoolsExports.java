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
  private static final List<String> AREA_LABELS = Collections.singletonList("area");
  private static final List<String> POOL_LABELS = Collections.singletonList("pool");
  private static final List<String> HEAP_LABELS = Collections.singletonList("heap");
  private static final List<String> NON_HEAP_LABELS = Collections.singletonList("nonheap");
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

    GaugeMetricFamily used = new GaugeMetricFamily(
        "jvm_memory_bytes_used",
        "Used bytes of a given JVM memory area.",
        AREA_LABELS);
    used.addMetric(HEAP_LABELS, heapUsage.getUsed());
    used.addMetric(NON_HEAP_LABELS, nonHeapUsage.getUsed());
    sampleFamilies.add(used);

    GaugeMetricFamily committed = new GaugeMetricFamily(
        "jvm_memory_bytes_committed",
        "Committed bytes of a given JVM memory area.",
        AREA_LABELS);
    committed.addMetric(HEAP_LABELS, heapUsage.getCommitted());
    committed.addMetric(NON_HEAP_LABELS, nonHeapUsage.getCommitted());
    sampleFamilies.add(committed);

    GaugeMetricFamily max = new GaugeMetricFamily(
        "jvm_memory_bytes_max",
        "Max bytes of a given JVM memory area.",
        AREA_LABELS);
    max.addMetric(HEAP_LABELS, heapUsage.getMax());
    max.addMetric(NON_HEAP_LABELS, nonHeapUsage.getMax());
    sampleFamilies.add(max);

    GaugeMetricFamily init = new GaugeMetricFamily(
        "jvm_memory_bytes_init",
        "Init bytes of a given JVM memory area.",
        AREA_LABELS);
    init.addMetric(HEAP_LABELS, heapUsage.getInit());
    init.addMetric(NON_HEAP_LABELS, nonHeapUsage.getInit());
    sampleFamilies.add(init);
  }

  void addMemoryPoolMetrics(List<MetricFamilySamples> sampleFamilies) {
    GaugeMetricFamily used = new GaugeMetricFamily(
        "jvm_memory_pool_bytes_used",
        "Used bytes of a given JVM memory pool.",
        POOL_LABELS);
    sampleFamilies.add(used);
    GaugeMetricFamily committed = new GaugeMetricFamily(
        "jvm_memory_pool_bytes_committed",
        "Committed bytes of a given JVM memory pool.",
        POOL_LABELS);
    sampleFamilies.add(committed);
    GaugeMetricFamily max = new GaugeMetricFamily(
        "jvm_memory_pool_bytes_max",
        "Max bytes of a given JVM memory pool.",
        POOL_LABELS);
    sampleFamilies.add(max);
    GaugeMetricFamily init = new GaugeMetricFamily(
        "jvm_memory_pool_bytes_init",
        "Init bytes of a given JVM memory pool.",
        POOL_LABELS);
    sampleFamilies.add(init);
    for (final MemoryPoolMXBean pool : poolBeans) {
      List<String> poolNameLabels = Collections.singletonList(pool.getName());
      MemoryUsage poolUsage = pool.getUsage();
      used.addMetric(
          poolNameLabels,
          poolUsage.getUsed());
      committed.addMetric(
          poolNameLabels,
          poolUsage.getCommitted());
      max.addMetric(
          poolNameLabels,
          poolUsage.getMax());
      init.addMetric(
          poolNameLabels,
          poolUsage.getInit());
    }
  }

  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addMemoryAreaMetrics(mfs);
    addMemoryPoolMetrics(mfs);
    return mfs;
  }
}
