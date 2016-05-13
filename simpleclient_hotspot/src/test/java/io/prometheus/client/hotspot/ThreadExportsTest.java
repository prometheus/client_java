package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.ThreadMXBean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ThreadExportsTest {

  private ThreadMXBean mockThreadsBean = Mockito.mock(ThreadMXBean.class);
  private CollectorRegistry registry = new CollectorRegistry();
  private ThreadExports collectorUnderTest;

  private static final String[] EMPTY_LABEL = new String[0];

  @Before
  public void setUp() {
    when(mockThreadsBean.getThreadCount()).thenReturn(300);
    when(mockThreadsBean.getDaemonThreadCount()).thenReturn(200);
    when(mockThreadsBean.getPeakThreadCount()).thenReturn(301);
    when(mockThreadsBean.getTotalStartedThreadCount()).thenReturn(503L);
    collectorUnderTest = new ThreadExports(mockThreadsBean).register(registry);
  }

  @Test
  public void testThreadPools() {
    assertEquals(
            300L,
            registry.getSampleValue("jvm_threads_current"),
            .0000001);
    assertEquals(
            200L,
            registry.getSampleValue("jvm_threads_daemon"),
            .0000001);
    assertEquals(
            301L,
            registry.getSampleValue("jvm_threads_peak"),
            .0000001);
    assertEquals(
            503L,
            registry.getSampleValue("jvm_threads_started_total"),
            .0000001);
  }
}
