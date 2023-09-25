package io.prometheus.client.hotspot;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
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
    when(mockThreadsBean.findDeadlockedThreads()).thenReturn(new long[]{1L,2L,3L});
    when(mockThreadsBean.findMonitorDeadlockedThreads()).thenReturn(new long[]{2L,3L,4L});
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

  @Test
  public void testInvalidThreadIds() {
    ThreadExports threadExports = new ThreadExports();

    // Number of threads to create with invalid thread ids
    int numberOfInvalidThreadIds = 2;

    // Get the current thread state counts
    Map<String, Double> expectedThreadStateCountMap = new HashMap<String, Double>();
    List<MetricFamilySamples> metricFamilySamplesList = threadExports.collect();
    for (MetricFamilySamples metricFamilySamples : metricFamilySamplesList) {
      if (ThreadExports.JVM_THREADS_STATE.equals(metricFamilySamples.name)) {
        for (MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
          expectedThreadStateCountMap.put(ThreadExports.JVM_THREADS_STATE + "-" + sample.labelValues.get(0), sample.value);
        }
      }
    }

    // Add numberOfInvalidThreadIds to the expected UNKNOWN thread state count
    expectedThreadStateCountMap.put(
      ThreadExports.JVM_THREADS_STATE + "-" + ThreadExports.UNKNOWN,
      expectedThreadStateCountMap.get(
        ThreadExports.JVM_THREADS_STATE + "-" + ThreadExports.UNKNOWN) + numberOfInvalidThreadIds);

    final CountDownLatch countDownLatch = new CountDownLatch(numberOfInvalidThreadIds);

    try {
      // Create and start threads with invalid thread ids (id=0, id=-1, etc.)
      for (int i = 0; i < numberOfInvalidThreadIds; i++) {
        new TestThread(-i, new TestRunnable(countDownLatch)).start();
      }

      // Get the current thread state counts
      Map<String, Double> actualThreadStateCountMap = new HashMap<String, Double>();
      metricFamilySamplesList = threadExports.collect();
      for (MetricFamilySamples metricFamilySamples : metricFamilySamplesList) {
        if (ThreadExports.JVM_THREADS_STATE.equals(metricFamilySamples.name)) {
          for (MetricFamilySamples.Sample sample : metricFamilySamples.samples) {
            actualThreadStateCountMap.put(ThreadExports.JVM_THREADS_STATE + "-" + sample.labelValues.get(0), sample.value);
          }
        }
      }

      // Assert that we have the same number of thread states
      assertEquals(expectedThreadStateCountMap.size(), actualThreadStateCountMap.size());

      // Check each thread state count
      for (String threadState : expectedThreadStateCountMap.keySet()) {
        double expectedThreadStateCount = expectedThreadStateCountMap.get(threadState);
        double actualThreadStateCount = actualThreadStateCountMap.get(threadState);

        // Assert the expected and actual thread count states are equal
        assertEquals(expectedThreadStateCount, actualThreadStateCount, 0.0);
      }
    } finally {
      for (int i = 0; i < numberOfInvalidThreadIds; i++) {
        countDownLatch.countDown();
      }
    }
  }

  private class TestThread extends Thread {

    private long id;

    public TestThread(long id, Runnable runnable) {
      super(runnable);
      setDaemon(true);
      this.id = id;
    }

    public long getId() {
      return this.id;
    }
  }

  private class TestRunnable implements Runnable {

    private CountDownLatch countDownLatch;

    public TestRunnable(CountDownLatch countDownLatch) {
      this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
      try {
        countDownLatch.await();
      } catch (InterruptedException e) {
        // DO NOTHING
      }
    }
  }
}
