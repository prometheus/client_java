package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

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
 *   jvm_memory_used{area="heap} 2000000
 *   jvm_memory_limit{area="nonheap"} 200000
 *   jvm_memory_pool_used{pool="PS-Eden-Space"} 2000
 * </pre>
 */
public class MemoryPoolsExports extends Collector {
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
  static final String MEMORY_USED_METRIC = "jvm_memory_used";
  static final String MEMORY_LIMIT_METRIC = "jvm_memory_limit";
  static final String POOLS_USED_METRIC = "jvm_memory_pool_used";
  static final String POOLS_LIMIT_METRIC = "jvm_memory_pool_limit";

  private static final List<String> MEMORY_LABEL_NAMES = Arrays.asList("area");
  private static final List<String> MEMORY_HEAP_LABEL = Arrays.asList("heap");
  private static final List<String> MEMORY_NONHEAP_LABEL = Arrays.asList("nonheap");

  private static final List<String> POOLS_LABEL_NAMES = Arrays.asList("pool");

  private final HashMap<MemoryPoolMXBean, List<String>> poolLabelValues = new HashMap<MemoryPoolMXBean, List<String>>();
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
    for (final MemoryPoolMXBean pool : poolBeans) {
      if (!poolLabelValues.containsKey(pool)) {
        String gcName = WHITESPACE.matcher(pool.getName()).replaceAll("-");
        poolLabelValues.put(pool, Arrays.asList(gcName));
      }
    }
  }

  void addMemoryAreaMetrics(List<MetricFamilySamples> sampleFamilies) {
    MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
    MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
    ArrayList<MetricFamilySamples.Sample> usedSamples = new ArrayList<MetricFamilySamples.Sample>();
    usedSamples.add(
        new MetricFamilySamples.Sample(
            MEMORY_USED_METRIC,
            MEMORY_LABEL_NAMES,
            MEMORY_HEAP_LABEL,
            heapUsage.getUsed()));
    usedSamples.add(
        new MetricFamilySamples.Sample(
            MEMORY_USED_METRIC,
            MEMORY_LABEL_NAMES,
            MEMORY_NONHEAP_LABEL,
            nonHeapUsage.getUsed()));
    sampleFamilies.add(
        new MetricFamilySamples(
            MEMORY_USED_METRIC,
            Type.GAUGE,
            "Used bytes of a given JVM memory area (heap, nonheap).",
            usedSamples));
    ArrayList<MetricFamilySamples.Sample> limitSamples = new ArrayList<MetricFamilySamples.Sample>();
    limitSamples.add(
        new MetricFamilySamples.Sample(
            MEMORY_LIMIT_METRIC,
            MEMORY_LABEL_NAMES,
            MEMORY_HEAP_LABEL,
            heapUsage.getMax() == -1 ? heapUsage.getMax() : heapUsage.getCommitted()));
    limitSamples.add(
        new MetricFamilySamples.Sample(
            MEMORY_LIMIT_METRIC,
            MEMORY_LABEL_NAMES,
            MEMORY_NONHEAP_LABEL,
            nonHeapUsage.getMax() == -1 ? nonHeapUsage.getMax() : nonHeapUsage.getCommitted()));
    sampleFamilies.add(
        new MetricFamilySamples(
            MEMORY_LIMIT_METRIC,
            Type.GAUGE,
            "Limit (bytes) of a given JVM memory area (heap, nonheap).",
            limitSamples));
  }

  void addMemoryPoolMetrics(List<MetricFamilySamples> sampleFamilies) {
    ArrayList<MetricFamilySamples.Sample> usedSamples = new ArrayList<MetricFamilySamples.Sample>();
    ArrayList<MetricFamilySamples.Sample> limitSamples = new ArrayList<MetricFamilySamples.Sample>();
    for (final MemoryPoolMXBean pool : poolBeans) {
      MemoryUsage poolUsage = pool.getUsage();
      usedSamples.add(
          new MetricFamilySamples.Sample(
              POOLS_USED_METRIC,
              POOLS_LABEL_NAMES,
              poolLabelValues.get(pool),
              poolUsage.getUsed()));
      limitSamples.add(
          new MetricFamilySamples.Sample(
              POOLS_LIMIT_METRIC,
              POOLS_LABEL_NAMES,
              poolLabelValues.get(pool),
              poolUsage.getMax() != -1 ? poolUsage.getMax() : poolUsage.getCommitted()));
    }
    sampleFamilies.add(
        new MetricFamilySamples(
            POOLS_USED_METRIC,
            Type.GAUGE,
            "Used bytes of a given JVM memory pool.",
            usedSamples));

    sampleFamilies.add(
        new MetricFamilySamples(
            POOLS_LIMIT_METRIC,
            Type.GAUGE,
            "Limit (bytes) of a given JVM memory pool.",
            limitSamples));
  }


  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addMemoryAreaMetrics(mfs);
    addMemoryPoolMetrics(mfs);
    return mfs;
  }
}
