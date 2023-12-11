package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Arrays;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JvmMemoryMetricsTest {

    private MemoryMXBean mockMemoryBean = Mockito.mock(MemoryMXBean.class);
    private MemoryPoolMXBean mockPoolsBeanEdenSpace = Mockito.mock(MemoryPoolMXBean.class);
    private MemoryPoolMXBean mockPoolsBeanOldGen = Mockito.mock(MemoryPoolMXBean.class);
    private MemoryUsage memoryUsageHeap = Mockito.mock(MemoryUsage.class);
    private MemoryUsage memoryUsageNonHeap = Mockito.mock(MemoryUsage.class);
    private MemoryUsage memoryUsagePoolEdenSpace = Mockito.mock(MemoryUsage.class);
    private MemoryUsage memoryUsagePoolOldGen = Mockito.mock(MemoryUsage.class);
    private MemoryUsage memoryUsagePoolCollectionEdenSpace = Mockito.mock(MemoryUsage.class);
    private MemoryUsage memoryUsagePoolCollectionOldGen = Mockito.mock(MemoryUsage.class);

    @Before
    public void setUp() {
        when(mockMemoryBean.getHeapMemoryUsage()).thenReturn(memoryUsageHeap);
        when(mockMemoryBean.getNonHeapMemoryUsage()).thenReturn(memoryUsageNonHeap);

        long val = 1L;
        when(mockMemoryBean.getObjectPendingFinalizationCount()).thenReturn((int) val++);

        when(memoryUsageHeap.getUsed()).thenReturn(val++);
        when(memoryUsageHeap.getMax()).thenReturn(val++);
        when(memoryUsageHeap.getCommitted()).thenReturn(val++);
        when(memoryUsageHeap.getInit()).thenReturn(val++);

        when(memoryUsageNonHeap.getUsed()).thenReturn(val++);
        when(memoryUsageNonHeap.getMax()).thenReturn(val++);
        when(memoryUsageNonHeap.getCommitted()).thenReturn(val++);
        when(memoryUsageNonHeap.getInit()).thenReturn(val++);

        when(memoryUsagePoolEdenSpace.getUsed()).thenReturn(val++);
        when(memoryUsagePoolEdenSpace.getMax()).thenReturn(val++);
        when(memoryUsagePoolEdenSpace.getCommitted()).thenReturn(val++);
        when(memoryUsagePoolEdenSpace.getInit()).thenReturn(val++);

        when(memoryUsagePoolOldGen.getUsed()).thenReturn(val++);
        when(memoryUsagePoolOldGen.getMax()).thenReturn(val++);
        when(memoryUsagePoolOldGen.getCommitted()).thenReturn(val++);
        when(memoryUsagePoolOldGen.getInit()).thenReturn(val++);

        when(memoryUsagePoolCollectionEdenSpace.getUsed()).thenReturn(val++);
        when(memoryUsagePoolCollectionEdenSpace.getMax()).thenReturn(val++);
        when(memoryUsagePoolCollectionEdenSpace.getCommitted()).thenReturn(val++);
        when(memoryUsagePoolCollectionEdenSpace.getInit()).thenReturn(val++);

        when(memoryUsagePoolCollectionOldGen.getUsed()).thenReturn(val++);
        when(memoryUsagePoolCollectionOldGen.getMax()).thenReturn(val++);
        when(memoryUsagePoolCollectionOldGen.getCommitted()).thenReturn(val++);
        when(memoryUsagePoolCollectionOldGen.getInit()).thenReturn(val++);

        when(mockPoolsBeanEdenSpace.getName()).thenReturn("PS Eden Space");
        when(mockPoolsBeanEdenSpace.getUsage()).thenReturn(memoryUsagePoolEdenSpace);
        when(mockPoolsBeanEdenSpace.getCollectionUsage()).thenReturn(memoryUsagePoolCollectionEdenSpace);

        when(mockPoolsBeanOldGen.getName()).thenReturn("PS Old Gen");
        when(mockPoolsBeanOldGen.getUsage()).thenReturn(memoryUsagePoolOldGen);
        when(mockPoolsBeanOldGen.getCollectionUsage()).thenReturn(memoryUsagePoolCollectionOldGen);
    }

    @Test
    public void testGoodCase() throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        JvmMemoryMetrics.builder()
                .withMemoryBean(mockMemoryBean)
                .withMemoryPoolBeans(Arrays.asList(mockPoolsBeanEdenSpace, mockPoolsBeanOldGen))
                .register(registry);
        MetricSnapshots snapshots = registry.scrape();

        String expected = "" +
                "# TYPE jvm_memory_committed_bytes gauge\n" +
                "# UNIT jvm_memory_committed_bytes bytes\n" +
                "# HELP jvm_memory_committed_bytes Committed (bytes) of a given JVM memory area.\n" +
                "jvm_memory_committed_bytes{area=\"heap\"} 4.0\n" +
                "jvm_memory_committed_bytes{area=\"nonheap\"} 8.0\n" +
                "# TYPE jvm_memory_init_bytes gauge\n" +
                "# UNIT jvm_memory_init_bytes bytes\n" +
                "# HELP jvm_memory_init_bytes Initial bytes of a given JVM memory area.\n" +
                "jvm_memory_init_bytes{area=\"heap\"} 5.0\n" +
                "jvm_memory_init_bytes{area=\"nonheap\"} 9.0\n" +
                "# TYPE jvm_memory_max_bytes gauge\n" +
                "# UNIT jvm_memory_max_bytes bytes\n" +
                "# HELP jvm_memory_max_bytes Max (bytes) of a given JVM memory area.\n" +
                "jvm_memory_max_bytes{area=\"heap\"} 3.0\n" +
                "jvm_memory_max_bytes{area=\"nonheap\"} 7.0\n" +
                "# TYPE jvm_memory_objects_pending_finalization gauge\n" +
                "# HELP jvm_memory_objects_pending_finalization The number of objects waiting in the finalizer queue.\n" +
                "jvm_memory_objects_pending_finalization 1.0\n" +
                "# TYPE jvm_memory_pool_collection_committed_bytes gauge\n" +
                "# UNIT jvm_memory_pool_collection_committed_bytes bytes\n" +
                "# HELP jvm_memory_pool_collection_committed_bytes Committed after last collection bytes of a given JVM memory pool.\n" +
                "jvm_memory_pool_collection_committed_bytes{pool=\"PS Eden Space\"} 20.0\n" +
                "jvm_memory_pool_collection_committed_bytes{pool=\"PS Old Gen\"} 24.0\n" +
                "# TYPE jvm_memory_pool_collection_init_bytes gauge\n" +
                "# UNIT jvm_memory_pool_collection_init_bytes bytes\n" +
                "# HELP jvm_memory_pool_collection_init_bytes Initial after last collection bytes of a given JVM memory pool.\n" +
                "jvm_memory_pool_collection_init_bytes{pool=\"PS Eden Space\"} 21.0\n" +
                "jvm_memory_pool_collection_init_bytes{pool=\"PS Old Gen\"} 25.0\n" +
                "# TYPE jvm_memory_pool_collection_max_bytes gauge\n" +
                "# UNIT jvm_memory_pool_collection_max_bytes bytes\n" +
                "# HELP jvm_memory_pool_collection_max_bytes Max bytes after last collection of a given JVM memory pool.\n" +
                "jvm_memory_pool_collection_max_bytes{pool=\"PS Eden Space\"} 19.0\n" +
                "jvm_memory_pool_collection_max_bytes{pool=\"PS Old Gen\"} 23.0\n" +
                "# TYPE jvm_memory_pool_collection_used_bytes gauge\n" +
                "# UNIT jvm_memory_pool_collection_used_bytes bytes\n" +
                "# HELP jvm_memory_pool_collection_used_bytes Used bytes after last collection of a given JVM memory pool.\n" +
                "jvm_memory_pool_collection_used_bytes{pool=\"PS Eden Space\"} 18.0\n" +
                "jvm_memory_pool_collection_used_bytes{pool=\"PS Old Gen\"} 22.0\n" +
                "# TYPE jvm_memory_pool_committed_bytes gauge\n" +
                "# UNIT jvm_memory_pool_committed_bytes bytes\n" +
                "# HELP jvm_memory_pool_committed_bytes Committed bytes of a given JVM memory pool.\n" +
                "jvm_memory_pool_committed_bytes{pool=\"PS Eden Space\"} 12.0\n" +
                "jvm_memory_pool_committed_bytes{pool=\"PS Old Gen\"} 16.0\n" +
                "# TYPE jvm_memory_pool_init_bytes gauge\n" +
                "# UNIT jvm_memory_pool_init_bytes bytes\n" +
                "# HELP jvm_memory_pool_init_bytes Initial bytes of a given JVM memory pool.\n" +
                "jvm_memory_pool_init_bytes{pool=\"PS Eden Space\"} 13.0\n" +
                "jvm_memory_pool_init_bytes{pool=\"PS Old Gen\"} 17.0\n" +
                "# TYPE jvm_memory_pool_max_bytes gauge\n" +
                "# UNIT jvm_memory_pool_max_bytes bytes\n" +
                "# HELP jvm_memory_pool_max_bytes Max bytes of a given JVM memory pool.\n" +
                "jvm_memory_pool_max_bytes{pool=\"PS Eden Space\"} 11.0\n" +
                "jvm_memory_pool_max_bytes{pool=\"PS Old Gen\"} 15.0\n" +
                "# TYPE jvm_memory_pool_used_bytes gauge\n" +
                "# UNIT jvm_memory_pool_used_bytes bytes\n" +
                "# HELP jvm_memory_pool_used_bytes Used bytes of a given JVM memory pool.\n" +
                "jvm_memory_pool_used_bytes{pool=\"PS Eden Space\"} 10.0\n" +
                "jvm_memory_pool_used_bytes{pool=\"PS Old Gen\"} 14.0\n" +
                "# TYPE jvm_memory_used_bytes gauge\n" +
                "# UNIT jvm_memory_used_bytes bytes\n" +
                "# HELP jvm_memory_used_bytes Used bytes of a given JVM memory area.\n" +
                "jvm_memory_used_bytes{area=\"heap\"} 2.0\n" +
                "jvm_memory_used_bytes{area=\"nonheap\"} 6.0\n" +
                "# EOF\n";

        Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
    }

    @Test
    public void testIgnoredMetricNotScraped() {
        MetricNameFilter filter = MetricNameFilter.builder()
                .nameMustNotBeEqualTo("jvm_memory_pool_used_bytes")
                .build();

        PrometheusRegistry registry = new PrometheusRegistry();
        JvmMemoryMetrics.builder()
                .withMemoryBean(mockMemoryBean)
                .withMemoryPoolBeans(Arrays.asList(mockPoolsBeanEdenSpace, mockPoolsBeanOldGen))
                .register(registry);
        registry.scrape(filter);

        verify(memoryUsagePoolEdenSpace, times(0)).getUsed();
        verify(memoryUsagePoolOldGen, times(0)).getUsed();
        verify(memoryUsagePoolEdenSpace, times(1)).getMax();
        verify(memoryUsagePoolOldGen, times(1)).getMax();
    }
}
