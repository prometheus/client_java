package io.prometheus.metrics.instrumentation.jvm;

import static java.util.Objects.requireNonNull;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.CounterWithCallback;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Labels;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * JVM Thread metrics. The {@link JvmThreadsMetrics} are registered as part of the {@link
 * JvmMetrics} like this:
 *
 * <pre>{@code
 * JvmMetrics.builder().register();
 * }</pre>
 *
 * However, if you want only the {@link JvmThreadsMetrics} you can also register them directly:
 *
 * <pre>{@code
 * JvmThreadMetrics.builder().register();
 * }</pre>
 *
 * Example metrics being exported:
 *
 * <pre>
 * # HELP jvm_threads_current Current thread count of a JVM
 * # TYPE jvm_threads_current gauge
 * jvm_threads_current 10.0
 * # HELP jvm_threads_daemon Daemon thread count of a JVM
 * # TYPE jvm_threads_daemon gauge
 * jvm_threads_daemon 8.0
 * # HELP jvm_threads_deadlocked Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or ownable synchronizers
 * # TYPE jvm_threads_deadlocked gauge
 * jvm_threads_deadlocked 0.0
 * # HELP jvm_threads_deadlocked_monitor Cycles of JVM-threads that are in deadlock waiting to acquire object monitors
 * # TYPE jvm_threads_deadlocked_monitor gauge
 * jvm_threads_deadlocked_monitor 0.0
 * # HELP jvm_threads_peak Peak thread count of a JVM
 * # TYPE jvm_threads_peak gauge
 * jvm_threads_peak 10.0
 * # HELP jvm_threads_started_total Started thread count of a JVM
 * # TYPE jvm_threads_started_total counter
 * jvm_threads_started_total 10.0
 * # HELP jvm_threads_state Current count of threads by state
 * # TYPE jvm_threads_state gauge
 * jvm_threads_state{state="BLOCKED"} 0.0
 * jvm_threads_state{state="NEW"} 0.0
 * jvm_threads_state{state="RUNNABLE"} 5.0
 * jvm_threads_state{state="TERMINATED"} 0.0
 * jvm_threads_state{state="TIMED_WAITING"} 2.0
 * jvm_threads_state{state="UNKNOWN"} 0.0
 * jvm_threads_state{state="WAITING"} 3.0
 * </pre>
 */
public class JvmThreadsMetrics {

  private static final String UNKNOWN = "UNKNOWN";
  private static final String JVM_THREADS_STATE = "jvm_threads_state";
  private static final String JVM_THREADS_CURRENT = "jvm_threads_current";
  private static final String JVM_THREADS_DAEMON = "jvm_threads_daemon";
  private static final String JVM_THREADS_PEAK = "jvm_threads_peak";
  private static final String JVM_THREADS_STARTED_TOTAL = "jvm_threads_started_total";
  private static final String JVM_THREADS_DEADLOCKED = "jvm_threads_deadlocked";
  private static final String JVM_THREADS_DEADLOCKED_MONITOR = "jvm_threads_deadlocked_monitor";

  private final PrometheusProperties config;
  private final ThreadMXBean threadBean;
  private final boolean isNativeImage;
  private final Labels constLabels;

  private JvmThreadsMetrics(
      boolean isNativeImage,
      ThreadMXBean threadBean,
      PrometheusProperties config,
      Labels constLabels) {
    this.config = config;
    this.threadBean = threadBean;
    this.isNativeImage = isNativeImage;
    this.constLabels = constLabels == null ? Labels.EMPTY : constLabels;
  }

  private void register(PrometheusRegistry registry) {

    GaugeWithCallback.builder(config)
        .name(JVM_THREADS_CURRENT)
        .help("Current thread count of a JVM")
        .callback(callback -> callback.call(threadBean.getThreadCount()))
        .constLabels(constLabels)
        .register(registry);

    GaugeWithCallback.builder(config)
        .name(JVM_THREADS_DAEMON)
        .help("Daemon thread count of a JVM")
        .callback(callback -> callback.call(threadBean.getDaemonThreadCount()))
        .constLabels(constLabels)
        .register(registry);

    GaugeWithCallback.builder(config)
        .name(JVM_THREADS_PEAK)
        .help("Peak thread count of a JVM")
        .callback(callback -> callback.call(threadBean.getPeakThreadCount()))
        .constLabels(constLabels)
        .register(registry);

    CounterWithCallback.builder(config)
        .name(JVM_THREADS_STARTED_TOTAL)
        .help("Started thread count of a JVM")
        .callback(callback -> callback.call(threadBean.getTotalStartedThreadCount()))
        .constLabels(constLabels)
        .register(registry);

    if (!isNativeImage) {
      GaugeWithCallback.builder(config)
          .name(JVM_THREADS_DEADLOCKED)
          .help(
              "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or "
                  + "ownable synchronizers")
          .callback(
              callback -> callback.call(nullSafeArrayLength(threadBean.findDeadlockedThreads())))
          .constLabels(constLabels)
          .register(registry);

      GaugeWithCallback.builder(config)
          .name(JVM_THREADS_DEADLOCKED_MONITOR)
          .help("Cycles of JVM-threads that are in deadlock waiting to acquire object monitors")
          .callback(
              callback ->
                  callback.call(nullSafeArrayLength(threadBean.findMonitorDeadlockedThreads())))
          .constLabels(constLabels)
          .register(registry);

      GaugeWithCallback.builder(config)
          .name(JVM_THREADS_STATE)
          .help("Current count of threads by state")
          .labelNames("state")
          .callback(
              callback -> {
                Map<String, Integer> threadStateCounts = getThreadStateCountMap(threadBean);
                for (Map.Entry<String, Integer> entry : threadStateCounts.entrySet()) {
                  callback.call(entry.getValue(), entry.getKey());
                }
              })
          .constLabels(constLabels)
          .register(registry);
    }
  }

  private Map<String, Integer> getThreadStateCountMap(ThreadMXBean threadBean) {
    long[] threadIds = threadBean.getAllThreadIds();

    // Code to remove any thread id values <= 0
    int writePos = 0;
    for (int i = 0; i < threadIds.length; i++) {
      if (threadIds[i] > 0) {
        threadIds[writePos++] = threadIds[i];
      }
    }

    final int numberOfInvalidThreadIds = threadIds.length - writePos;
    threadIds = Arrays.copyOf(threadIds, writePos);

    // Get thread information without computing any stack traces
    ThreadInfo[] allThreads = threadBean.getThreadInfo(threadIds, 0);

    // Initialize the map with all thread states
    Map<String, Integer> threadCounts = new HashMap<>();
    for (Thread.State state : Thread.State.values()) {
      threadCounts.put(state.name(), 0);
    }

    // Collect the actual thread counts
    for (ThreadInfo curThread : allThreads) {
      if (curThread != null) {
        Thread.State threadState = curThread.getThreadState();
        threadCounts.put(
            threadState.name(), requireNonNull(threadCounts.get(threadState.name())) + 1);
      }
    }

    // Add the thread count for invalid thread ids
    threadCounts.put(UNKNOWN, numberOfInvalidThreadIds);

    return threadCounts;
  }

  private double nullSafeArrayLength(long[] array) {
    return null == array ? 0 : array.length;
  }

  public static Builder builder() {
    return new Builder(PrometheusProperties.get());
  }

  public static Builder builder(PrometheusProperties config) {
    return new Builder(config);
  }

  public static class Builder {

    private final PrometheusProperties config;
    @Nullable private Boolean isNativeImage;
    @Nullable private ThreadMXBean threadBean;
    private Labels constLabels = Labels.EMPTY;

    private Builder(PrometheusProperties config) {
      this.config = config;
    }

    public Builder constLabels(Labels constLabels) {
      this.constLabels = constLabels == null ? Labels.EMPTY : constLabels;
      return this;
    }

    /** Package private. For testing only. */
    Builder threadBean(ThreadMXBean threadBean) {
      this.threadBean = threadBean;
      return this;
    }

    /** Package private. For testing only. */
    Builder isNativeImage(boolean isNativeImage) {
      this.isNativeImage = isNativeImage;
      return this;
    }

    public void register() {
      register(PrometheusRegistry.defaultRegistry);
    }

    public void register(PrometheusRegistry registry) {
      ThreadMXBean threadBean =
          this.threadBean != null ? this.threadBean : ManagementFactory.getThreadMXBean();
      boolean isNativeImage =
          this.isNativeImage != null ? this.isNativeImage : NativeImageChecker.isGraalVmNativeImage;
      new JvmThreadsMetrics(isNativeImage, threadBean, config, constLabels).register(registry);
    }
  }
}
