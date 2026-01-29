package io.prometheus.metrics.instrumentation.jvm;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JvmNativeMemoryMetricsTest {

  @Test
  void testNativeMemoryTrackingFail() throws IOException {
    JvmNativeMemoryMetrics.isEnabled.set(true);

    JvmNativeMemoryMetrics.PlatformMBeanServerAdapter adapter =
        Mockito.mock(JvmNativeMemoryMetrics.PlatformMBeanServerAdapter.class);
    when(adapter.vmNativeMemorySummaryInBytes()).thenThrow(new RuntimeException("mock"));

    PrometheusRegistry registry = new PrometheusRegistry();
    new JvmNativeMemoryMetrics.Builder(PrometheusProperties.get(), adapter).register(registry);
    MetricSnapshots snapshots = registry.scrape();

    String expected = "# EOF\n";

    assertThat(convertToOpenMetricsFormat(snapshots)).isEqualTo(expected);
  }

  @Test
  void nativeMemoryTrackingNotEnabled() {
    assertThat(
            new JvmNativeMemoryMetrics.DefaultPlatformMBeanServerAdapter()
                .vmNativeMemorySummaryInBytes())
        .isEqualTo("Native memory tracking is not enabled\n");
  }

  @Test
  void testNativeMemoryTrackingEmpty() throws IOException {
    JvmNativeMemoryMetrics.isEnabled.set(true);

    JvmNativeMemoryMetrics.PlatformMBeanServerAdapter adapter =
        Mockito.mock(JvmNativeMemoryMetrics.PlatformMBeanServerAdapter.class);
    when(adapter.vmNativeMemorySummaryInBytes()).thenReturn("");

    PrometheusRegistry registry = new PrometheusRegistry();
    new JvmNativeMemoryMetrics.Builder(PrometheusProperties.get(), adapter).register(registry);
    MetricSnapshots snapshots = registry.scrape();

    String expected = "# EOF\n";

    assertThat(convertToOpenMetricsFormat(snapshots)).isEqualTo(expected);
  }

  @Test
  void testNativeMemoryTrackingDisabled() throws IOException {
    JvmNativeMemoryMetrics.isEnabled.set(true);

    JvmNativeMemoryMetrics.PlatformMBeanServerAdapter adapter =
        Mockito.mock(JvmNativeMemoryMetrics.PlatformMBeanServerAdapter.class);
    when(adapter.vmNativeMemorySummaryInBytes())
        .thenReturn("Native memory tracking is not enabled");

    PrometheusRegistry registry = new PrometheusRegistry();
    new JvmNativeMemoryMetrics.Builder(PrometheusProperties.get(), adapter).register(registry);
    MetricSnapshots snapshots = registry.scrape();

    String expected = "# EOF\n";

    assertThat(convertToOpenMetricsFormat(snapshots)).isEqualTo(expected);
  }

  @Test
  void testNativeMemoryTrackingEnabled() throws IOException {
    JvmNativeMemoryMetrics.isEnabled.set(true);

    JvmNativeMemoryMetrics.PlatformMBeanServerAdapter adapter =
        Mockito.mock(JvmNativeMemoryMetrics.PlatformMBeanServerAdapter.class);
    when(adapter.vmNativeMemorySummaryInBytes())
        .thenReturn(
            """
            Native Memory Tracking:

            Total: reserved=10341970661, committed=642716389
                   malloc: 27513573 #22947
                   mmap:   reserved=10314457088, committed=615202816

            -                 Java Heap (reserved=8531214336, committed=536870912)
                                        (mmap: reserved=8531214336, committed=536870912)\s
            \s
            -                     Class (reserved=1073899939, committed=616867)
                                        (classes #1630)
                                        (  instance classes #1462, array classes #168)
                                        (malloc=158115 #2350)\s
                                        (mmap: reserved=1073741824, committed=458752)\s
                                        (  Metadata:   )
                                        (    reserved=67108864, committed=2818048)
                                        (    used=2748008)
                                        (    waste=70040 =2.49%)
                                        (  Class space:)
                                        (    reserved=1073741824, committed=458752)
                                        (    used=343568)
                                        (    waste=115184 =25.11%)
            \s
            -                    Thread (reserved=21020080, committed=847280)
                                        (thread #20)
                                        (stack: reserved=20971520, committed=798720)
                                        (malloc=27512 #125)\s
                                        (arena=21048 #37)
            \s
            -                      Code (reserved=253796784, committed=7836080)
                                        (malloc=105944 #1403)\s
                                        (mmap: reserved=253689856, committed=7729152)\s
                                        (arena=984 #1)
            \s
            -                        GC (reserved=373343252, committed=76530708)
                                        (malloc=22463508 #720)\s
                                        (mmap: reserved=350879744, committed=54067200)\s
            \s
            -                  Compiler (reserved=1926356, committed=1926356)
                                        (malloc=20428 #73)\s
                                        (arena=1905928 #20)
            \s
            -                  Internal (reserved=242257, committed=242257)
                                        (malloc=176721 #1808)\s
                                        (mmap: reserved=65536, committed=65536)\s
            \s
            -                     Other (reserved=4096, committed=4096)
                                        (malloc=4096 #2)\s
            \s
            -                    Symbol (reserved=1505072, committed=1505072)
                                        (malloc=1136432 #14482)\s
                                        (arena=368640 #1)
            \s
            -    Native Memory Tracking (reserved=373448, committed=373448)
                                        (malloc=6280 #91)\s
                                        (tracking overhead=367168)
            \s
            -        Shared class space (reserved=16777216, committed=12386304)
                                        (mmap: reserved=16777216, committed=12386304)\s
            \s
            -               Arena Chunk (reserved=503216, committed=503216)
                                        (malloc=503216)\s
            \s
            -                   Tracing (reserved=33097, committed=33097)
                                        (malloc=369 #10)\s
                                        (arena=32728 #1)
            \s
            -                 Arguments (reserved=160, committed=160)
                                        (malloc=160 #5)\s
            \s
            -                    Module (reserved=169168, committed=169168)
                                        (malloc=169168 #1266)\s
            \s
            -                 Safepoint (reserved=8192, committed=8192)
                                        (mmap: reserved=8192, committed=8192)\s
            \s
            -           Synchronization (reserved=31160, committed=31160)
                                        (malloc=31160 #452)\s
            \s
            -            Serviceability (reserved=600, committed=600)
                                        (malloc=600 #6)\s
            \s
            -                 Metaspace (reserved=67120768, committed=2829952)
                                        (malloc=11904 #12)\s
                                        (mmap: reserved=67108864, committed=2818048)\s
            \s
            -      String Deduplication (reserved=632, committed=632)
                                        (malloc=632 #8)\s
            \s
            -           Object Monitors (reserved=832, committed=832)
                                        (malloc=832 #4)\s
            \s

            """);

    PrometheusRegistry registry = new PrometheusRegistry();
    new JvmNativeMemoryMetrics.Builder(PrometheusProperties.get(), adapter).register(registry);
    MetricSnapshots snapshots = registry.scrape();

    String expected =
"""
# TYPE jvm_native_memory_committed_bytes gauge
# UNIT jvm_native_memory_committed_bytes bytes
# HELP jvm_native_memory_committed_bytes Committed bytes of a given JVM. Committed memory represents the amount of memory the JVM is using right now.
jvm_native_memory_committed_bytes{pool="Arena Chunk"} 503216.0
jvm_native_memory_committed_bytes{pool="Arguments"} 160.0
jvm_native_memory_committed_bytes{pool="Class"} 616867.0
jvm_native_memory_committed_bytes{pool="Code"} 7836080.0
jvm_native_memory_committed_bytes{pool="Compiler"} 1926356.0
jvm_native_memory_committed_bytes{pool="GC"} 7.6530708E7
jvm_native_memory_committed_bytes{pool="Internal"} 242257.0
jvm_native_memory_committed_bytes{pool="Java Heap"} 5.36870912E8
jvm_native_memory_committed_bytes{pool="Metaspace"} 2829952.0
jvm_native_memory_committed_bytes{pool="Module"} 169168.0
jvm_native_memory_committed_bytes{pool="Native Memory Tracking"} 373448.0
jvm_native_memory_committed_bytes{pool="Object Monitors"} 832.0
jvm_native_memory_committed_bytes{pool="Other"} 4096.0
jvm_native_memory_committed_bytes{pool="Safepoint"} 8192.0
jvm_native_memory_committed_bytes{pool="Serviceability"} 600.0
jvm_native_memory_committed_bytes{pool="Shared class space"} 1.2386304E7
jvm_native_memory_committed_bytes{pool="String Deduplication"} 632.0
jvm_native_memory_committed_bytes{pool="Symbol"} 1505072.0
jvm_native_memory_committed_bytes{pool="Synchronization"} 31160.0
jvm_native_memory_committed_bytes{pool="Thread"} 847280.0
jvm_native_memory_committed_bytes{pool="Total"} 6.42716389E8
jvm_native_memory_committed_bytes{pool="Tracing"} 33097.0
# TYPE jvm_native_memory_reserved_bytes gauge
# UNIT jvm_native_memory_reserved_bytes bytes
# HELP jvm_native_memory_reserved_bytes Reserved bytes of a given JVM. Reserved memory represents the total amount of memory the JVM can potentially use.
jvm_native_memory_reserved_bytes{pool="Arena Chunk"} 503216.0
jvm_native_memory_reserved_bytes{pool="Arguments"} 160.0
jvm_native_memory_reserved_bytes{pool="Class"} 1.073899939E9
jvm_native_memory_reserved_bytes{pool="Code"} 2.53796784E8
jvm_native_memory_reserved_bytes{pool="Compiler"} 1926356.0
jvm_native_memory_reserved_bytes{pool="GC"} 3.73343252E8
jvm_native_memory_reserved_bytes{pool="Internal"} 242257.0
jvm_native_memory_reserved_bytes{pool="Java Heap"} 8.531214336E9
jvm_native_memory_reserved_bytes{pool="Metaspace"} 6.7120768E7
jvm_native_memory_reserved_bytes{pool="Module"} 169168.0
jvm_native_memory_reserved_bytes{pool="Native Memory Tracking"} 373448.0
jvm_native_memory_reserved_bytes{pool="Object Monitors"} 832.0
jvm_native_memory_reserved_bytes{pool="Other"} 4096.0
jvm_native_memory_reserved_bytes{pool="Safepoint"} 8192.0
jvm_native_memory_reserved_bytes{pool="Serviceability"} 600.0
jvm_native_memory_reserved_bytes{pool="Shared class space"} 1.6777216E7
jvm_native_memory_reserved_bytes{pool="String Deduplication"} 632.0
jvm_native_memory_reserved_bytes{pool="Symbol"} 1505072.0
jvm_native_memory_reserved_bytes{pool="Synchronization"} 31160.0
jvm_native_memory_reserved_bytes{pool="Thread"} 2.102008E7
jvm_native_memory_reserved_bytes{pool="Total"} 1.0341970661E10
jvm_native_memory_reserved_bytes{pool="Tracing"} 33097.0
# EOF
""";

    assertThat(convertToOpenMetricsFormat(snapshots)).isEqualTo(expected);
  }
}
