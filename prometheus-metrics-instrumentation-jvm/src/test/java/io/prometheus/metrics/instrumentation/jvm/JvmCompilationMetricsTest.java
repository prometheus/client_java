package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.management.CompilationMXBean;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class JvmCompilationMetricsTest {

    private CompilationMXBean mockCompilationBean = Mockito.mock(CompilationMXBean.class);

    @Before
    public void setUp() {
        when(mockCompilationBean.getTotalCompilationTime()).thenReturn(10000l);
        when(mockCompilationBean.isCompilationTimeMonitoringSupported()).thenReturn(true);
    }

    @Test
    public void testGoodCase() throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        JvmCompilationMetrics.builder()
                .compilationBean(mockCompilationBean)
                .register(registry);
        MetricSnapshots snapshots = registry.scrape();

        String expected = "" +
                "# TYPE jvm_compilation_time_seconds counter\n" +
                "# UNIT jvm_compilation_time_seconds seconds\n" +
                "# HELP jvm_compilation_time_seconds The total time in seconds taken for HotSpot class compilation\n" +
                "jvm_compilation_time_seconds_total 10.0\n" +
                "# EOF\n";

        Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
    }

    @Test
    public void testIgnoredMetricNotScraped() {
        MetricNameFilter filter = MetricNameFilter.builder()
                .nameMustNotBeEqualTo("jvm_compilation_time_seconds_total")
                .build();

        PrometheusRegistry registry = new PrometheusRegistry();
        JvmCompilationMetrics.builder()
                .compilationBean(mockCompilationBean)
                .register(registry);
        MetricSnapshots snapshots = registry.scrape(filter);

        verify(mockCompilationBean, times(0)).getTotalCompilationTime();
        Assert.assertEquals(0, snapshots.size());
    }
}
