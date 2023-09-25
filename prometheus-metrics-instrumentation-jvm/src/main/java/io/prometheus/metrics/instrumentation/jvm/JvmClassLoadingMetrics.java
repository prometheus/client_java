package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.Predicate;

import java.lang.management.ManagementFactory;
import java.lang.management.ClassLoadingMXBean;
import java.util.ArrayList;
import java.util.List;

import static io.prometheus.client.SampleNameFilter.ALLOW_ALL;

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
 *   jvm_classes_currently_loaded{} 1000
 *   jvm_classes_loaded_total{} 2000
 *   jvm_classes_unloaded_total{} 500
 * </pre>
 */
public class ClassLoadingExports extends Collector {

  private static final String JVM_CLASSES_CURRENTLY_LOADED = "jvm_classes_currently_loaded";
  private static final String JVM_CLASSES_LOADED_TOTAL = "jvm_classes_loaded_total";
  private static final String JVM_CLASSES_UNLOADED_TOTAL = "jvm_classes_unloaded_total";

  private final ClassLoadingMXBean clBean;

  public ClassLoadingExports() {
    this(ManagementFactory.getClassLoadingMXBean());
  }

  public ClassLoadingExports(ClassLoadingMXBean clBean) {
    this.clBean = clBean;
  }

  void addClassLoadingMetrics(List<MetricFamilySamples> sampleFamilies, Predicate<String> nameFilter) {
    if (nameFilter.test(JVM_CLASSES_CURRENTLY_LOADED)) {
      sampleFamilies.add(new GaugeMetricFamily(
              JVM_CLASSES_CURRENTLY_LOADED,
              "The number of classes that are currently loaded in the JVM",
              clBean.getLoadedClassCount()));
    }
    if (nameFilter.test(JVM_CLASSES_LOADED_TOTAL)) {
      sampleFamilies.add(new CounterMetricFamily(
              JVM_CLASSES_LOADED_TOTAL,
              "The total number of classes that have been loaded since the JVM has started execution",
              clBean.getTotalLoadedClassCount()));
    }
    if (nameFilter.test(JVM_CLASSES_UNLOADED_TOTAL)) {
      sampleFamilies.add(new CounterMetricFamily(
              JVM_CLASSES_UNLOADED_TOTAL,
              "The total number of classes that have been unloaded since the JVM has started execution",
              clBean.getUnloadedClassCount()));
    }
  }

  @Override
  public List<MetricFamilySamples> collect() {
    return collect(null);
  }

  @Override
  public List<MetricFamilySamples> collect(Predicate<String> nameFilter) {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addClassLoadingMetrics(mfs, nameFilter == null ? ALLOW_ALL : nameFilter);
    return mfs;
  }
}
