package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.CounterWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;

import static io.prometheus.metrics.model.snapshots.Unit.millisToSeconds;

/**
 * JVM Compilation metrics. The {@link JvmCompilationMetrics} are registered as part of the {@link JvmMetrics} like this:
 * <pre>{@code
 *   JvmMetrics.builder().register();
 * }</pre>
 * However, if you want only the {@link JvmCompilationMetrics} you can also register them directly:
 * <pre>{@code
 *   JvmCompilationMetrics.builder().register();
 * }</pre>
 * Example metrics being exported:
 * <pre>
 * # HELP jvm_compilation_time_seconds_total The total time in seconds taken for HotSpot class compilation
 * # TYPE jvm_compilation_time_seconds_total counter
 * jvm_compilation_time_seconds_total 0.152
 * </pre>
 */
public class JvmCompilationMetrics {

    private static final String JVM_COMPILATION_TIME_SECONDS_TOTAL = "jvm_compilation_time_seconds_total";

    private final PrometheusProperties config;
    private final CompilationMXBean compilationBean;

    private JvmCompilationMetrics(CompilationMXBean compilationBean, PrometheusProperties config) {
        this.compilationBean = compilationBean;
        this.config = config;
    }

    private void register(PrometheusRegistry registry) {

        if (compilationBean == null || !compilationBean.isCompilationTimeMonitoringSupported()) {
            return;
        }

        CounterWithCallback.builder(config)
                .name(JVM_COMPILATION_TIME_SECONDS_TOTAL)
                .help("The total time in seconds taken for HotSpot class compilation")
                .unit(Unit.SECONDS)
                .callback(callback -> callback.call(millisToSeconds(compilationBean.getTotalCompilationTime())))
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
        private CompilationMXBean compilationBean;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        /**
         * Package private. For testing only.
         */
        Builder compilationBean(CompilationMXBean compilationBean) {
            this.compilationBean = compilationBean;
            return this;
        }

        public void register() {
            register(PrometheusRegistry.defaultRegistry);
        }

        public void register(PrometheusRegistry registry) {
            CompilationMXBean compilationBean = this.compilationBean != null ? this.compilationBean : ManagementFactory.getCompilationMXBean();
            new JvmCompilationMetrics(compilationBean, config).register(registry);
        }
    }
}
