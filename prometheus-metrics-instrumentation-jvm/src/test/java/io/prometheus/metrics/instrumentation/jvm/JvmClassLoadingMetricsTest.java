package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JvmClassLoadingMetricsTest {

    private ClassLoadingMXBean mockClassLoadingBean = Mockito.mock(ClassLoadingMXBean.class);

    @Before
    public void setUp() {
        when(mockClassLoadingBean.getLoadedClassCount()).thenReturn(1000);
        when(mockClassLoadingBean.getTotalLoadedClassCount()).thenReturn(2000L);
        when(mockClassLoadingBean.getUnloadedClassCount()).thenReturn(500L);
    }

    @Test
    public void testGoodCase() throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        JvmClassLoadingMetrics.builder()
                .classLoadingBean(mockClassLoadingBean)
                .register(registry);
        MetricSnapshots snapshots = registry.scrape();

        String expected = "" +
                "# TYPE jvm_classes_currently_loaded gauge\n" +
                "# HELP jvm_classes_currently_loaded The number of classes that are currently loaded in the JVM\n" +
                "jvm_classes_currently_loaded 1000.0\n" +
                "# TYPE jvm_classes_loaded counter\n" +
                "# HELP jvm_classes_loaded The total number of classes that have been loaded since the JVM has started execution\n" +
                "jvm_classes_loaded_total 2000.0\n" +
                "# TYPE jvm_classes_unloaded counter\n" +
                "# HELP jvm_classes_unloaded The total number of classes that have been unloaded since the JVM has started execution\n" +
                "jvm_classes_unloaded_total 500.0\n" +
                "# EOF\n";

        Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
    }

    @Test
    public void testIgnoredMetricNotScraped() {
        MetricNameFilter filter = MetricNameFilter.builder()
                .nameMustNotBeEqualTo("jvm_classes_currently_loaded")
                .build();

        PrometheusRegistry registry = new PrometheusRegistry();
        JvmClassLoadingMetrics.builder()
                .classLoadingBean(mockClassLoadingBean)
                .register(registry);
        registry.scrape(filter);

        verify(mockClassLoadingBean, times(0)).getLoadedClassCount();
        verify(mockClassLoadingBean, times(1)).getTotalLoadedClassCount();
    }
}
