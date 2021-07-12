package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;

import java.lang.management.ManagementFactory;
import java.lang.management.CompilationMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports metrics about JVM compilation.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new CompilationExports().register();
 * }
 * </pre>
 * Example metrics being exported:
 * <pre>
 *   jvm_compilation_time{} 123432
 * </pre>
 */
public class CompilationExports extends Collector {
  private final CompilationMXBean compBean;

  public CompilationExports() {
    this(ManagementFactory.getCompilationMXBean());
  }

  public CompilationExports(CompilationMXBean compBean) {
    this.compBean = compBean;
  }

  void addCompilationMetrics(List<MetricFamilySamples> sampleFamilies) {
    sampleFamilies.add(new CounterMetricFamily(
          "jvm_compilation_time_total",
          "The total time taken for HotSpot class compilation",
          compBean.getTotalCompilationTime() / MILLISECONDS_PER_SECOND));
  }


  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addCompilationMetrics(mfs);
    return mfs;
  }
}
