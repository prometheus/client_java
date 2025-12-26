package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Labels;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registers all JVM metrics. Example usage:
 *
 * <pre>{@code
 * JvmMetrics.builder().register();
 * }</pre>
 */
public class JvmMetrics {

  private static final Set<PrometheusRegistry> REGISTERED = ConcurrentHashMap.newKeySet();

  public static Builder builder() {
    return new Builder(PrometheusProperties.get());
  }

  // Note: Currently there is no configuration for JVM metrics, so it doesn't matter whether you
  // pass a config or not.
  // However, we will add config options in the future, like whether you want to use Prometheus
  // naming conventions
  // or OpenTelemetry semantic conventions for JVM metrics.
  public static Builder builder(PrometheusProperties config) {
    return new Builder(config);
  }

  public static class Builder {

  private final PrometheusProperties config;
  private Labels constLabels = Labels.EMPTY;

    private Builder(PrometheusProperties config) {
      this.config = config;
    }

    /** Set constant labels that will be applied to all JVM metrics registered by this builder. */
    public Builder constLabels(Labels constLabels) {
      this.constLabels = constLabels == null ? Labels.EMPTY : constLabels;
      return this;
    }

    /**
     * Register all JVM metrics with the default registry.
     *
     * <p>It's safe to call this multiple times, only the first call will register the metrics, all
     * subsequent calls will be ignored.
     */
    public void register() {
      register(PrometheusRegistry.defaultRegistry);
    }

    /**
     * Register all JVM metrics with the {@code registry}.
     *
     * <p>It's safe to call this multiple times, only the first call will register the metrics, all
     * subsequent calls will be ignored.
     */
    public void register(PrometheusRegistry registry) {
      if (REGISTERED.add(registry)) {
        JvmThreadsMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmBufferPoolMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmClassLoadingMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmCompilationMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmGarbageCollectorMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmMemoryPoolAllocationMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmMemoryMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmNativeMemoryMetrics.builder(config).constLabels(constLabels).register(registry);
        JvmRuntimeInfoMetric.builder(config).constLabels(constLabels).register(registry);
        ProcessMetrics.builder(config).constLabels(constLabels).register(registry);
      }
    }
  }
}
