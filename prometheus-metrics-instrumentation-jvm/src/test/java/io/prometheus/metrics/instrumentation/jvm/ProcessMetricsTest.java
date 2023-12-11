package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProcessMetricsTest {

    private com.sun.management.UnixOperatingSystemMXBean sunOsBean = Mockito.mock(com.sun.management.UnixOperatingSystemMXBean.class);
    private java.lang.management.OperatingSystemMXBean javaOsBean = Mockito.mock(java.lang.management.OperatingSystemMXBean.class);
    private ProcessMetrics.Grepper linuxGrepper = Mockito.mock(ProcessMetrics.Grepper.class);
    private ProcessMetrics.Grepper windowsGrepper = Mockito.mock(ProcessMetrics.Grepper.class);
    private RuntimeMXBean runtimeBean = Mockito.mock(RuntimeMXBean.class);

    @Before
    public void setUp() throws IOException {
        when(sunOsBean.getProcessCpuTime()).thenReturn(TimeUnit.MILLISECONDS.toNanos(72));
        when(sunOsBean.getOpenFileDescriptorCount()).thenReturn(127L);
        when(sunOsBean.getMaxFileDescriptorCount()).thenReturn(244L);
        when(runtimeBean.getStartTime()).thenReturn(37100L);
        when(linuxGrepper.lineStartingWith(any(File.class), eq("VmSize:"))).thenReturn("VmSize:     6036 kB");
        when(linuxGrepper.lineStartingWith(any(File.class), eq("VmRSS:"))).thenReturn("VmRSS:      1012 kB");
    }

    @Test
    public void testGoodCase() throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        ProcessMetrics.builder()
                        .osBean(sunOsBean)
                                .runtimeBean(runtimeBean)
                .grepper(linuxGrepper)
                .register(registry);
        MetricSnapshots snapshots = registry.scrape();

        String expected = "" +
                "# TYPE process_cpu_seconds counter\n" +
                "# UNIT process_cpu_seconds seconds\n" +
                "# HELP process_cpu_seconds Total user and system CPU time spent in seconds.\n" +
                "process_cpu_seconds_total 0.072\n" +
                "# TYPE process_max_fds gauge\n" +
                "# HELP process_max_fds Maximum number of open file descriptors.\n" +
                "process_max_fds 244.0\n" +
                "# TYPE process_open_fds gauge\n" +
                "# HELP process_open_fds Number of open file descriptors.\n" +
                "process_open_fds 127.0\n" +
                "# TYPE process_resident_memory_bytes gauge\n" +
                "# UNIT process_resident_memory_bytes bytes\n" +
                "# HELP process_resident_memory_bytes Resident memory size in bytes.\n" +
                "process_resident_memory_bytes 1036288.0\n" +
                "# TYPE process_start_time_seconds gauge\n" +
                "# UNIT process_start_time_seconds seconds\n" +
                "# HELP process_start_time_seconds Start time of the process since unix epoch in seconds.\n" +
                "process_start_time_seconds 37.1\n" +
                "# TYPE process_virtual_memory_bytes gauge\n" +
                "# UNIT process_virtual_memory_bytes bytes\n" +
                "# HELP process_virtual_memory_bytes Virtual memory size in bytes.\n" +
                "process_virtual_memory_bytes 6180864.0\n" +
                "# EOF\n";

        Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
    }

    @Test
    public void testMinimal() throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        ProcessMetrics.builder()
                .osBean(javaOsBean)
                .runtimeBean(runtimeBean)
                .grepper(windowsGrepper)
                .register(registry);
        MetricSnapshots snapshots = registry.scrape();

        String expected = "" +
                "# TYPE process_start_time_seconds gauge\n" +
                "# UNIT process_start_time_seconds seconds\n" +
                "# HELP process_start_time_seconds Start time of the process since unix epoch in seconds.\n" +
                "process_start_time_seconds 37.1\n" +
                "# EOF\n";

        Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
    }

    @Test
    public void testIgnoredMetricNotScraped() {
        MetricNameFilter filter = MetricNameFilter.builder()
                .nameMustNotBeEqualTo("process_max_fds")
                .build();

        PrometheusRegistry registry = new PrometheusRegistry();
        ProcessMetrics.builder()
                .osBean(sunOsBean)
                .runtimeBean(runtimeBean)
                .grepper(linuxGrepper)
                .register(registry);
        registry.scrape(filter);

        verify(sunOsBean, times(0)).getMaxFileDescriptorCount();
        verify(sunOsBean, times(1)).getOpenFileDescriptorCount();
    }
}
