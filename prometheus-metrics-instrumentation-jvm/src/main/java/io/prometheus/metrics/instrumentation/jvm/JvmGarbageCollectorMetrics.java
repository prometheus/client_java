package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.SummaryWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Quantiles;
import io.prometheus.metrics.model.snapshots.Unit;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * JVM Garbage Collector metrics. The {@link JvmGarbageCollectorMetrics} are registered as part of the {@link JvmMetrics} like this:
 * <pre>{@code
 *   JvmMetrics.builder().register();
 * }</pre>
 * However, if you want only the {@link JvmGarbageCollectorMetrics} you can also register them directly:
 * <pre>{@code
 *   JvmGarbageCollectorMetrics.builder().register();
 * }</pre>
 * Example metrics being exported:
 * <pre>
 * # HELP jvm_gc_collection_seconds Time spent in a given JVM garbage collector in seconds.
 * # TYPE jvm_gc_collection_seconds summary
 * jvm_gc_collection_seconds_count{gc="PS MarkSweep"} 0
 * jvm_gc_collection_seconds_sum{gc="PS MarkSweep"} 0.0
 * jvm_gc_collection_seconds_count{gc="PS Scavenge"} 0
 * jvm_gc_collection_seconds_sum{gc="PS Scavenge"} 0.0
 * </pre>
 */
public class JvmGarbageCollectorMetrics {

    private static final String JVM_GC_COLLECTION_SECONDS = "jvm_gc_collection_seconds";

    private final PrometheusProperties config;
    private final List<GarbageCollectorMXBean> garbageCollectorBeans;

    private JvmGarbageCollectorMetrics(List<GarbageCollectorMXBean> garbageCollectorBeans, PrometheusProperties config) {
        this.config = config;
        this.garbageCollectorBeans = garbageCollectorBeans;
    }

    private void register(PrometheusRegistry registry) {

        SummaryWithCallback.builder(config)
                .name(JVM_GC_COLLECTION_SECONDS)
                .help("Time spent in a given JVM garbage collector in seconds.")
                .unit(Unit.SECONDS)
                .labelNames("gc")
                .callback(callback -> {
                    for (GarbageCollectorMXBean gc : garbageCollectorBeans) {
                        callback.call(gc.getCollectionCount(), Unit.millisToSeconds(gc.getCollectionTime()), Quantiles.EMPTY, gc.getName());
                    }
                })
                .register(registry);
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
        Builder garbageCollectorBeans(List<GarbageCollectorMXBean> garbageCollectorBeans) {
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
            new JvmGarbageCollectorMetrics(garbageCollectorBeans, config).register(registry);
        }
    }
}
