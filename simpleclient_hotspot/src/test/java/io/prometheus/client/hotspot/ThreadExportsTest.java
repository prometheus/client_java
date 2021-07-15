package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class ThreadExportsTest {

  private ThreadMXBean mockThreadsBean = Mockito.mock(ThreadMXBean.class);
  private CollectorRegistry registry = new CollectorRegistry();
  private ThreadExports collectorUnderTest;
  private ThreadInfo mockThreadInfoBlocked = Mockito.mock(ThreadInfo.class);
  private ThreadInfo mockThreadInfoRunnable1 = Mockito.mock(ThreadInfo.class);
  private ThreadInfo mockThreadInfoRunnable2 = Mockito.mock(ThreadInfo.class);

  private static final String[] EMPTY_LABEL = new String[0];
  private static final String[] STATE_LABEL = {"state"};
  private static final String[] STATE_BLOCKED_LABEL = {Thread.State.BLOCKED.toString()};
  private static final String[] STATE_RUNNABLE_LABEL = {Thread.State.RUNNABLE.toString()};
  private static final String[] STATE_TERMINATED_LABEL = {Thread.State.TERMINATED.toString()};


  @Before
  public void setUp() {
    when(mockThreadsBean.getThreadCount()).thenReturn(300);
    when(mockThreadsBean.getDaemonThreadCount()).thenReturn(200);
    when(mockThreadsBean.getPeakThreadCount()).thenReturn(301);
    when(mockThreadsBean.getTotalStartedThreadCount()).thenReturn(503L);
    when(mockThreadsBean.getAllThreadIds()).thenReturn(new long[]{3L,4L,5L});
    when(mockThreadInfoBlocked.getThreadState()).thenReturn(Thread.State.BLOCKED);
    when(mockThreadInfoRunnable1.getThreadState()).thenReturn(Thread.State.RUNNABLE);
    when(mockThreadInfoRunnable2.getThreadState()).thenReturn(Thread.State.RUNNABLE);
    when(mockThreadsBean.getThreadInfo(new long[]{3L, 4L, 5L}, 0)).thenReturn(new ThreadInfo[]{
            mockThreadInfoBlocked, mockThreadInfoRunnable1, mockThreadInfoRunnable2
    });
    collectorUnderTest = new ThreadExports(mockThreadsBean).register(registry);
  }

  @Test
  public void testThreadPools() {
    assertEquals(
            300L,
            registry.getSampleValue(
                    "jvm_threads_current", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
    assertEquals(
            200L,
            registry.getSampleValue(
                    "jvm_threads_daemon", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
    assertEquals(
            301L,
            registry.getSampleValue(
                    "jvm_threads_peak", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
    assertEquals(
            503L,
            registry.getSampleValue(
                    "jvm_threads_started_total", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
    assertNull(
            registry.getSampleValue(
                    "jvm_threads_deadlocked", EMPTY_LABEL, EMPTY_LABEL));
    assertNull(
            registry.getSampleValue(
                    "jvm_threads_deadlocked_monitor", EMPTY_LABEL, EMPTY_LABEL));

    assertEquals(
            1L,
            registry.getSampleValue(
                    "jvm_threads_state", STATE_LABEL, STATE_BLOCKED_LABEL),
            .0000001);

    assertEquals(
            2L,
            registry.getSampleValue(
                    "jvm_threads_state", STATE_LABEL, STATE_RUNNABLE_LABEL),
            .0000001);

    assertEquals(
            0L,
            registry.getSampleValue(
                    "jvm_threads_state", STATE_LABEL, STATE_TERMINATED_LABEL),
            .0000001);
  }
}
