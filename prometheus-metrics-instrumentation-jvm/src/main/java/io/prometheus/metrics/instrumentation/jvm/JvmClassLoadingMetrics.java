package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.CounterWithCallback;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;

/**
 * JVM Class Loading metrics. The {@link JvmClassLoadingMetrics} are registered as part of the {@link JvmMetrics} like this:
 * <pre>{@code
 *   JvmMetrics.builder().register();
 * }</pre>
 * However, if you want only the {@link JvmClassLoadingMetrics} you can also register them directly:
 * <pre>{@code
 *   JvmClassLoadingMetrics.builder().register();
 * }</pre>
 * Example metrics being exported:
 * <pre>
 * # HELP jvm_classes_currently_loaded The number of classes that are currently loaded in the JVM
 * # TYPE jvm_classes_currently_loaded gauge
 * jvm_classes_currently_loaded 1109.0
 * # HELP jvm_classes_loaded_total The total number of classes that have been loaded since the JVM has started execution
 * # TYPE jvm_classes_loaded_total counter
 * jvm_classes_loaded_total 1109.0
 * # HELP jvm_classes_unloaded_total The total number of classes that have been unloaded since the JVM has started execution
 * # TYPE jvm_classes_unloaded_total counter
 * jvm_classes_unloaded_total 0.0
 * </pre>
 */
public class JvmClassLoadingMetrics {

    private static final String JVM_CLASSES_CURRENTLY_LOADED = "jvm_classes_currently_loaded";
    private static final String JVM_CLASSES_LOADED_TOTAL = "jvm_classes_loaded_total";
    private static final String JVM_CLASSES_UNLOADED_TOTAL = "jvm_classes_unloaded_total";

    private final PrometheusProperties config;
    private final ClassLoadingMXBean classLoadingBean;

    private JvmClassLoadingMetrics(ClassLoadingMXBean classLoadingBean, PrometheusProperties config) {
        this.classLoadingBean = classLoadingBean;
        this.config = config;
    }

    private void register(PrometheusRegistry registry) {

        GaugeWithCallback.builder(config)
                .name(JVM_CLASSES_CURRENTLY_LOADED)
                .help("The number of classes that are currently loaded in the JVM")
                .callback(callback -> callback.call(classLoadingBean.getLoadedClassCount()))
                .register(registry);

        CounterWithCallback.builder(config)
                .name(JVM_CLASSES_LOADED_TOTAL)
                .help("The total number of classes that have been loaded since the JVM has started execution")
                .callback(callback -> callback.call(classLoadingBean.getTotalLoadedClassCount()))
                .register(registry);

        CounterWithCallback.builder(config)
                .name(JVM_CLASSES_UNLOADED_TOTAL)
                .help("The total number of classes that have been unloaded since the JVM has started execution")
                .callback(callback -> callback.call(classLoadingBean.getUnloadedClassCount()))
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
        private ClassLoadingMXBean classLoadingBean;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        /**
         * Package private. For testing only.
         */
        Builder classLoadingBean(ClassLoadingMXBean classLoadingBean) {
            this.classLoadingBean = classLoadingBean;
            return this;
        }

        public void register() {
            register(PrometheusRegistry.defaultRegistry);
        }

        public void register(PrometheusRegistry registry) {
            ClassLoadingMXBean classLoadingBean = this.classLoadingBean != null ? this.classLoadingBean : ManagementFactory.getClassLoadingMXBean();
            new JvmClassLoadingMetrics(classLoadingBean, config).register(registry);
        }
    }
}
