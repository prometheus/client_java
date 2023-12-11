package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;

public class JvmRuntimeInfoMetricTest {

    @Test
    public void testGoodCase() throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        JvmRuntimeInfoMetric.builder()
                .version("1.8.0_382-b05")
                .vendor("Oracle Corporation")
                .runtime("OpenJDK Runtime Environment")
                .register(registry);
        MetricSnapshots snapshots = registry.scrape();

        String expected = "" +
                "# TYPE jvm_runtime info\n" +
                "# HELP jvm_runtime JVM runtime info\n" +
                "jvm_runtime_info{runtime=\"OpenJDK Runtime Environment\",vendor=\"Oracle Corporation\",version=\"1.8.0_382-b05\"} 1\n" +
                "# EOF\n";

        Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
    }
}
