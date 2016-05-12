package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    List<MetricFamilySamples.Sample> usedSamples = new ArrayList<MetricFamilySamples.Sample>();

    final Map<String, String> labelsAreaHeap = Collections.singletonMap("area", "heap");
    final Map<String, String> labelsAreaNonHeap = Collections.singletonMap("area", "nonheap");

    usedSamples.add(
        new MetricFamilySamples.Sample(
            "jvm_memory_bytes_used",
            labelsAreaHeap,
            heapUsage.getUsed()));
    usedSamples.add(
        new MetricFamilySamples.Sample(
            "jvm_memory_bytes_used",
            labelsAreaNonHeap,
            nonHeapUsage.getUsed()));
    sampleFamilies.add(
        new MetricFamilySamples(
            "jvm_memory_bytes_used",
            Type.GAUGE,
            "Used bytes of a given JVM memory area.",
            usedSamples));

    List<MetricFamilySamples.Sample> committedSamples = new ArrayList<MetricFamilySamples.Sample>();
    committedSamples.add(
        new MetricFamilySamples.Sample(
            "jvm_memory_bytes_committed",
            labelsAreaHeap,
            heapUsage.getCommitted()));
    committedSamples.add(
        new MetricFamilySamples.Sample(
            "jvm_memory_bytes_committed",
            labelsAreaNonHeap,
            nonHeapUsage.getCommitted()));
    sampleFamilies.add(
        new MetricFamilySamples(
            "jvm_memory_bytes_committed",
            Type.GAUGE,
            "Committed (bytes) of a given JVM memory area.",
            committedSamples));

    List<MetricFamilySamples.Sample> maxSamples = new ArrayList<MetricFamilySamples.Sample>();
    maxSamples.add(
        new MetricFamilySamples.Sample(
            "jvm_memory_bytes_max",
            labelsAreaHeap,
            heapUsage.getMax()));
    maxSamples.add(
        new MetricFamilySamples.Sample(
            "jvm_memory_bytes_max",
            labelsAreaNonHeap,
            nonHeapUsage.getMax()));
    sampleFamilies.add(
        new MetricFamilySamples(
            "jvm_memory_bytes_max",
            Type.GAUGE,
            "Maximum (bytes) of a given JVM memory area.",
            maxSamples));
  }

  void addMemoryPoolMetrics(List<MetricFamilySamples> sampleFamilies) {
    List<MetricFamilySamples.Sample> usedSamples = new ArrayList<MetricFamilySamples.Sample>();
    List<MetricFamilySamples.Sample> committedSamples = new ArrayList<MetricFamilySamples.Sample>();
    List<MetricFamilySamples.Sample> maxSamples = new ArrayList<MetricFamilySamples.Sample>();
    for (final MemoryPoolMXBean pool : poolBeans) {
      MemoryUsage poolUsage = pool.getUsage();
      Map<String, String> labelsPoolName = Collections.singletonMap("pool", pool.getName());
      usedSamples.add(
          new MetricFamilySamples.Sample(
              "jvm_memory_pool_bytes_used",
              labelsPoolName,
              poolUsage.getUsed()));
      committedSamples.add(
          new MetricFamilySamples.Sample(
              "jvm_memory_pool_bytes_committed",
              labelsPoolName,
              poolUsage.getCommitted()));
      maxSamples.add(
          new MetricFamilySamples.Sample(
              "jvm_memory_pool_bytes_max",
              labelsPoolName,
              poolUsage.getMax()));
    }
    sampleFamilies.add(
        new MetricFamilySamples(
            "jvm_memory_pool_bytes_used",
            Type.GAUGE,
            "Used bytes of a given JVM memory pool.",
            usedSamples));

    sampleFamilies.add(
        new MetricFamilySamples(
            "jvm_memory_pool_bytes_committed",
            Type.GAUGE,
            "Limit (bytes) of a given JVM memory pool.",
            committedSamples));

    sampleFamilies.add(
        new MetricFamilySamples(
            "jvm_memory_pool_bytes_max",
            Type.GAUGE,
            "Max (bytes) of a given JVM memory pool.",
            maxSamples));
  }


  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addMemoryAreaMetrics(mfs);
    addMemoryPoolMetrics(mfs);
    return mfs;
  }
}
