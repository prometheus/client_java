package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * JVM memory metrics. The {@link JvmMemoryMetrics} are registered as part of the {@link JvmMetrics} like this:
 * <pre>{@code
 *   JvmMetrics.builder().register();
 * }</pre>
 * However, if you want only the {@link JvmMemoryMetrics} you can also register them directly:
 * <pre>{@code
 *   JvmMemoryMetrics.builder().register();
 * }</pre>
 * Example metrics being exported:
 * <pre>
 * # HELP jvm_memory_committed_bytes Committed (bytes) of a given JVM memory area.
 * # TYPE jvm_memory_committed_bytes gauge
 * jvm_memory_committed_bytes{area="heap"} 4.98597888E8
 * jvm_memory_committed_bytes{area="nonheap"} 1.1993088E7
 * # HELP jvm_memory_init_bytes Initial bytes of a given JVM memory area.
 * # TYPE jvm_memory_init_bytes gauge
 * jvm_memory_init_bytes{area="heap"} 5.20093696E8
 * jvm_memory_init_bytes{area="nonheap"} 2555904.0
 * # HELP jvm_memory_max_bytes Max (bytes) of a given JVM memory area.
 * # TYPE jvm_memory_max_bytes gauge
 * jvm_memory_max_bytes{area="heap"} 7.38983936E9
 * jvm_memory_max_bytes{area="nonheap"} -1.0
 * # HELP jvm_memory_objects_pending_finalization The number of objects waiting in the finalizer queue.
 * # TYPE jvm_memory_objects_pending_finalization gauge
 * jvm_memory_objects_pending_finalization 0.0
 * # HELP jvm_memory_pool_collection_committed_bytes Committed after last collection bytes of a given JVM memory pool.
 * # TYPE jvm_memory_pool_collection_committed_bytes gauge
 * jvm_memory_pool_collection_committed_bytes{pool="PS Eden Space"} 1.30023424E8
 * jvm_memory_pool_collection_committed_bytes{pool="PS Old Gen"} 3.47078656E8
 * jvm_memory_pool_collection_committed_bytes{pool="PS Survivor Space"} 2.1495808E7
 * # HELP jvm_memory_pool_collection_init_bytes Initial after last collection bytes of a given JVM memory pool.
 * # TYPE jvm_memory_pool_collection_init_bytes gauge
 * jvm_memory_pool_collection_init_bytes{pool="PS Eden Space"} 1.30023424E8
 * jvm_memory_pool_collection_init_bytes{pool="PS Old Gen"} 3.47078656E8
 * jvm_memory_pool_collection_init_bytes{pool="PS Survivor Space"} 2.1495808E7
 * # HELP jvm_memory_pool_collection_max_bytes Max bytes after last collection of a given JVM memory pool.
 * # TYPE jvm_memory_pool_collection_max_bytes gauge
 * jvm_memory_pool_collection_max_bytes{pool="PS Eden Space"} 2.727870464E9
 * jvm_memory_pool_collection_max_bytes{pool="PS Old Gen"} 5.542248448E9
 * jvm_memory_pool_collection_max_bytes{pool="PS Survivor Space"} 2.1495808E7
 * # HELP jvm_memory_pool_collection_used_bytes Used bytes after last collection of a given JVM memory pool.
 * # TYPE jvm_memory_pool_collection_used_bytes gauge
 * jvm_memory_pool_collection_used_bytes{pool="PS Eden Space"} 0.0
 * jvm_memory_pool_collection_used_bytes{pool="PS Old Gen"} 1249696.0
 * jvm_memory_pool_collection_used_bytes{pool="PS Survivor Space"} 0.0
 * # HELP jvm_memory_pool_committed_bytes Committed bytes of a given JVM memory pool.
 * # TYPE jvm_memory_pool_committed_bytes gauge
 * jvm_memory_pool_committed_bytes{pool="Code Cache"} 4128768.0
 * jvm_memory_pool_committed_bytes{pool="Compressed Class Space"} 917504.0
 * jvm_memory_pool_committed_bytes{pool="Metaspace"} 6946816.0
 * jvm_memory_pool_committed_bytes{pool="PS Eden Space"} 1.30023424E8
 * jvm_memory_pool_committed_bytes{pool="PS Old Gen"} 3.47078656E8
 * jvm_memory_pool_committed_bytes{pool="PS Survivor Space"} 2.1495808E7
 * # HELP jvm_memory_pool_init_bytes Initial bytes of a given JVM memory pool.
 * # TYPE jvm_memory_pool_init_bytes gauge
 * jvm_memory_pool_init_bytes{pool="Code Cache"} 2555904.0
 * jvm_memory_pool_init_bytes{pool="Compressed Class Space"} 0.0
 * jvm_memory_pool_init_bytes{pool="Metaspace"} 0.0
 * jvm_memory_pool_init_bytes{pool="PS Eden Space"} 1.30023424E8
 * jvm_memory_pool_init_bytes{pool="PS Old Gen"} 3.47078656E8
 * jvm_memory_pool_init_bytes{pool="PS Survivor Space"} 2.1495808E7
 * # HELP jvm_memory_pool_max_bytes Max bytes of a given JVM memory pool.
 * # TYPE jvm_memory_pool_max_bytes gauge
 * jvm_memory_pool_max_bytes{pool="Code Cache"} 2.5165824E8
 * jvm_memory_pool_max_bytes{pool="Compressed Class Space"} 1.073741824E9
 * jvm_memory_pool_max_bytes{pool="Metaspace"} -1.0
 * jvm_memory_pool_max_bytes{pool="PS Eden Space"} 2.727870464E9
 * jvm_memory_pool_max_bytes{pool="PS Old Gen"} 5.542248448E9
 * jvm_memory_pool_max_bytes{pool="PS Survivor Space"} 2.1495808E7
 * # HELP jvm_memory_pool_used_bytes Used bytes of a given JVM memory pool.
 * # TYPE jvm_memory_pool_used_bytes gauge
 * jvm_memory_pool_used_bytes{pool="Code Cache"} 4065472.0
 * jvm_memory_pool_used_bytes{pool="Compressed Class Space"} 766680.0
 * jvm_memory_pool_used_bytes{pool="Metaspace"} 6659432.0
 * jvm_memory_pool_used_bytes{pool="PS Eden Space"} 7801536.0
 * jvm_memory_pool_used_bytes{pool="PS Old Gen"} 1249696.0
 * jvm_memory_pool_used_bytes{pool="PS Survivor Space"} 0.0
 * # HELP jvm_memory_used_bytes Used bytes of a given JVM memory area.
 * # TYPE jvm_memory_used_bytes gauge
 * jvm_memory_used_bytes{area="heap"} 9051232.0
 * jvm_memory_used_bytes{area="nonheap"} 1.1490688E7
 * </pre>
 */
public class JvmMemoryMetrics {

    private static final String JVM_MEMORY_OBJECTS_PENDING_FINALIZATION = "jvm_memory_objects_pending_finalization";
    private static final String JVM_MEMORY_USED_BYTES = "jvm_memory_used_bytes";
    private static final String JVM_MEMORY_COMMITTED_BYTES = "jvm_memory_committed_bytes";
    private static final String JVM_MEMORY_MAX_BYTES = "jvm_memory_max_bytes";
    private static final String JVM_MEMORY_INIT_BYTES = "jvm_memory_init_bytes";
    private static final String JVM_MEMORY_POOL_USED_BYTES = "jvm_memory_pool_used_bytes";
    private static final String JVM_MEMORY_POOL_COMMITTED_BYTES = "jvm_memory_pool_committed_bytes";
    private static final String JVM_MEMORY_POOL_MAX_BYTES = "jvm_memory_pool_max_bytes";
    private static final String JVM_MEMORY_POOL_INIT_BYTES = "jvm_memory_pool_init_bytes";
    private static final String JVM_MEMORY_POOL_COLLECTION_USED_BYTES = "jvm_memory_pool_collection_used_bytes";
    private static final String JVM_MEMORY_POOL_COLLECTION_COMMITTED_BYTES = "jvm_memory_pool_collection_committed_bytes";
    private static final String JVM_MEMORY_POOL_COLLECTION_MAX_BYTES = "jvm_memory_pool_collection_max_bytes";
    private static final String JVM_MEMORY_POOL_COLLECTION_INIT_BYTES = "jvm_memory_pool_collection_init_bytes";

    private final PrometheusProperties config;
    private final MemoryMXBean memoryBean;
    private final List<MemoryPoolMXBean> poolBeans;

    private JvmMemoryMetrics(List<MemoryPoolMXBean> poolBeans, MemoryMXBean memoryBean, PrometheusProperties config) {
        this.config = config;
        this.poolBeans = poolBeans;
        this.memoryBean = memoryBean;
    }

    private void register(PrometheusRegistry registry) {

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_OBJECTS_PENDING_FINALIZATION)
                .help("The number of objects waiting in the finalizer queue.")
                .callback(callback -> callback.call(memoryBean.getObjectPendingFinalizationCount()))
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_USED_BYTES)
                .help("Used bytes of a given JVM memory area.")
                .unit(Unit.BYTES)
                .labelNames("area")
                .callback(callback -> {
                    callback.call(memoryBean.getHeapMemoryUsage().getUsed(), "heap");
                    callback.call(memoryBean.getNonHeapMemoryUsage().getUsed(), "nonheap");
                })
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_COMMITTED_BYTES)
                .help("Committed (bytes) of a given JVM memory area.")
                .unit(Unit.BYTES)
                .labelNames("area")
                .callback(callback -> {
                    callback.call(memoryBean.getHeapMemoryUsage().getCommitted(), "heap");
                    callback.call(memoryBean.getNonHeapMemoryUsage().getCommitted(), "nonheap");
                })
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_MAX_BYTES)
                .help("Max (bytes) of a given JVM memory area.")
                .unit(Unit.BYTES)
                .labelNames("area")
                .callback(callback -> {
                    callback.call(memoryBean.getHeapMemoryUsage().getMax(), "heap");
                    callback.call(memoryBean.getNonHeapMemoryUsage().getMax(), "nonheap");
                })
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_INIT_BYTES)
                .help("Initial bytes of a given JVM memory area.")
                .unit(Unit.BYTES)
                .labelNames("area")
                .callback(callback -> {
                    callback.call(memoryBean.getHeapMemoryUsage().getInit(), "heap");
                    callback.call(memoryBean.getNonHeapMemoryUsage().getInit(), "nonheap");
                })
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_POOL_USED_BYTES)
                .help("Used bytes of a given JVM memory pool.")
                .unit(Unit.BYTES)
                .labelNames("pool")
                .callback(makeCallback(poolBeans, MemoryPoolMXBean::getUsage, MemoryUsage::getUsed))
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_POOL_COMMITTED_BYTES)
                .help("Committed bytes of a given JVM memory pool.")
                .unit(Unit.BYTES)
                .labelNames("pool")
                .callback(makeCallback(poolBeans, MemoryPoolMXBean::getUsage, MemoryUsage::getCommitted))
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_POOL_MAX_BYTES)
                .help("Max bytes of a given JVM memory pool.")
                .unit(Unit.BYTES)
                .labelNames("pool")
                .callback(makeCallback(poolBeans, MemoryPoolMXBean::getUsage, MemoryUsage::getMax))
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_POOL_INIT_BYTES)
                .help("Initial bytes of a given JVM memory pool.")
                .unit(Unit.BYTES)
                .labelNames("pool")
                .callback(makeCallback(poolBeans, MemoryPoolMXBean::getUsage, MemoryUsage::getInit))
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_POOL_COLLECTION_USED_BYTES)
                .help("Used bytes after last collection of a given JVM memory pool.")
                .unit(Unit.BYTES)
                .labelNames("pool")
                .callback(makeCallback(poolBeans, MemoryPoolMXBean::getCollectionUsage, MemoryUsage::getUsed))
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_POOL_COLLECTION_COMMITTED_BYTES)
                .help("Committed after last collection bytes of a given JVM memory pool.")
                .unit(Unit.BYTES)
                .labelNames("pool")
                .callback(makeCallback(poolBeans, MemoryPoolMXBean::getCollectionUsage, MemoryUsage::getCommitted))
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_POOL_COLLECTION_MAX_BYTES)
                .help("Max bytes after last collection of a given JVM memory pool.")
                .unit(Unit.BYTES)
                .labelNames("pool")
                .callback(makeCallback(poolBeans, MemoryPoolMXBean::getCollectionUsage, MemoryUsage::getMax))
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(JVM_MEMORY_POOL_COLLECTION_INIT_BYTES)
                .help("Initial after last collection bytes of a given JVM memory pool.")
                .unit(Unit.BYTES)
                .labelNames("pool")
                .callback(makeCallback(poolBeans, MemoryPoolMXBean::getCollectionUsage, MemoryUsage::getInit))
                .register(registry);
    }

    private Consumer<GaugeWithCallback.Callback> makeCallback(List<MemoryPoolMXBean> poolBeans, Function<MemoryPoolMXBean, MemoryUsage> memoryUsageFunc, Function<MemoryUsage, Long> valueFunc) {
        return callback -> {
            for (MemoryPoolMXBean pool : poolBeans) {
                MemoryUsage poolUsage = memoryUsageFunc.apply(pool);
                if (poolUsage != null) {
                    callback.call(valueFunc.apply(poolUsage), pool.getName());
                }
            }
        };
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder {

        private final PrometheusProperties config;
        private MemoryMXBean memoryBean;
        private List<MemoryPoolMXBean> poolBeans;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        /**
         * Package private. For testing only.
         */
        Builder withMemoryBean(MemoryMXBean memoryBean) {
            this.memoryBean = memoryBean;
            return this;
        }

        /**
         * Package private. For testing only.
         */
        Builder withMemoryPoolBeans(List<MemoryPoolMXBean> memoryPoolBeans) {
            this.poolBeans = memoryPoolBeans;
            return this;
        }

        public void register() {
            register(PrometheusRegistry.defaultRegistry);
        }

        public void register(PrometheusRegistry registry) {
            MemoryMXBean memoryMXBean = this.memoryBean != null ? this.memoryBean : ManagementFactory.getMemoryMXBean();
            List<MemoryPoolMXBean> poolBeans = this.poolBeans != null ? this.poolBeans : ManagementFactory.getMemoryPoolMXBeans();
            new JvmMemoryMetrics(poolBeans, memoryMXBean, config).register(registry);
        }
    }
}
