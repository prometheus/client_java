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
import java.lang.management.GarbageCollectorMXBean;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JvmGarbageCollectorMetricsTest {

  private final GarbageCollectorMXBean mockGcBean1 = Mockito.mock(GarbageCollectorMXBean.class);
  private final GarbageCollectorMXBean mockGcBean2 = Mockito.mock(GarbageCollectorMXBean.class);

  @BeforeEach
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

    String expected =
        """
        # TYPE jvm_gc_collection_seconds summary
        # UNIT jvm_gc_collection_seconds seconds
        # HELP jvm_gc_collection_seconds Time spent in a given JVM garbage collector in seconds.
        jvm_gc_collection_seconds_count{gc="MyGC1"} 100
        jvm_gc_collection_seconds_sum{gc="MyGC1"} 10.0
        jvm_gc_collection_seconds_count{gc="MyGC2"} 200
        jvm_gc_collection_seconds_sum{gc="MyGC2"} 20.0
        # EOF
        """;

    assertThat(convertToOpenMetricsFormat(snapshots)).isEqualTo(expected);
  }

  @Test
  public void testIgnoredMetricNotScraped() {
    MetricNameFilter filter =
        MetricNameFilter.builder().nameMustNotBeEqualTo("jvm_gc_collection_seconds").build();

    PrometheusRegistry registry = new PrometheusRegistry();
    JvmGarbageCollectorMetrics.builder()
        .garbageCollectorBeans(Arrays.asList(mockGcBean1, mockGcBean2))
        .register(registry);
    MetricSnapshots snapshots = registry.scrape(filter);

    verify(mockGcBean1, times(0)).getCollectionTime();
    verify(mockGcBean1, times(0)).getCollectionCount();
    assertThat(snapshots.size()).isZero();
  }
}
