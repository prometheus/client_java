package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.Info;

import java.util.List;

/**
 * Exports JVM version info.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new VersionInfoExports().register();
 * }
 * </pre>
 * Metrics being exported:
 * <pre>
 *   jvm_info{version="1.8.0_151-b12",vendor="Oracle Corporation",runtime="OpenJDK Runtime Environment",} 1.0
 * </pre>
 */

public class VersionInfoExports extends Collector implements HotspotCollector {


    public List<MetricFamilySamples> collect() {
        Info i = Info.build().name("jvm").help("VM version info").create();
        i.info(
            "version", System.getProperty("java.runtime.version", "unknown"),
            "vendor", System.getProperty("java.vm.vendor", "unknown"),
            "runtime", System.getProperty("java.runtime.name", "unknown")
        );
        return i.collect();
    }
}
