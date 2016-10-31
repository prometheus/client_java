package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;

import java.lang.management.ManagementFactory;
import java.lang.management.ClassLoadingMXBean;
import java.util.ArrayList;
import java.util.Collections;
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
  private static final List<String> EMPTY_LABEL = Collections.emptyList();

  private final ClassLoadingMXBean clBean;

  public ClassLoadingExports() {
    this(ManagementFactory.getClassLoadingMXBean());
  }

  public ClassLoadingExports(ClassLoadingMXBean clBean) {
    this.clBean = clBean;
  }

  void addClassLoadingMetrics(List<MetricFamilySamples> sampleFamilies) {
    sampleFamilies.add(
            new MetricFamilySamples(
                    "jvm_classes_loaded",
                    Type.GAUGE,
                    "The number of classes that are currently loaded in the JVM",
                    Collections.singletonList(
                            new MetricFamilySamples.Sample(
                                    "jvm_classes_loaded", EMPTY_LABEL, EMPTY_LABEL,
                                    clBean.getLoadedClassCount()))));

    sampleFamilies.add(
            new MetricFamilySamples(
                    "jvm_classes_loaded_total",
                    Type.COUNTER,
                    "The total number of classes that have been loaded since the JVM has started execution",
                    Collections.singletonList(
                            new MetricFamilySamples.Sample(
                                    "jvm_classes_loaded_total", EMPTY_LABEL, EMPTY_LABEL,
                                    clBean.getTotalLoadedClassCount()))));

    sampleFamilies.add(
            new MetricFamilySamples(
                    "jvm_classes_unloaded_total",
                    Type.COUNTER,
                    "The total number of classes that have been unloaded since the JVM has started execution",
                    Collections.singletonList(
                            new MetricFamilySamples.Sample(
                                    "jvm_classes_unloaded_total", EMPTY_LABEL, EMPTY_LABEL,
                                    clBean.getUnloadedClassCount()))));
  }


  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
    addClassLoadingMetrics(mfs);
    return mfs;
  }
}
