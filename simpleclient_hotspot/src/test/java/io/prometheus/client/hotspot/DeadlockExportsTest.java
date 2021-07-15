package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.ThreadMXBean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class DeadlockExportsTest {

  private ThreadMXBean mockThreadsBean = Mockito.mock(ThreadMXBean.class);
  private CollectorRegistry registry = new CollectorRegistry();
  private DeadlockExports collectorUnderTest;

  private static final String[] EMPTY_LABEL = new String[0];

  @Before
  public void setUp() {
    when(mockThreadsBean.findDeadlockedThreads()).thenReturn(new long[]{1L,2L,3L});
    when(mockThreadsBean.findMonitorDeadlockedThreads()).thenReturn(new long[]{2L,3L,4L});
    collectorUnderTest = new DeadlockExports(mockThreadsBean).register(registry);
  }

  @Test
  public void testDeadlockExports() {
    assertNull(
            registry.getSampleValue(
                    "jvm_threads_current", EMPTY_LABEL, EMPTY_LABEL));
    assertNull(
            registry.getSampleValue(
                    "jvm_threads_daemon", EMPTY_LABEL, EMPTY_LABEL));
    assertNull(
            registry.getSampleValue(
                    "jvm_threads_peak", EMPTY_LABEL, EMPTY_LABEL));
    assertNull(
            registry.getSampleValue(
                    "jvm_threads_started_total", EMPTY_LABEL, EMPTY_LABEL));
    assertEquals(
            3L,
            registry.getSampleValue(
                    "jvm_threads_deadlocked", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
    assertEquals(
            3L,
            registry.getSampleValue(
                    "jvm_threads_deadlocked_monitor", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
  }
}
