package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.lang.management.BufferPoolMXBean;
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
  private final List<MemoryPoolMXBean> memoryPoolBeans;
  private final List<BufferPoolMXBean> bufferPoolBeans;

  public MemoryPoolsExports() {
    this(
        ManagementFactory.getMemoryMXBean(),
        ManagementFactory.getMemoryPoolMXBeans(),
        ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class));
  }

  public MemoryPoolsExports(MemoryMXBean memoryBean,
                             List<MemoryPoolMXBean> memoryPoolBeans,
                             List<BufferPoolMXBean> bufferPoolBeans) {
    this.memoryBean = memoryBean;
    this.memoryPoolBeans = memoryPoolBeans;
    this.bufferPoolBeans = bufferPoolBeans;
  }

  void addMemoryAreaMetrics(List<MetricFamilySamples> sampleFamilies) {
    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
    MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

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
    for (final MemoryPoolMXBean pool : memoryPoolBeans) {
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
    }
  }

  void addBufferPoolMetrics(List<MetricFamilySamples> sampleFamilies) {
    GaugeMetricFamily used = new GaugeMetricFamily(
            "jvm_buffer_pool_bytes_used",
            "Used bytes of a given JVM buffer pool.",
            Collections.singletonList("pool"));
    sampleFamilies.add(used);
    GaugeMetricFamily capacity = new GaugeMetricFamily(
            "jvm_buffer_pool_bytes_capacity",
            "Bytes capacity of a given JVM buffer pool.",
            Collections.singletonList("pool"));
    sampleFamilies.add(capacity);
    GaugeMetricFamily buffers = new GaugeMetricFamily(
            "jvm_buffer_pool_buffers_used",
            "Used buffers of a given JVM buffer pool.",
            Collections.singletonList("pool"));
    sampleFamilies.add(buffers);
    for (final BufferPoolMXBean pool : bufferPoolBeans) {
      used.addMetric(
              Collections.singletonList(pool.getName()),
              pool.getMemoryUsed());
      capacity.addMetric(
              Collections.singletonList(pool.getName()),
              pool.getTotalCapacity());
      buffers.addMetric(
              Collections.singletonList(pool.getName()),
              pool.getCount());
    }
  }

  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addMemoryAreaMetrics(mfs);
    addMemoryPoolMetrics(mfs);
    addBufferPoolMetrics(mfs);
    return mfs;
  }
}
