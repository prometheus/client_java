package io.prometheus.metrics.instrumentation.jvm;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.IOException;
import java.lang.management.ClassLoadingMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JvmClassLoadingMetricsTest {

  private final ClassLoadingMXBean mockClassLoadingBean = Mockito.mock(ClassLoadingMXBean.class);

  @BeforeEach
  public void setUp() {
    when(mockClassLoadingBean.getLoadedClassCount()).thenReturn(1000);
    when(mockClassLoadingBean.getTotalLoadedClassCount()).thenReturn(2000L);
    when(mockClassLoadingBean.getUnloadedClassCount()).thenReturn(500L);
  }

  @Test
  public void testGoodCase() throws IOException {
    PrometheusRegistry registry = new PrometheusRegistry();
    JvmClassLoadingMetrics.builder().classLoadingBean(mockClassLoadingBean).register(registry);
    MetricSnapshots snapshots = registry.scrape();

    String expected =
        """
# TYPE jvm_classes_currently_loaded gauge
# HELP jvm_classes_currently_loaded The number of classes that are currently loaded in the JVM
jvm_classes_currently_loaded 1000.0
# TYPE jvm_classes_loaded counter
# HELP jvm_classes_loaded The total number of classes that have been loaded since the JVM has started execution
jvm_classes_loaded_total 2000.0
# TYPE jvm_classes_unloaded counter
# HELP jvm_classes_unloaded The total number of classes that have been unloaded since the JVM has started execution
jvm_classes_unloaded_total 500.0
# EOF
""";

    assertThat(convertToOpenMetricsFormat(snapshots)).isEqualTo(expected);
  }

  @Test
  public void testIgnoredMetricNotScraped() {
    MetricNameFilter filter =
        MetricNameFilter.builder().nameMustNotBeEqualTo("jvm_classes_currently_loaded").build();

    PrometheusRegistry registry = new PrometheusRegistry();
    JvmClassLoadingMetrics.builder().classLoadingBean(mockClassLoadingBean).register(registry);
    registry.scrape(filter);

    verify(mockClassLoadingBean, times(0)).getLoadedClassCount();
    verify(mockClassLoadingBean, times(1)).getTotalLoadedClassCount();
  }
}
