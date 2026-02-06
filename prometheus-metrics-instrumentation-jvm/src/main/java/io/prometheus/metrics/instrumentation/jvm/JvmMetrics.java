package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Labels;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registers all JVM metrics. Example usage:
 *
 * <pre>{@code
 * JvmMetrics jvmMetrics = JvmMetrics.builder().register();
 * // ... later, during shutdown:
 * jvmMetrics.close();
 * }</pre>
 *
 * <p><b>Note on resource cleanup:</b> When using OpenTelemetry semantic conventions for GC metrics
 * (via {@code use_otel_semconv} configuration), JMX notification listeners are registered that
 * should be cleaned up when shutting down. Call {@link #close()} to remove these listeners and
 * prevent memory leaks.
 */
public class JvmMetrics implements AutoCloseable {

  private static final Set<PrometheusRegistry> REGISTERED = ConcurrentHashMap.newKeySet();

  private final List<AutoCloseable> closeables = new ArrayList<>();

  public static Builder builder() {
    return new Builder(PrometheusProperties.get());
  }

  public static Builder builder(PrometheusProperties config) {
    return new Builder(config);
  }

  /**
   * Closes all registered metrics that require cleanup.
   *
   * <p>This removes JMX notification listeners registered by GC metrics and allocation metrics when
   * using OpenTelemetry semantic conventions. It is safe to call this method multiple times.
   */
  @Override
  public void close() {
    for (AutoCloseable closeable : closeables) {
      try {
        closeable.close();
      } catch (Exception e) {
        // Continue closing other resources even if one fails
      }
    }
    closeables.clear();
  }

  public static class Builder {

    private final PrometheusProperties config;
    private Labels constLabels = Labels.EMPTY;

    private Builder(PrometheusProperties config) {
      this.config = config;
    }

    /** Set constant labels that will be applied to all JVM metrics registered by this builder. */
    public Builder constLabels(Labels constLabels) {
      this.constLabels = constLabels;
      return this;
    }

    /**
     * Register all JVM metrics with the default registry.
     *
     * <p>It's safe to call this multiple times, only the first call will register the metrics, all
     * subsequent calls will be ignored.
     *
     * <p><b>Important:</b> Keep a reference to the returned {@link JvmMetrics} instance and call
     * {@link JvmMetrics#close()} during shutdown to clean up JMX notification listeners:
     *
     * <pre>{@code
     * JvmMetrics jvmMetrics = JvmMetrics.builder().register();
     * // ... later during shutdown:
     * jvmMetrics.close();
     * }</pre>
     *
     * @return the JvmMetrics instance, which should be closed when no longer needed
     */
    public JvmMetrics register() {
      return register(PrometheusRegistry.defaultRegistry);
    }

    /**
     * Register all JVM metrics with the {@code registry}.
     *
     * <p>It's safe to call this multiple times, only the first call will register the metrics, all
     * subsequent calls will be ignored.
     *
     * <p><b>Important:</b> Keep a reference to the returned {@link JvmMetrics} instance and call
     * {@link JvmMetrics#close()} during shutdown to clean up JMX notification listeners.
     *
     * @param registry the registry to register metrics with
     * @return the JvmMetrics instance, which should be closed when no longer needed
     */
    public JvmMetrics register(PrometheusRegistry registry) {
      JvmMetrics jvmMetrics = new JvmMetrics();
      if (REGISTERED.add(registry)) {
        JvmThreadsMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmBufferPoolMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmClassLoadingMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmCompilationMetrics.builder(config).constLabels(constLabels).register(registry);

        // Store closeable metrics for cleanup
        JvmGarbageCollectorMetrics gcMetrics =
            JvmGarbageCollectorMetrics.builder(config).constLabels(constLabels).register(registry);
        jvmMetrics.closeables.add(gcMetrics);

        // Note: JvmMemoryPoolAllocationMetrics also uses notification listeners but doesn't
        // currently implement cleanup. This should be fixed in a future update.
        JvmMemoryPoolAllocationMetrics.builder(config).constLabels(constLabels).register(registry);

        JvmMemoryMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmNativeMemoryMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmRuntimeInfoMetric.builder(config).constLabels(constLabels).register(registry);
        ProcessMetrics.builder(config).constLabels(constLabels).register(registry);
      }
      return jvmMetrics;
    }
  }
}
