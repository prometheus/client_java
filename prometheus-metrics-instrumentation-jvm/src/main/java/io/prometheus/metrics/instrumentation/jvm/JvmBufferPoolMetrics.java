package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import io.prometheus.metrics.model.registry.Collector;
import io.prometheus.metrics.model.registry.CollectorBuilder;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * JVM Buffer Pool metrics. The {@link JvmBufferPoolMetrics} are registered as part of the {@link JvmMetrics} like this:
 * <pre>{@code
 *   JvmMetrics.builder().register();
 * }</pre>
 * However, if you want only the {@link JvmBufferPoolMetrics} you can also register them directly:
 * <pre>{@code
 *   JvmBufferPoolMetrics.builder().register();
 * }</pre>
 * Example metrics being exported:
 * <pre>
 * # HELP jvm_buffer_pool_capacity_bytes Bytes capacity of a given JVM buffer pool.
 * # TYPE jvm_buffer_pool_capacity_bytes gauge
 * jvm_buffer_pool_capacity_bytes{pool="direct"} 8192.0
 * jvm_buffer_pool_capacity_bytes{pool="mapped"} 0.0
 * # HELP jvm_buffer_pool_used_buffers Used buffers of a given JVM buffer pool.
 * # TYPE jvm_buffer_pool_used_buffers gauge
 * jvm_buffer_pool_used_buffers{pool="direct"} 1.0
 * jvm_buffer_pool_used_buffers{pool="mapped"} 0.0
 * # HELP jvm_buffer_pool_used_bytes Used bytes of a given JVM buffer pool.
 * # TYPE jvm_buffer_pool_used_bytes gauge
 * jvm_buffer_pool_used_bytes{pool="direct"} 8192.0
 * jvm_buffer_pool_used_bytes{pool="mapped"} 0.0
 * </pre>
 */
public class JvmBufferPoolMetrics {

    private static final String JVM_BUFFER_POOL_USED_BYTES = "jvm_buffer_pool_used_bytes";
    private static final String JVM_BUFFER_POOL_CAPACITY_BYTES = "jvm_buffer_pool_capacity_bytes";
    private static final String JVM_BUFFER_POOL_USED_BUFFERS = "jvm_buffer_pool_used_buffers";

    private static Collector build(List<BufferPoolMXBean> bufferPoolBeans, PrometheusProperties config) {
        var builder = CollectorBuilder.compositeBuilder();

        builder.add(GaugeWithCallback.builder(config)
                .name(JVM_BUFFER_POOL_USED_BYTES)
                .help("Used bytes of a given JVM buffer pool.")
                .unit(Unit.BYTES)
                .labelNames("pool")
                .callback(callback -> {
                    for (BufferPoolMXBean pool : bufferPoolBeans) {
                        callback.call(pool.getMemoryUsed(), pool.getName());
                    }
                })
                .build());

        builder.add(GaugeWithCallback.builder(config)
                .name(JVM_BUFFER_POOL_CAPACITY_BYTES)
                .help("Bytes capacity of a given JVM buffer pool.")
                .unit(Unit.BYTES)
                .labelNames("pool")
                .callback(callback -> {
                    for (BufferPoolMXBean pool : bufferPoolBeans) {
                        callback.call(pool.getTotalCapacity(), pool.getName());
                    }
                })
                .build());

        builder.add(GaugeWithCallback.builder(config)
                .name(JVM_BUFFER_POOL_USED_BUFFERS)
                .help("Used buffers of a given JVM buffer pool.")
                .labelNames("pool")
                .callback(callback -> {
                    for (BufferPoolMXBean pool : bufferPoolBeans) {
                        callback.call(pool.getCount(), pool.getName());
                    }
                })
                .build());

        return builder.build();
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder {

        private final PrometheusProperties config;
        private List<BufferPoolMXBean> bufferPoolBeans;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        /**
         * Package private. For testing only.
         */
        Builder bufferPoolBeans(List<BufferPoolMXBean> bufferPoolBeans) {
            this.bufferPoolBeans = bufferPoolBeans;
            return this;
        }

        public Collector build() {
            return JvmBufferPoolMetrics.build(bufferPoolBeans, config);
        }

        public void register() {
            PrometheusRegistry.defaultRegistry.register(build());
        }

        public void register(PrometheusRegistry registry) {
            registry.register(build());
        }
    }
}
