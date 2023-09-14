package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.management.BufferPoolMXBean;
import java.util.Arrays;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JvmBufferPoolMetricsTest {

    private final BufferPoolMXBean directBuffer = Mockito.mock(BufferPoolMXBean.class);
    private final BufferPoolMXBean mappedBuffer = Mockito.mock(BufferPoolMXBean.class);

    @Before
    public void setUp() {
        when(directBuffer.getName()).thenReturn("direct");
        when(directBuffer.getCount()).thenReturn(2L);
        when(directBuffer.getMemoryUsed()).thenReturn(1234L);
        when(directBuffer.getTotalCapacity()).thenReturn(3456L);
        when(mappedBuffer.getName()).thenReturn("mapped");
        when(mappedBuffer.getCount()).thenReturn(3L);
        when(mappedBuffer.getMemoryUsed()).thenReturn(2345L);
        when(mappedBuffer.getTotalCapacity()).thenReturn(4567L);
    }

    @Test
    public void testGoodCase() throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        JvmBufferPoolMetrics.builder()
                        .bufferPoolBeans(Arrays.asList(mappedBuffer, directBuffer))
                                .register(registry);
        MetricSnapshots snapshots = registry.scrape();

        String expected = "" +
                "# TYPE jvm_buffer_pool_capacity_bytes gauge\n" +
                "# UNIT jvm_buffer_pool_capacity_bytes bytes\n" +
                "# HELP jvm_buffer_pool_capacity_bytes Bytes capacity of a given JVM buffer pool.\n" +
                "jvm_buffer_pool_capacity_bytes{pool=\"direct\"} 3456.0\n" +
                "jvm_buffer_pool_capacity_bytes{pool=\"mapped\"} 4567.0\n" +
                "# TYPE jvm_buffer_pool_used_buffers gauge\n" +
                "# HELP jvm_buffer_pool_used_buffers Used buffers of a given JVM buffer pool.\n" +
                "jvm_buffer_pool_used_buffers{pool=\"direct\"} 2.0\n" +
                "jvm_buffer_pool_used_buffers{pool=\"mapped\"} 3.0\n" +
                "# TYPE jvm_buffer_pool_used_bytes gauge\n" +
                "# UNIT jvm_buffer_pool_used_bytes bytes\n" +
                "# HELP jvm_buffer_pool_used_bytes Used bytes of a given JVM buffer pool.\n" +
                "jvm_buffer_pool_used_bytes{pool=\"direct\"} 1234.0\n" +
                "jvm_buffer_pool_used_bytes{pool=\"mapped\"} 2345.0\n" +
                "# EOF\n";

        Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
    }

    @Test
    public void testIgnoredMetricNotScraped() {
        MetricNameFilter filter = MetricNameFilter.builder()
                .nameMustNotBeEqualTo("jvm_buffer_pool_used_bytes")
                .build();

        PrometheusRegistry registry = new PrometheusRegistry();
        JvmBufferPoolMetrics.builder()
                .bufferPoolBeans(Arrays.asList(directBuffer, mappedBuffer))
                .register(registry);
        registry.scrape(filter);

        verify(directBuffer, times(0)).getMemoryUsed();
        verify(mappedBuffer, times(0)).getMemoryUsed();
        verify(directBuffer, times(1)).getTotalCapacity();
        verify(mappedBuffer, times(1)).getTotalCapacity();
    }
}
