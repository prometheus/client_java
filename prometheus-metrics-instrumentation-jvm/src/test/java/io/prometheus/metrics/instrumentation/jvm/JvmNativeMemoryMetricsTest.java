package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.mockito.Mockito.when;

public class JvmNativeMemoryMetricsTest extends TestCase {

  @Test
  public void testNativeMemoryTrackingFail() throws IOException {
    JvmNativeMemoryMetrics.isEnabled.set(true);

    JvmNativeMemoryMetrics.PlatformMBeanServerAdapter adapter = Mockito.mock(JvmNativeMemoryMetrics.PlatformMBeanServerAdapter.class);
    when(adapter.vmNativeMemorySummaryInBytes()).thenThrow(new RuntimeException("mock"));

    PrometheusRegistry registry = new PrometheusRegistry();
    new JvmNativeMemoryMetrics.Builder(PrometheusProperties.get(), adapter).register(registry);
    MetricSnapshots snapshots = registry.scrape();

    String expected = "# EOF\n";

    Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
  }

  @Test
  public void testNativeMemoryTrackingEmpty() throws IOException {
    JvmNativeMemoryMetrics.isEnabled.set(true);

    JvmNativeMemoryMetrics.PlatformMBeanServerAdapter adapter = Mockito.mock(JvmNativeMemoryMetrics.PlatformMBeanServerAdapter.class);
    when(adapter.vmNativeMemorySummaryInBytes()).thenReturn("");

    PrometheusRegistry registry = new PrometheusRegistry();
    new JvmNativeMemoryMetrics.Builder(PrometheusProperties.get(), adapter).register(registry);
    MetricSnapshots snapshots = registry.scrape();

    String expected = "# EOF\n";

    Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
  }

  @Test
  public void testNativeMemoryTrackingDisabled() throws IOException {
    JvmNativeMemoryMetrics.isEnabled.set(true);

    JvmNativeMemoryMetrics.PlatformMBeanServerAdapter adapter = Mockito.mock(JvmNativeMemoryMetrics.PlatformMBeanServerAdapter.class);
    when(adapter.vmNativeMemorySummaryInBytes()).thenReturn("Native memory tracking is not enabled");

    PrometheusRegistry registry = new PrometheusRegistry();
    new JvmNativeMemoryMetrics.Builder(PrometheusProperties.get(), adapter).register(registry);
    MetricSnapshots snapshots = registry.scrape();

    String expected = "# EOF\n";

    Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
  }

  @Test
  public void testNativeMemoryTrackingEnabled() throws IOException {
    JvmNativeMemoryMetrics.isEnabled.set(true);

    JvmNativeMemoryMetrics.PlatformMBeanServerAdapter adapter = Mockito.mock(JvmNativeMemoryMetrics.PlatformMBeanServerAdapter.class);
    when(adapter.vmNativeMemorySummaryInBytes()).thenReturn(
        "Native Memory Tracking:\n" +
            "\n" +
            "Total: reserved=10341970661, committed=642716389\n" +
            "       malloc: 27513573 #22947\n" +
            "       mmap:   reserved=10314457088, committed=615202816\n" +
            "\n" +
            "-                 Java Heap (reserved=8531214336, committed=536870912)\n" +
            "                            (mmap: reserved=8531214336, committed=536870912) \n" +
            " \n" +
            "-                     Class (reserved=1073899939, committed=616867)\n" +
            "                            (classes #1630)\n" +
            "                            (  instance classes #1462, array classes #168)\n" +
            "                            (malloc=158115 #2350) \n" +
            "                            (mmap: reserved=1073741824, committed=458752) \n" +
            "                            (  Metadata:   )\n" +
            "                            (    reserved=67108864, committed=2818048)\n" +
            "                            (    used=2748008)\n" +
            "                            (    waste=70040 =2.49%)\n" +
            "                            (  Class space:)\n" +
            "                            (    reserved=1073741824, committed=458752)\n" +
            "                            (    used=343568)\n" +
            "                            (    waste=115184 =25.11%)\n" +
            " \n" +
            "-                    Thread (reserved=21020080, committed=847280)\n" +
            "                            (thread #20)\n" +
            "                            (stack: reserved=20971520, committed=798720)\n" +
            "                            (malloc=27512 #125) \n" +
            "                            (arena=21048 #37)\n" +
            " \n" +
            "-                      Code (reserved=253796784, committed=7836080)\n" +
            "                            (malloc=105944 #1403) \n" +
            "                            (mmap: reserved=253689856, committed=7729152) \n" +
            "                            (arena=984 #1)\n" +
            " \n" +
            "-                        GC (reserved=373343252, committed=76530708)\n" +
            "                            (malloc=22463508 #720) \n" +
            "                            (mmap: reserved=350879744, committed=54067200) \n" +
            " \n" +
            "-                  Compiler (reserved=1926356, committed=1926356)\n" +
            "                            (malloc=20428 #73) \n" +
            "                            (arena=1905928 #20)\n" +
            " \n" +
            "-                  Internal (reserved=242257, committed=242257)\n" +
            "                            (malloc=176721 #1808) \n" +
            "                            (mmap: reserved=65536, committed=65536) \n" +
            " \n" +
            "-                     Other (reserved=4096, committed=4096)\n" +
            "                            (malloc=4096 #2) \n" +
            " \n" +
            "-                    Symbol (reserved=1505072, committed=1505072)\n" +
            "                            (malloc=1136432 #14482) \n" +
            "                            (arena=368640 #1)\n" +
            " \n" +
            "-    Native Memory Tracking (reserved=373448, committed=373448)\n" +
            "                            (malloc=6280 #91) \n" +
            "                            (tracking overhead=367168)\n" +
            " \n" +
            "-        Shared class space (reserved=16777216, committed=12386304)\n" +
            "                            (mmap: reserved=16777216, committed=12386304) \n" +
            " \n" +
            "-               Arena Chunk (reserved=503216, committed=503216)\n" +
            "                            (malloc=503216) \n" +
            " \n" +
            "-                   Tracing (reserved=33097, committed=33097)\n" +
            "                            (malloc=369 #10) \n" +
            "                            (arena=32728 #1)\n" +
            " \n" +
            "-                 Arguments (reserved=160, committed=160)\n" +
            "                            (malloc=160 #5) \n" +
            " \n" +
            "-                    Module (reserved=169168, committed=169168)\n" +
            "                            (malloc=169168 #1266) \n" +
            " \n" +
            "-                 Safepoint (reserved=8192, committed=8192)\n" +
            "                            (mmap: reserved=8192, committed=8192) \n" +
            " \n" +
            "-           Synchronization (reserved=31160, committed=31160)\n" +
            "                            (malloc=31160 #452) \n" +
            " \n" +
            "-            Serviceability (reserved=600, committed=600)\n" +
            "                            (malloc=600 #6) \n" +
            " \n" +
            "-                 Metaspace (reserved=67120768, committed=2829952)\n" +
            "                            (malloc=11904 #12) \n" +
            "                            (mmap: reserved=67108864, committed=2818048) \n" +
            " \n" +
            "-      String Deduplication (reserved=632, committed=632)\n" +
            "                            (malloc=632 #8) \n" +
            " \n" +
            "-           Object Monitors (reserved=832, committed=832)\n" +
            "                            (malloc=832 #4) \n" +
            " \n" +
            "\n"
    );

    PrometheusRegistry registry = new PrometheusRegistry();
    new JvmNativeMemoryMetrics.Builder(PrometheusProperties.get(), adapter).register(registry);
    MetricSnapshots snapshots = registry.scrape();

    String expected = "" +
        "# TYPE jvm_native_memory_committed_bytes gauge\n" +
        "# UNIT jvm_native_memory_committed_bytes bytes\n" +
        "# HELP jvm_native_memory_committed_bytes Committed bytes of a given JVM. Committed memory represents the amount of memory the JVM is using right now.\n" +
        "jvm_native_memory_committed_bytes{pool=\"Arena Chunk\"} 503216.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Arguments\"} 160.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Class\"} 616867.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Code\"} 7836080.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Compiler\"} 1926356.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"GC\"} 7.6530708E7\n" +
        "jvm_native_memory_committed_bytes{pool=\"Internal\"} 242257.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Java Heap\"} 5.36870912E8\n" +
        "jvm_native_memory_committed_bytes{pool=\"Metaspace\"} 2829952.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Module\"} 169168.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Native Memory Tracking\"} 373448.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Object Monitors\"} 832.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Other\"} 4096.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Safepoint\"} 8192.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Serviceability\"} 600.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Shared class space\"} 1.2386304E7\n" +
        "jvm_native_memory_committed_bytes{pool=\"String Deduplication\"} 632.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Symbol\"} 1505072.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Synchronization\"} 31160.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Thread\"} 847280.0\n" +
        "jvm_native_memory_committed_bytes{pool=\"Total\"} 6.42716389E8\n" +
        "jvm_native_memory_committed_bytes{pool=\"Tracing\"} 33097.0\n" +
        "# TYPE jvm_native_memory_reserved_bytes gauge\n" +
        "# UNIT jvm_native_memory_reserved_bytes bytes\n" +
        "# HELP jvm_native_memory_reserved_bytes Reserved bytes of a given JVM. Reserved memory represents the total amount of memory the JVM can potentially use.\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Arena Chunk\"} 503216.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Arguments\"} 160.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Class\"} 1.073899939E9\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Code\"} 2.53796784E8\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Compiler\"} 1926356.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"GC\"} 3.73343252E8\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Internal\"} 242257.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Java Heap\"} 8.531214336E9\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Metaspace\"} 6.7120768E7\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Module\"} 169168.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Native Memory Tracking\"} 373448.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Object Monitors\"} 832.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Other\"} 4096.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Safepoint\"} 8192.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Serviceability\"} 600.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Shared class space\"} 1.6777216E7\n" +
        "jvm_native_memory_reserved_bytes{pool=\"String Deduplication\"} 632.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Symbol\"} 1505072.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Synchronization\"} 31160.0\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Thread\"} 2.102008E7\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Total\"} 1.0341970661E10\n" +
        "jvm_native_memory_reserved_bytes{pool=\"Tracing\"} 33097.0\n" +
        "# EOF\n";

    Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
  }
}
