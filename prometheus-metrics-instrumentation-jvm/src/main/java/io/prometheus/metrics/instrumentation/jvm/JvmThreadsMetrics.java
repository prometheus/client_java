package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.CounterWithCallback;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

  private JvmThreadsMetrics(
      boolean isNativeImage, ThreadMXBean threadBean, PrometheusProperties config) {
    this.config = config;
    this.threadBean = threadBean;
    this.isNativeImage = isNativeImage;
  }

  private void register(PrometheusRegistry registry) {

    GaugeWithCallback.builder(config)
        .name(JVM_THREADS_CURRENT)
        .help("Current thread count of a JVM")
        .callback(callback -> callback.call(threadBean.getThreadCount()))
        .register(registry);

    GaugeWithCallback.builder(config)
        .name(JVM_THREADS_DAEMON)
        .help("Daemon thread count of a JVM")
        .callback(callback -> callback.call(threadBean.getDaemonThreadCount()))
        .register(registry);

    GaugeWithCallback.builder(config)
        .name(JVM_THREADS_PEAK)
        .help("Peak thread count of a JVM")
        .callback(callback -> callback.call(threadBean.getPeakThreadCount()))
        .register(registry);

    CounterWithCallback.builder(config)
        .name(JVM_THREADS_STARTED_TOTAL)
        .help("Started thread count of a JVM")
        .callback(callback -> callback.call(threadBean.getTotalStartedThreadCount()))
        .register(registry);

    if (!isNativeImage) {
      GaugeWithCallback.builder(config)
          .name(JVM_THREADS_DEADLOCKED)
          .help(
              "Cycles of JVM-threads that are in deadlock waiting to acquire object monitors or "
                  + "ownable synchronizers")
          .callback(
              callback -> callback.call(nullSafeArrayLength(threadBean.findDeadlockedThreads())))
          .register(registry);

      GaugeWithCallback.builder(config)
          .name(JVM_THREADS_DEADLOCKED_MONITOR)
          .help("Cycles of JVM-threads that are in deadlock waiting to acquire object monitors")
          .callback(
              callback ->
                  callback.call(nullSafeArrayLength(threadBean.findMonitorDeadlockedThreads())))
          .register(registry);

      GaugeWithCallback.builder(config)
          .name(JVM_THREADS_STATE)
          .help("Current count of threads by state")
          .labelNames("state")
          .callback(
              callback -> {
                Map<String, Integer> threadStateCounts = getThreadStateCountMapFromThreadGroup();
                for (Map.Entry<String, Integer> entry : threadStateCounts.entrySet()) {
                  callback.call(entry.getValue(), entry.getKey());
                }
              })
          .register(registry);
    }
  }

  private Map<String, Integer> getThreadStateCountMapFromThreadGroup() {
    int threadsNew = 0;
    int threadsRunnable = 0;
    int threadsBlocked = 0;
    int threadsWaiting = 0;
    int threadsTimedWaiting = 0;
    int threadsTerminated = 0;
    int threadsUnknown = 0;
    ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
    Thread[] threads = new Thread[threadGroup.activeCount()];
    threadGroup.enumerate(threads);
    for (Thread thread : threads) {
      if (thread == null) {
        // race protection
        continue;
      }
      switch (thread.getState()) {
        case NEW:           threadsNew++;           break;
        case RUNNABLE:      threadsRunnable++;      break;
        case BLOCKED:       threadsBlocked++;       break;
        case WAITING:       threadsWaiting++;       break;
        case TIMED_WAITING: threadsTimedWaiting++;  break;
        case TERMINATED:    threadsTerminated++;    break;
        default:
          threadsUnknown++;
      }
    }

    // Initialize the map with all thread states
    Map<String, Integer> threadCounts = new HashMap<>();

    threadCounts.put(Thread.State.NEW.name(), threadsNew);
    threadCounts.put(Thread.State.RUNNABLE.name(), threadsRunnable);
    threadCounts.put(Thread.State.BLOCKED.name(), threadsBlocked);
    threadCounts.put(Thread.State.WAITING.name(), threadsWaiting);
    threadCounts.put(Thread.State.TIMED_WAITING.name(), threadsTimedWaiting);
    threadCounts.put(Thread.State.TERMINATED.name(), threadsTerminated);
    // Add the thread count for invalid thread ids
    threadCounts.put(UNKNOWN, threadsUnknown);

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
    private Boolean isNativeImage;
    private ThreadMXBean threadBean;

    private Builder(PrometheusProperties config) {
      this.config = config;
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
      new JvmThreadsMetrics(isNativeImage, threadBean, config).register(registry);
    }
  }
}
