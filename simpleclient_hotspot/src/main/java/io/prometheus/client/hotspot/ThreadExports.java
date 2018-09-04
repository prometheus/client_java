package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    GaugeMetricFamily threadStateFamily = new GaugeMetricFamily(
      "jvm_threads_state",
      "Current count of threads by state",
      Collections.singletonList("state"));

    Map<Thread.State, Integer> threadStateCounts = getThreadStateCountMap();
    for (Map.Entry<Thread.State, Integer> entry : threadStateCounts.entrySet()) {
      threadStateFamily.addMetric(
        Collections.singletonList(entry.getKey().toString()),
        entry.getValue()
      );
    }
    sampleFamilies.add(threadStateFamily);
  }

  private Map<Thread.State, Integer> getThreadStateCountMap() {
    // Get thread information without computing any stack traces
    ThreadInfo[] allThreads = threadBean.getThreadInfo(threadBean.getAllThreadIds(), 0);

    // Initialize the map with all thread states
    HashMap<Thread.State, Integer> threadCounts = new HashMap<Thread.State, Integer>();
    for (Thread.State state : Thread.State.values()) {
      threadCounts.put(state, 0);
    }

    // Collect the actual thread counts
    for (ThreadInfo curThread : allThreads) {
      if (curThread != null) {
        Thread.State threadState = curThread.getThreadState();
        threadCounts.put(threadState, threadCounts.get(threadState) + 1);
      }
    }

    return threadCounts;
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
