package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports metrics about JVM thread areas.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new ThreadExports().register();
 * }
 * </pre>
 * Example metrics being exported:
 * <pre>
 *   jvm_threads_current{} 300
 *   jvm_threads_daemon{} 200
 *   jvm_threads_peak{} 410
 *   jvm_threads_started_total{} 1200
 * </pre>
 */
public class ThreadExports extends Collector {
  private final ThreadMXBean threadBean;

  public ThreadExports() {
    this(ManagementFactory.getThreadMXBean());
  }

  public ThreadExports(ThreadMXBean threadBean) {
    this.threadBean = threadBean;
  }

  void addThreadMetrics(List<MetricFamilySamples> sampleFamilies) {
    sampleFamilies.add(
        new GaugeMetricFamily(
          "jvm_threads_current",
          "Current thread count of a JVM",
          threadBean.getThreadCount()));

    sampleFamilies.add(
        new GaugeMetricFamily(
          "jvm_threads_daemon",
          "Daemon thread count of a JVM",
          threadBean.getDaemonThreadCount()));

    sampleFamilies.add(
        new GaugeMetricFamily(
          "jvm_threads_peak",
          "Peak thread count of a JVM",
          threadBean.getPeakThreadCount()));

    sampleFamilies.add(
        new CounterMetricFamily(
          "jvm_threads_started_total",
          "Started thread count of a JVM",
          threadBean.getTotalStartedThreadCount()));

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

  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addThreadMetrics(mfs);
    return mfs;
  }
}
