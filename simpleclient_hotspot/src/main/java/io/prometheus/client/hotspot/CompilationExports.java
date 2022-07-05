package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.Predicate;

import java.lang.management.ManagementFactory;
import java.lang.management.CompilationMXBean;
import java.util.ArrayList;
import java.util.List;

import static io.prometheus.client.SampleNameFilter.ALLOW_ALL;

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
 *   jvm_compilation_time_ms_total{} 123432
 * </pre>
 */
public class CompilationExports extends Collector {

    private static final String JVM_COMPILATION_TIME_SECONDS_TOTAL = "jvm_compilation_time_seconds_total";

    private final CompilationMXBean compilationMXBean;

    public CompilationExports() {
        this(ManagementFactory.getCompilationMXBean());
    }

    public CompilationExports(CompilationMXBean compilationMXBean) {
        this.compilationMXBean = compilationMXBean;
    }

    void addCompilationMetrics(List<MetricFamilySamples> sampleFamilies, Predicate<String> nameFilter) {
        // Sanity check in the scenario that a JVM doesn't implement compilation time monitoring
        if (compilationMXBean != null && compilationMXBean.isCompilationTimeMonitoringSupported()) {
            if (nameFilter.test(JVM_COMPILATION_TIME_SECONDS_TOTAL)) {
                sampleFamilies.add(new CounterMetricFamily(
                        JVM_COMPILATION_TIME_SECONDS_TOTAL,
                        "The total time in seconds taken for HotSpot class compilation",
                        compilationMXBean.getTotalCompilationTime() / MILLISECONDS_PER_SECOND));
            }
        }
    }

    @Override
    public List<MetricFamilySamples> collect() {
        return collect(null);
    }

    @Override
    public List<MetricFamilySamples> collect(Predicate<String> nameFilter) {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>(1);
        addCompilationMetrics(mfs, nameFilter == null ? ALLOW_ALL : nameFilter);
        return mfs;
    }
}