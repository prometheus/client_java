package io.prometheus.metrics.instrumentation.jvm;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.IOException;
import java.lang.management.CompilationMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JvmCompilationMetricsTest {

  private final CompilationMXBean mockCompilationBean = Mockito.mock(CompilationMXBean.class);

  @BeforeEach
  void setUp() {
    when(mockCompilationBean.getTotalCompilationTime()).thenReturn(10000L);
    when(mockCompilationBean.isCompilationTimeMonitoringSupported()).thenReturn(true);
  }

  @Test
  void testGoodCase() throws IOException {
    PrometheusRegistry registry = new PrometheusRegistry();
    JvmCompilationMetrics.builder().compilationBean(mockCompilationBean).register(registry);
    MetricSnapshots snapshots = registry.scrape();

    String expected =
"""
# TYPE jvm_compilation_time_seconds counter
# UNIT jvm_compilation_time_seconds seconds
# HELP jvm_compilation_time_seconds The total time in seconds taken for HotSpot class compilation
jvm_compilation_time_seconds_total 10.0
# EOF
""";

    assertThat(convertToOpenMetricsFormat(snapshots)).isEqualTo(expected);
  }

  @Test
  void testIgnoredMetricNotScraped() {
    MetricNameFilter filter =
        MetricNameFilter.builder()
            .nameMustNotBeEqualTo("jvm_compilation_time_seconds_total")
            .build();

    PrometheusRegistry registry = new PrometheusRegistry();
    JvmCompilationMetrics.builder().compilationBean(mockCompilationBean).register(registry);
    MetricSnapshots snapshots = registry.scrape(filter);

    verify(mockCompilationBean, times(0)).getTotalCompilationTime();
    assertThat(snapshots.size()).isZero();
  }
}
