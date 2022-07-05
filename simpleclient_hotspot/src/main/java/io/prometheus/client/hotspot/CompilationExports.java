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

    private final CompilationMXBean compilationMXBean;

    public CompilationExports() {
        this(ManagementFactory.getCompilationMXBean());
    }

    public CompilationExports(CompilationMXBean compilationMXBean) {
        this.compilationMXBean = compilationMXBean;
    }

    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>(1);

        // Sanity check in the scenario that
        if (compilationMXBean != null && compilationMXBean.isCompilationTimeMonitoringSupported()) {
            mfs.add(new CounterMetricFamily(
                    "jvm_compilation_time_ms_total",
                    "The total time in ms taken for HotSpot class compilation",
                    compilationMXBean.getTotalCompilationTime()));
        }

        return mfs;
    }
}