package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Exports JVM and OS version info.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new VersionInfoExports().register();
 * }
 * </pre>
 * Metrics being exported:
 * <pre>
 *   jvm_info{version="",vendor=""} 1
 *   os_info{name="",version="",arch=""} 1
 * </pre>
 */

public class VersionInfoExports extends Collector {


    public List<MetricFamilySamples> collect() {
        String UNKNOWN_LABEL_VALUE = "unknown";
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();

        GaugeMetricFamily jvmInfo = new GaugeMetricFamily(
                "jvm_info",
                "jvm version info",
                Arrays.asList("version", "vendor"));
        jvmInfo.addMetric(Arrays.asList(System.getProperty("java.runtime.version", UNKNOWN_LABEL_VALUE), System.getProperty("java.vm.vendor", UNKNOWN_LABEL_VALUE)), 1L);
        mfs.add(jvmInfo);

        GaugeMetricFamily osInfo = new GaugeMetricFamily(
                "os_info",
                "os version info",
                Arrays.asList("name", "version", "arch"));
        osInfo.addMetric(Arrays.asList(System.getProperty("os.name", UNKNOWN_LABEL_VALUE), System.getProperty("os.version"), System.getProperty("os.arch", UNKNOWN_LABEL_VALUE)), 1L);
        mfs.add(osInfo);

        return mfs;
    }
}