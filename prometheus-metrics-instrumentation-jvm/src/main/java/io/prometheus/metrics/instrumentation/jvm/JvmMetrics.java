package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Registers all JVM metrics. Example usage:
 * <pre>{@code
 *   JvmMetrics.newBuilder().register();
 * }</pre>
 */
public class JvmMetrics {

    private static AtomicBoolean registeredWithTheDefaultRegistry = new AtomicBoolean(false);

    public static Builder newBuilder() {
        return new Builder(PrometheusProperties.get());
    }

    // Note: Currently there is no configuration for JVM metrics, so it doesn't matter whether you pass a config or not.
    // However, we will add config options in the future, like whether you want to use Prometheus naming conventions
    //'or OpenTelemetry semantic conventions for JVM metrics.
    public static Builder newBuilder(PrometheusProperties config) {
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
            JvmThreadsMetrics.newBuilder(config).register(registry);
            JvmBufferPoolMetrics.newBuilder(config).register(registry);
            JvmClassLoadingMetrics.newBuilder(config).register(registry);
            JvmCompilationMetrics.newBuilder(config).register(registry);
            JvmGarbageCollectorMetrics.newBuilder(config).register(registry);
            JvmMemoryPoolAllocationMetrics.newBuilder(config).register(registry);
            JvmMemoryMetrics.newBuilder(config).register(registry);
            JvmRuntimeInfoMetric.newBuilder(config).register(registry);
            ProcessMetrics.newBuilder(config).register(registry);
        }
    }
}
