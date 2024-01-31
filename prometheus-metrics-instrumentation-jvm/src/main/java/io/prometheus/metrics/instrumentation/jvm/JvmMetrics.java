package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Registers all JVM metrics. Example usage:
 * <pre>{@code
 *   JvmMetrics.builder().register();
 * }</pre>
 */
public class JvmMetrics {

    private static AtomicBoolean registeredWithTheDefaultRegistry = new AtomicBoolean(false);

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    // Note: Currently there is no configuration for JVM metrics, so it doesn't matter whether you pass a config or not.
    // However, we will add config options in the future, like whether you want to use Prometheus naming conventions
    //'or OpenTelemetry semantic conventions for JVM metrics.
    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder {

        private final PrometheusProperties config;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        /**
         * Register all JVM metrics with the default registry.
         * <p>
         * It's safe to call this multiple times:
         * Only the first call will register the metrics, all subsequent calls will be ignored.
         */
        public void register() {
            if (!registeredWithTheDefaultRegistry.getAndSet(true)) {
                register(PrometheusRegistry.defaultRegistry);
            }
        }

        /**
         * Register all JVM metrics with the {@code registry}.
         * <p>
         * You must make sure to call this only once per {@code registry}, otherwise it will
         * throw an Exception because you are trying to register duplicate metrics.
         */
        public void register(PrometheusRegistry registry) {
            JvmThreadsMetrics.builder(config).register(registry);
            JvmBufferPoolMetrics.builder(config).register(registry);
            JvmClassLoadingMetrics.builder(config).register(registry);
            JvmCompilationMetrics.builder(config).register(registry);
            JvmGarbageCollectorMetrics.builder(config).register(registry);
            JvmMemoryPoolAllocationMetrics.builder(config).register(registry);
            JvmMemoryMetrics.builder(config).register(registry);
            JvmNativeMemoryMetrics.builder(config).register(registry);
            JvmRuntimeInfoMetric.builder(config).register(registry);
            ProcessMetrics.builder(config).register(registry);
        }
    }
}
