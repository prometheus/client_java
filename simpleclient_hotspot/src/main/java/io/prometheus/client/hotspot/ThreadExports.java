package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
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
            new MetricFamilySamples(
                    "jvm_threads_current",
                    Type.GAUGE,
                    "Current thread count of a JVM",
                    Collections.singletonList(
                            new MetricFamilySamples.Sample(
                                    "jvm_threads_current",
                                    threadBean.getThreadCount()))));

    sampleFamilies.add(
            new MetricFamilySamples(
                    "jvm_threads_daemon",
                    Type.GAUGE,
                    "Daemon thread count of a JVM",
                    Collections.singletonList(
                            new MetricFamilySamples.Sample(
                                    "jvm_threads_daemon",
                                    threadBean.getDaemonThreadCount()))));

    sampleFamilies.add(
            new MetricFamilySamples(
                    "jvm_peak_threads",
                    Type.GAUGE,
                    "Peak thread count of a JVM",
                    Collections.singletonList(
                            new MetricFamilySamples.Sample(
                                    "jvm_threads_peak",
                                    threadBean.getPeakThreadCount()))));

    sampleFamilies.add(
            new MetricFamilySamples(
                    "jvm_threads_started_total",
                    Type.COUNTER,
                    "Started thread count of a JVM",
                    Collections.singletonList(
                            new MetricFamilySamples.Sample(
                                    "jvm_threads_started_total",
                                    threadBean.getTotalStartedThreadCount()))));
  }


  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addThreadMetrics(mfs);
    return mfs;
  }
}
