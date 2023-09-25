package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class JvmGarbageCollectorMetricsTest {

    private GarbageCollectorMXBean mockGcBean1 = Mockito.mock(GarbageCollectorMXBean.class);
    private GarbageCollectorMXBean mockGcBean2 = Mockito.mock(GarbageCollectorMXBean.class);

    @Before
    public void setUp() {
        when(mockGcBean1.getName()).thenReturn("MyGC1");
        when(mockGcBean1.getCollectionCount()).thenReturn(100L);
        when(mockGcBean1.getCollectionTime()).thenReturn(TimeUnit.SECONDS.toMillis(10));
        when(mockGcBean2.getName()).thenReturn("MyGC2");
        when(mockGcBean2.getCollectionCount()).thenReturn(200L);
        when(mockGcBean2.getCollectionTime()).thenReturn(TimeUnit.SECONDS.toMillis(20));
    }

    @Test
    public void testGoodCase() throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        JvmGarbageCollectorMetrics.builder()
                .garbageCollectorBeans(Arrays.asList(mockGcBean1, mockGcBean2))
                .register(registry);
        MetricSnapshots snapshots = registry.scrape();

        String expected = "" +
                "# TYPE jvm_gc_collection_seconds summary\n" +
                "# UNIT jvm_gc_collection_seconds seconds\n" +
                "# HELP jvm_gc_collection_seconds Time spent in a given JVM garbage collector in seconds.\n" +
                "jvm_gc_collection_seconds_count{gc=\"MyGC1\"} 100\n" +
                "jvm_gc_collection_seconds_sum{gc=\"MyGC1\"} 10.0\n" +
                "jvm_gc_collection_seconds_count{gc=\"MyGC2\"} 200\n" +
                "jvm_gc_collection_seconds_sum{gc=\"MyGC2\"} 20.0\n" +
                "# EOF\n";

        Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
    }

    @Test
    public void testIgnoredMetricNotScraped() {
        MetricNameFilter filter = MetricNameFilter.builder()
                .nameMustNotBeEqualTo("jvm_gc_collection_seconds")
                .build();

        PrometheusRegistry registry = new PrometheusRegistry();
        JvmGarbageCollectorMetrics.builder()
                .garbageCollectorBeans(Arrays.asList(mockGcBean1, mockGcBean2))
                .register(registry);
        MetricSnapshots snapshots = registry.scrape(filter);

        verify(mockGcBean1, times(0)).getCollectionTime();
        verify(mockGcBean1, times(0)).getCollectionCount();
        Assert.assertEquals(0, snapshots.size());
    }
}
