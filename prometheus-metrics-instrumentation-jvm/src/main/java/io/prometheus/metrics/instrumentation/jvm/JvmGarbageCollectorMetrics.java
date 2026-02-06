package io.prometheus.metrics.instrumentation.jvm;

import com.sun.management.GarbageCollectionNotificationInfo;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.core.metrics.SummaryWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.Unit;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.management.ListenerNotFoundException;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

/**
 * JVM Garbage Collector metrics. The {@link JvmGarbageCollectorMetrics} are registered as part of
 * the {@link JvmMetrics} like this:
 *
 * <pre>{@code
 * JvmMetrics.builder().register();
 * }</pre>
 *
 * <p>However, if you want only the {@link JvmGarbageCollectorMetrics} you can also register them
 * directly:
 *
 * <pre>{@code
 * JvmGarbageCollectorMetrics.builder().register();
 * }</pre>
 *
 * <p>Example metrics being exported:
 *
 * <pre>
 * # HELP jvm_gc_collection_seconds Time spent in a given JVM garbage collector in seconds.
 * # TYPE jvm_gc_collection_seconds summary
 * jvm_gc_collection_seconds_count{gc="PS MarkSweep"} 0
 * jvm_gc_collection_seconds_sum{gc="PS MarkSweep"} 0.0
 * jvm_gc_collection_seconds_count{gc="PS Scavenge"} 0
 * jvm_gc_collection_seconds_sum{gc="PS Scavenge"} 0.0
 * </pre>
 *
 * <p><b>Note on resource cleanup:</b> When using OpenTelemetry semantic conventions (via {@code
 * use_otel_semconv} configuration), this class registers JMX notification listeners that should be
 * cleaned up when the metrics are no longer needed. To ensure proper cleanup, keep a reference to
 * the {@link JvmGarbageCollectorMetrics} instance and call {@link #close()} when done:
 *
 * <pre>{@code
 * JvmGarbageCollectorMetrics gcMetrics = new JvmGarbageCollectorMetrics(...);
 * gcMetrics.register(registry);
 * // ... later, when shutting down:
 * gcMetrics.close();
 * }</pre>
 */
public class JvmGarbageCollectorMetrics implements AutoCloseable {
  private static final Logger logger = Logger.getLogger(JvmGarbageCollectorMetrics.class.getName());

  private static final String JVM_GC_COLLECTION_SECONDS = "jvm_gc_collection_seconds";
  private static final String JVM_GC_DURATION = "jvm.gc.duration";

  private final PrometheusProperties config;
  private final List<GarbageCollectorMXBean> garbageCollectorBeans;
  private final Labels constLabels;
  private final List<ListenerRegistration> listenerRegistrations = new ArrayList<>();

  private JvmGarbageCollectorMetrics(
      List<GarbageCollectorMXBean> garbageCollectorBeans,
      PrometheusProperties config,
      Labels constLabels) {
    this.config = config;
    this.garbageCollectorBeans = garbageCollectorBeans;
    this.constLabels = constLabels;
  }

  private void register(PrometheusRegistry registry) {
    if (config.useOtelSemconv(JVM_GC_DURATION)) {
      registerOtel(registry);
    } else {
      registerPrometheus(registry);
    }
  }

  private void registerPrometheus(PrometheusRegistry registry) {
    SummaryWithCallback.builder(config)
        .name(JVM_GC_COLLECTION_SECONDS)
        .help("Time spent in a given JVM garbage collector in seconds.")
        .unit(Unit.SECONDS)
        .labelNames("gc")
        .callback(
            callback -> {
              for (GarbageCollectorMXBean gc : garbageCollectorBeans) {
                callback.call(
                    gc.getCollectionCount(),
                    Unit.millisToSeconds(gc.getCollectionTime()),
                    Quantiles.EMPTY,
                    gc.getName());
              }
            })
        .constLabels(constLabels)
        .register(registry);
  }

  private void registerOtel(PrometheusRegistry registry) {
    double[] buckets = {0.01, 0.1, 1, 10};

    Histogram gcDurationHistogram =
        Histogram.builder(config)
            .name(JVM_GC_DURATION)
            .unit(Unit.SECONDS)
            .help("Duration of JVM garbage collection actions.")
            .labelNames("jvm.gc.action", "jvm.gc.name", "jvm.gc.cause")
            .classicUpperBounds(buckets)
            .register(registry);

    registerNotificationListener(gcDurationHistogram);
  }

  private void registerNotificationListener(Histogram gcDurationHistogram) {
    for (GarbageCollectorMXBean gcBean : garbageCollectorBeans) {

      if (!(gcBean instanceof NotificationEmitter)) {
        continue;
      }

      NotificationEmitter notificationEmitter = (NotificationEmitter) gcBean;

      // Create a named listener instance so we can remove it later
      NotificationListener listener =
          (notification, handback) -> {
            try {
              if (!GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION.equals(
                  notification.getType())) {
                return;
              }

              GarbageCollectionNotificationInfo info =
                  GarbageCollectionNotificationInfo.from(
                      (CompositeData) notification.getUserData());

              observe(gcDurationHistogram, info);
            } catch (Exception e) {
              // Must not propagate exceptions - would cause JVM to remove listener permanently
              logger.log(
                  Level.WARNING, "Exception while processing garbage collection notification", e);
            }
          };

      notificationEmitter.addNotificationListener(listener, null, null);

      // Store registration info for cleanup
      listenerRegistrations.add(new ListenerRegistration(notificationEmitter, listener));
    }
  }

  private void observe(Histogram gcDurationHistogram, GarbageCollectionNotificationInfo info) {
    double observedDuration = Unit.millisToSeconds(info.getGcInfo().getDuration());
    gcDurationHistogram
        .labelValues(info.getGcAction(), info.getGcName(), info.getGcCause())
        .observe(observedDuration);
  }

  /**
   * Removes all JMX notification listeners registered by this instance.
   *
   * <p>This method should be called when the metrics are no longer needed to prevent memory leaks.
   * It is safe to call this method multiple times.
   *
   * <p><b>Note:</b> This only affects metrics registered with OpenTelemetry semantic conventions
   * (when {@code use_otel_semconv} is enabled). The callback-based Prometheus metrics do not
   * require cleanup.
   */
  @Override
  public void close() {
    for (ListenerRegistration registration : listenerRegistrations) {
      try {
        registration.notificationEmitter.removeNotificationListener(registration.listener);
      } catch (ListenerNotFoundException e) {
        // Listener was already removed or never added - ignore
        logger.log(Level.FINE, "Listener not found during cleanup", e);
      } catch (Exception e) {
        // Log but continue removing other listeners
        logger.log(Level.WARNING, "Error removing GC notification listener", e);
      }
    }
    listenerRegistrations.clear();
  }

  /**
   * Holds registration information for a notification listener so it can be removed later.
   *
   * <p>Package-private for testing.
   */
  static class ListenerRegistration {
    final NotificationEmitter notificationEmitter;
    final NotificationListener listener;

    ListenerRegistration(NotificationEmitter notificationEmitter, NotificationListener listener) {
      this.notificationEmitter = notificationEmitter;
      this.listener = listener;
    }
  }

  public static Builder builder() {
    return new Builder(PrometheusProperties.get());
  }

  public static Builder builder(PrometheusProperties config) {
    return new Builder(config);
  }

  public static class Builder {

    private final PrometheusProperties config;
    @Nullable private List<GarbageCollectorMXBean> garbageCollectorBeans;
    private Labels constLabels = Labels.EMPTY;

    private Builder(PrometheusProperties config) {
      this.config = config;
    }

    public Builder constLabels(Labels constLabels) {
      this.constLabels = constLabels;
      return this;
    }

    /** Package private. For testing only. */
    Builder garbageCollectorBeans(List<GarbageCollectorMXBean> garbageCollectorBeans) {
      this.garbageCollectorBeans = garbageCollectorBeans;
      return this;
    }

    /**
     * Register GC metrics with the default registry.
     *
     * <p><b>Important:</b> When using OpenTelemetry semantic conventions, this method returns a
     * {@link JvmGarbageCollectorMetrics} instance that implements {@link AutoCloseable}. Keep a
     * reference and call {@link JvmGarbageCollectorMetrics#close()} when shutting down to prevent
     * memory leaks:
     *
     * <pre>{@code
     * JvmGarbageCollectorMetrics gcMetrics = JvmGarbageCollectorMetrics.builder().register();
     * // ... later during shutdown:
     * gcMetrics.close();
     * }</pre>
     *
     * @return the registered metrics instance, which should be closed when no longer needed
     */
    public JvmGarbageCollectorMetrics register() {
      return register(PrometheusRegistry.defaultRegistry);
    }

    /**
     * Register GC metrics with the specified registry.
     *
     * <p><b>Important:</b> When using OpenTelemetry semantic conventions, this method returns a
     * {@link JvmGarbageCollectorMetrics} instance that implements {@link AutoCloseable}. Keep a
     * reference and call {@link JvmGarbageCollectorMetrics#close()} when shutting down to prevent
     * memory leaks.
     *
     * @param registry the registry to register metrics with
     * @return the registered metrics instance, which should be closed when no longer needed
     */
    public JvmGarbageCollectorMetrics register(PrometheusRegistry registry) {
      List<GarbageCollectorMXBean> garbageCollectorBeans = this.garbageCollectorBeans;
      if (garbageCollectorBeans == null) {
        garbageCollectorBeans = ManagementFactory.getGarbageCollectorMXBeans();
      }
      JvmGarbageCollectorMetrics metrics =
          new JvmGarbageCollectorMetrics(garbageCollectorBeans, config, constLabels);
      metrics.register(registry);
      return metrics;
    }
  }
}
