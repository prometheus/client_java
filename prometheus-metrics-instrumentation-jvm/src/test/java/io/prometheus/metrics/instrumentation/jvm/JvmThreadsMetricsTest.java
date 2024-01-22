package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.model.registry.MetricNameFilter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static io.prometheus.metrics.instrumentation.jvm.TestUtil.convertToOpenMetricsFormat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JvmThreadsMetricsTest {

    private ThreadMXBean mockThreadsBean = Mockito.mock(ThreadMXBean.class);
    private ThreadInfo mockThreadInfoBlocked = Mockito.mock(ThreadInfo.class);
    private ThreadInfo mockThreadInfoRunnable1 = Mockito.mock(ThreadInfo.class);
    private ThreadInfo mockThreadInfoRunnable2 = Mockito.mock(ThreadInfo.class);

    @Before
    public void setUp() {
        when(mockThreadsBean.getThreadCount()).thenReturn(300);
        when(mockThreadsBean.getDaemonThreadCount()).thenReturn(200);
        when(mockThreadsBean.getPeakThreadCount()).thenReturn(301);
        when(mockThreadsBean.getTotalStartedThreadCount()).thenReturn(503L);
        when(mockThreadsBean.findDeadlockedThreads()).thenReturn(new long[]{1L, 2L, 3L});
        when(mockThreadsBean.findMonitorDeadlockedThreads()).thenReturn(new long[]{2L, 3L, 4L});
        when(mockThreadsBean.getAllThreadIds()).thenReturn(new long[]{3L, 4L, 5L});
        when(mockThreadInfoBlocked.getThreadState()).thenReturn(Thread.State.BLOCKED);
        when(mockThreadInfoRunnable1.getThreadState()).thenReturn(Thread.State.RUNNABLE);
        when(mockThreadInfoRunnable2.getThreadState()).thenReturn(Thread.State.RUNNABLE);
        when(mockThreadsBean.getThreadInfo(new long[]{3L, 4L, 5L}, 0)).thenReturn(new ThreadInfo[]{
                mockThreadInfoBlocked, mockThreadInfoRunnable1, mockThreadInfoRunnable2
        });
    }

    @Test
    public void testGoodCase() throws IOException {
        PrometheusRegistry registry = new PrometheusRegistry();
        JvmThreadsMetrics.builder()
                .threadBean(mockThreadsBean)
                .isNativeImage(false)
                .register(registry);
        MetricSnapshots snapshots = registry.scrape();

        String expected = "" +
                "# TYPE jvm_threads_current gauge\n" +
                "# HELP jvm_threads_current Current thread count of a JVM\n" +
                "jvm_threads_current 300.0\n" +
                "# TYPE jvm_threads_daemon gauge\n" +
                "# HELP jvm_threads_daemon Daemon thread count of a JVM\n" +
                "jvm_threads_daemon 200.0\n" +
                "# TYPE jvm_threads_deadlocked gauge\n" +
                "# HELP jvm_threads_deadlocked Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers\n" +
                "jvm_threads_deadlocked 3.0\n" +
                "# TYPE jvm_threads_deadlocked_monitor gauge\n" +
                "# HELP jvm_threads_deadlocked_monitor Cycles of JVM-threads that are in deadlock waiting to acquire object monitors\n" +
                "jvm_threads_deadlocked_monitor 3.0\n" +
                "# TYPE jvm_threads_peak gauge\n" +
                "# HELP jvm_threads_peak Peak thread count of a JVM\n" +
                "jvm_threads_peak 301.0\n" +
                "# TYPE jvm_threads_started counter\n" +
                "# HELP jvm_threads_started Started thread count of a JVM\n" +
                "jvm_threads_started_total 503.0\n" +
                "# TYPE jvm_threads_state gauge\n" +
                "# HELP jvm_threads_state Current count of threads by state\n" +
                "jvm_threads_state{state=\"BLOCKED\"} 1.0\n" +
                "jvm_threads_state{state=\"NEW\"} 0.0\n" +
                "jvm_threads_state{state=\"RUNNABLE\"} 2.0\n" +
                "jvm_threads_state{state=\"TERMINATED\"} 0.0\n" +
                "jvm_threads_state{state=\"TIMED_WAITING\"} 0.0\n" +
                "jvm_threads_state{state=\"UNKNOWN\"} 0.0\n" +
                "jvm_threads_state{state=\"WAITING\"} 0.0\n" +
                "# EOF\n";

        Assert.assertEquals(expected, convertToOpenMetricsFormat(snapshots));
    }

    @Test
    public void testIgnoredMetricNotScraped() {
        MetricNameFilter filter = MetricNameFilter.builder()
                .nameMustNotBeEqualTo("jvm_threads_deadlocked")
                .build();

        PrometheusRegistry registry = new PrometheusRegistry();
        JvmThreadsMetrics.builder()
                .threadBean(mockThreadsBean)
                .isNativeImage(false)
                .register(registry);
        registry.scrape(filter);

        verify(mockThreadsBean, times(0)).findDeadlockedThreads();
        verify(mockThreadsBean, times(1)).getThreadCount();
    }

    @Test
    public void testInvalidThreadIds() {
        try {
            String javaVersion = System.getProperty("java.version"); // Example: "21.0.2"
            String majorJavaVersion = javaVersion.replaceAll("\\..*", ""); // Example: "21"
            if (Integer.parseInt(majorJavaVersion) >= 21) {
                // With Java 21 and newer you can no longer have invalid thread ids.
                return;
            }
        } catch (NumberFormatException ignored) {
        }
        PrometheusRegistry registry = new PrometheusRegistry();
        JvmThreadsMetrics.builder().register(registry);

        // Number of threads to create with invalid thread ids
        int numberOfInvalidThreadIds = 2;

        Map<String, Double> expected = getCountByState(registry.scrape());
        expected.compute("UNKNOWN", (key, oldValue) -> oldValue == null ? numberOfInvalidThreadIds : oldValue + numberOfInvalidThreadIds);

        final CountDownLatch countDownLatch = new CountDownLatch(numberOfInvalidThreadIds);

        try {
            // Create and start threads with invalid thread ids (id=0, id=-1, etc.)
            for (int i = 0; i < numberOfInvalidThreadIds; i++) {
                new ThreadWithInvalidId(-i, new TestRunnable(countDownLatch)).start();
            }

            Map<String, Double> actual = getCountByState(registry.scrape());

            Assert.assertEquals(expected.size(), actual.size());
            for (String threadState : expected.keySet()) {
                Assert.assertEquals(expected.get(threadState), actual.get(threadState), 0.0);
            }
        } finally {
            for (int i = 0; i < numberOfInvalidThreadIds; i++) {
                countDownLatch.countDown();
            }
        }
    }

    private Map<String, Double> getCountByState(MetricSnapshots snapshots) {
        Map<String, Double> result = new HashMap<>();
        for (MetricSnapshot snapshot : snapshots) {
            if (snapshot.getMetadata().getName().equals("jvm_threads_state")) {
                for (GaugeSnapshot.GaugeDataPointSnapshot data : ((GaugeSnapshot) snapshot).getDataPoints()) {
                    String state = data.getLabels().get("state");
                    Assert.assertNotNull(state);
                    result.put(state, data.getValue());
                }
            }
        }
        return result;
    }

    private static class ThreadWithInvalidId extends Thread {

        private final long id;

        public ThreadWithInvalidId(long id, Runnable runnable) {
            super(runnable);
            setDaemon(true);
            this.id = id;
        }

        /**
         * Note that only Java versions < 21 call this to get the thread id.
         * With Java 21 and newer it's no longer possible to make an invalid thread id.
         */
        @Override
        public long getId() {
            return this.id;
        }
    }

    private static class TestRunnable implements Runnable {

        private final CountDownLatch countDownLatch;

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
