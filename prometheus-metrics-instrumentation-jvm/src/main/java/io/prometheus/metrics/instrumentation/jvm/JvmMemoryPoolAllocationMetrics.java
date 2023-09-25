package io.prometheus.metrics.instrumentation.jvm;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JVM memory allocation metrics. The {@link JvmMemoryPoolAllocationMetrics} are registered as part of the {@link JvmMetrics} like this:
 * <pre>{@code
 *   JvmMetrics.builder().register();
 * }</pre>
 * However, if you want only the {@link JvmMemoryPoolAllocationMetrics} you can also register them directly:
 * <pre>{@code
 *   JvmMemoryAllocationMetrics.builder().register();
 * }</pre>
 * Example metrics being exported:
 * <pre>
 * # HELP jvm_memory_pool_allocated_bytes_total Total bytes allocated in a given JVM memory pool. Only updated after GC, not continuously.
 * # TYPE jvm_memory_pool_allocated_bytes_total counter
 * jvm_memory_pool_allocated_bytes_total{pool="Code Cache"} 4336448.0
 * jvm_memory_pool_allocated_bytes_total{pool="Compressed Class Space"} 875016.0
 * jvm_memory_pool_allocated_bytes_total{pool="Metaspace"} 7480456.0
 * jvm_memory_pool_allocated_bytes_total{pool="PS Eden Space"} 1.79232824E8
 * jvm_memory_pool_allocated_bytes_total{pool="PS Old Gen"} 1428888.0
 * jvm_memory_pool_allocated_bytes_total{pool="PS Survivor Space"} 4115280.0
 * </pre>
 */
public class JvmMemoryPoolAllocationMetrics {

    private static final String JVM_MEMORY_POOL_ALLOCATED_BYTES_TOTAL = "jvm_memory_pool_allocated_bytes_total";

    private final PrometheusProperties config;
    private final List<GarbageCollectorMXBean> garbageCollectorBeans;

    private JvmMemoryPoolAllocationMetrics(List<GarbageCollectorMXBean> garbageCollectorBeans, PrometheusProperties config) {
        this.garbageCollectorBeans = garbageCollectorBeans;
        this.config = config;
    }

    private void register(PrometheusRegistry registry) {

        Counter allocatedCounter = Counter.builder()
                .name(JVM_MEMORY_POOL_ALLOCATED_BYTES_TOTAL)
                .help("Total bytes allocated in a given JVM memory pool. Only updated after GC, not continuously.")
                .labelNames("pool")
                .register(registry);

        AllocationCountingNotificationListener listener = new AllocationCountingNotificationListener(allocatedCounter);
        for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorBeans) {
            if (garbageCollectorMXBean instanceof NotificationEmitter) {
                ((NotificationEmitter) garbageCollectorMXBean).addNotificationListener(listener, null, null);
            }
        }
    }

    static class AllocationCountingNotificationListener implements NotificationListener {

        private final Map<String, Long> lastMemoryUsage = new HashMap<String, Long>();
        private final Counter counter;

        AllocationCountingNotificationListener(Counter counter) {
            this.counter = counter;
        }

        @Override
        public synchronized void handleNotification(Notification notification, Object handback) {
            GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
            GcInfo gcInfo = info.getGcInfo();
            Map<String, MemoryUsage> memoryUsageBeforeGc = gcInfo.getMemoryUsageBeforeGc();
            Map<String, MemoryUsage> memoryUsageAfterGc = gcInfo.getMemoryUsageAfterGc();
            for (Map.Entry<String, MemoryUsage> entry : memoryUsageBeforeGc.entrySet()) {
                String memoryPool = entry.getKey();
                long before = entry.getValue().getUsed();
                long after = memoryUsageAfterGc.get(memoryPool).getUsed();
                handleMemoryPool(memoryPool, before, after);
            }
        }

        // Visible for testing
        void handleMemoryPool(String memoryPool, long before, long after) {
            /*
             * Calculate increase in the memory pool by comparing memory used
             * after last GC, before this GC, and after this GC.
             * See ascii illustration below.
             * Make sure to count only increases and ignore decreases.
             * (Typically a pool will only increase between GCs or during GCs, not both.
             * E.g. eden pools between GCs. Survivor and old generation pools during GCs.)
             *
             *                         |<-- diff1 -->|<-- diff2 -->|
             * Timeline: |-- last GC --|             |---- GC -----|
             *                      ___^__        ___^____      ___^___
             * Mem. usage vars:    / last \      / before \    / after \
             */

            // Get last memory usage after GC and remember memory used after for next time
            long last = getAndSet(lastMemoryUsage, memoryPool, after);
            // Difference since last GC
            long diff1 = before - last;
            // Difference during this GC
            long diff2 = after - before;
            // Make sure to only count increases
            if (diff1 < 0) {
                diff1 = 0;
            }
            if (diff2 < 0) {
                diff2 = 0;
            }
            long increase = diff1 + diff2;
            if (increase > 0) {
                counter.labelValues(memoryPool).inc(increase);
            }
        }

        private static long getAndSet(Map<String, Long> map, String key, long value) {
            Long last = map.put(key, value);
            return last == null ? 0 : last;
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
        private List<GarbageCollectorMXBean> garbageCollectorBeans;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        /**
         * Package private. For testing only.
         */
        Builder withGarbageCollectorBeans(List<GarbageCollectorMXBean> garbageCollectorBeans) {
            this.garbageCollectorBeans = garbageCollectorBeans;
            return this;
        }

        public void register() {
            register(PrometheusRegistry.defaultRegistry);
        }

        public void register(PrometheusRegistry registry) {
            List<GarbageCollectorMXBean> garbageCollectorBeans = this.garbageCollectorBeans;
            if (garbageCollectorBeans == null) {
                garbageCollectorBeans = ManagementFactory.getGarbageCollectorMXBeans();
            }
            new JvmMemoryPoolAllocationMetrics(garbageCollectorBeans, config).register(registry);
        }
    }
}
