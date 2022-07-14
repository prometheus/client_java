package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.Predicate;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.prometheus.client.SampleNameFilter.ALLOW_ALL;

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

  public static final String UNKNOWN = "UNKNOWN";

  public static final String JVM_THREADS_STATE = "jvm_threads_state";

  private static final String JVM_THREADS_CURRENT = "jvm_threads_current";
  private static final String JVM_THREADS_DAEMON = "jvm_threads_daemon";
  private static final String JVM_THREADS_PEAK = "jvm_threads_peak";
  private static final String JVM_THREADS_STARTED_TOTAL = "jvm_threads_started_total";
  private static final String JVM_THREADS_DEADLOCKED = "jvm_threads_deadlocked";
  private static final String JVM_THREADS_DEADLOCKED_MONITOR = "jvm_threads_deadlocked_monitor";

  private final ThreadMXBean threadBean;

  public ThreadExports() {
    this(ManagementFactory.getThreadMXBean());
  }

  public ThreadExports(ThreadMXBean threadBean) {
    this.threadBean = threadBean;
  }

  void addThreadMetrics(List<MetricFamilySamples> sampleFamilies, Predicate<String> nameFilter) {
    if (nameFilter.test(JVM_THREADS_CURRENT)) {
      sampleFamilies.add(
              new GaugeMetricFamily(
                      JVM_THREADS_CURRENT,
                      "Current thread count of a JVM",
                      threadBean.getThreadCount()));
    }

    if (nameFilter.test(JVM_THREADS_DAEMON)) {
      sampleFamilies.add(
              new GaugeMetricFamily(
                      JVM_THREADS_DAEMON,
                      "Daemon thread count of a JVM",
                      threadBean.getDaemonThreadCount()));
    }

    if (nameFilter.test(JVM_THREADS_PEAK)) {
      sampleFamilies.add(
              new GaugeMetricFamily(
                      JVM_THREADS_PEAK,
                      "Peak thread count of a JVM",
                      threadBean.getPeakThreadCount()));
    }

    if (nameFilter.test(JVM_THREADS_STARTED_TOTAL)) {
      sampleFamilies.add(
              new CounterMetricFamily(
                      JVM_THREADS_STARTED_TOTAL,
                      "Started thread count of a JVM",
                      threadBean.getTotalStartedThreadCount()));
    }

    if (!NativeImageChecker.isGraalVmNativeImage) {
      if (nameFilter.test(JVM_THREADS_DEADLOCKED)) {
        sampleFamilies.add(
                new GaugeMetricFamily(
                        JVM_THREADS_DEADLOCKED,
                        "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers",
                        nullSafeArrayLength(threadBean.findDeadlockedThreads())));
      }

      if (nameFilter.test(JVM_THREADS_DEADLOCKED_MONITOR)) {
        sampleFamilies.add(
                new GaugeMetricFamily(
                        JVM_THREADS_DEADLOCKED_MONITOR,
                        "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors",
                        nullSafeArrayLength(threadBean.findMonitorDeadlockedThreads())));
      }

      if (nameFilter.test(JVM_THREADS_STATE)) {
        GaugeMetricFamily threadStateFamily = new GaugeMetricFamily(
                JVM_THREADS_STATE,
                "Current count of threads by state",
                Collections.singletonList("state"));

        Map<String, Integer> threadStateCounts = getThreadStateCountMap();
        for (Map.Entry<String, Integer> entry : threadStateCounts.entrySet()) {
          threadStateFamily.addMetric(
                  Collections.singletonList(entry.getKey()),
                  entry.getValue()
          );
        }
        sampleFamilies.add(threadStateFamily);
      }
    }
  }

  private Map<String, Integer> getThreadStateCountMap() {
    long[] threadIds = threadBean.getAllThreadIds();

    // Code to remove any thread id values <= 0
    int writePos = 0;
    for (int i = 0; i < threadIds.length; i++) {
      if (threadIds[i] > 0) {
        threadIds[writePos++] = threadIds[i];
      }
    }

    int numberOfInvalidThreadIds = threadIds.length - writePos;
    threadIds = Arrays.copyOf(threadIds, writePos);

    // Get thread information without computing any stack traces
    ThreadInfo[] allThreads = threadBean.getThreadInfo(threadIds, 0);

    // Initialize the map with all thread states
    HashMap<String, Integer> threadCounts = new HashMap<String, Integer>();
    for (Thread.State state : Thread.State.values()) {
      threadCounts.put(state.name(), 0);
    }

    // Collect the actual thread counts
    for (ThreadInfo curThread : allThreads) {
      if (curThread != null) {
        Thread.State threadState = curThread.getThreadState();
        threadCounts.put(threadState.name(), threadCounts.get(threadState.name()) + 1);
      }
    }

    // Add the thread count for invalid thread ids
    threadCounts.put(UNKNOWN, numberOfInvalidThreadIds);

    return threadCounts;
  }

  private static double nullSafeArrayLength(long[] array) {
    return null == array ? 0 : array.length;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    return collect(null);
  }

  @Override
  public List<MetricFamilySamples> collect(Predicate<String> nameFilter) {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addThreadMetrics(mfs, nameFilter == null ? ALLOW_ALL : nameFilter);
    return mfs;
  }
}