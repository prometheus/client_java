package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import sun.management.HotspotRuntimeMBean;
import sun.management.ManagementFactoryHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Exports metrics about JVM Safepoints.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new SafepointExports().register();
 * }
 * </pre>
 * Example metrics being exported:
 * <pre>
 *   jvm_safepoint_count_total{} 200
 *   jvm_safepoint_total_time_seconds{} 6.7
 *   jvm_safepoint_sync_time_seconds{} 6.7
 * </pre>
 */
public class SafepointExports extends Collector {
  private final HotspotRuntimeMBean hotspotRuntimeMBean;

  public SafepointExports() {
    this(ManagementFactoryHelper.getHotspotRuntimeMBean());
  }

  SafepointExports(HotspotRuntimeMBean hotspotRuntimeMBean) {
    this.hotspotRuntimeMBean = hotspotRuntimeMBean;
  }

  public List<MetricFamilySamples> collect() {
    CounterMetricFamily safepointCount = new CounterMetricFamily(
        "jvm_safepoint_count_total",
        "The number of safepoints taken place since the JVM started.",
            hotspotRuntimeMBean.getSafepointCount());

    CounterMetricFamily safepointTime = new CounterMetricFamily(
        "jvm_safepoint_total_time_seconds",
        "The accumulated time spent at safepoints in seconds. This is the accumulated elapsed time that the application has been stopped for safepoint operations.",
            hotspotRuntimeMBean.getTotalSafepointTime() / MILLISECONDS_PER_SECOND);

    CounterMetricFamily safepointSyncTime = new CounterMetricFamily(
        "jvm_safepoint_sync_time_seconds",
        "The accumulated time spent getting to safepoints in seconds.",
            hotspotRuntimeMBean.getSafepointSyncTime() / MILLISECONDS_PER_SECOND);

    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    mfs.add(safepointCount);
    mfs.add(safepointTime);
    mfs.add(safepointSyncTime);

    return mfs;
  }
}
