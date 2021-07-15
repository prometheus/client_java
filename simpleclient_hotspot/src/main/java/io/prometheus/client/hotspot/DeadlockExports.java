package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports metrics about JVM thread deadlocks.
 *
 * Deadlock detection might be an expensive operation.
 * Consider to avoid these metrics for performance-critical applications.
 *
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new DeadlockExports().register();
 * }
 * </pre>
 * Example metrics being exported:
 * <pre>
 *   jvm_threads_deadlocked{} 4
 *   jvm_threads_deadlocked_monitor{} 2
 * </pre>
 */
public class DeadlockExports extends Collector implements HotspotCollector {
  private final ThreadMXBean threadBean;

  public DeadlockExports() {
    this(ManagementFactory.getThreadMXBean());
  }

  public DeadlockExports(ThreadMXBean threadBean) {
    this.threadBean = threadBean;
  }

  void addDeadlockMetrics(List<MetricFamilySamples> sampleFamilies) {
    sampleFamilies.add(
        new GaugeMetricFamily(
        "jvm_threads_deadlocked",
        "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers",
        nullSafeArrayLength(threadBean.findDeadlockedThreads())));

    sampleFamilies.add(
        new GaugeMetricFamily(
        "jvm_threads_deadlocked_monitor",
        "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors",
        nullSafeArrayLength(threadBean.findMonitorDeadlockedThreads())));
  }

  private static double nullSafeArrayLength(long[] array) {
    return null == array ? 0 : array.length;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addDeadlockMetrics(mfs);
    return mfs;
  }
}
