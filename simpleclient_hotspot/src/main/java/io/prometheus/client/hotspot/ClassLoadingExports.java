package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

import java.lang.management.ManagementFactory;
import java.lang.management.ClassLoadingMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports metrics about JVM classloading.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new ClassLoadingExports().register();
 * }
 * </pre>
 * Example metrics being exported:
 * <pre>
 *   jvm_classes_loaded{} 1000
 *   jvm_classes_loaded_total{} 2000
 *   jvm_classes_unloaded_total{} 500
 * </pre>
 */
public class ClassLoadingExports extends Collector {
  private final ClassLoadingMXBean clBean;

  public ClassLoadingExports() {
    this(ManagementFactory.getClassLoadingMXBean());
  }

  public ClassLoadingExports(ClassLoadingMXBean clBean) {
    this.clBean = clBean;
  }

  void addClassLoadingMetrics(List<MetricFamilySamples> sampleFamilies) {
    sampleFamilies.add(new GaugeMetricFamily(
          "jvm_classes_loaded",
          "The number of classes that are currently loaded in the JVM",
          clBean.getLoadedClassCount()));
    sampleFamilies.add(new CounterMetricFamily(
          "jvm_classes_loaded_total",
          "The total number of classes that have been loaded since the JVM has started execution",
          clBean.getTotalLoadedClassCount()));
    sampleFamilies.add(new CounterMetricFamily(
          "jvm_classes_unloaded_total",
          "The total number of classes that have been unloaded since the JVM has started execution",
          clBean.getUnloadedClassCount()));
  }


  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addClassLoadingMetrics(mfs);
    return mfs;
  }
}
