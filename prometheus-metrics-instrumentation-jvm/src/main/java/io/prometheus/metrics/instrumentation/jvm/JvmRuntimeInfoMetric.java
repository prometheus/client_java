package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.Info;
import io.prometheus.metrics.model.registry.PrometheusRegistry;

/**
 * JVM Runtime Info metric. The {@link JvmRuntimeInfoMetric} is registered as part of the {@link JvmMetrics} like this:
 * <pre>{@code
 *   JvmMetrics.builder().register();
 * }</pre>
 * However, if you want only the {@link JvmRuntimeInfoMetric} you can also register them directly:
 * <pre>{@code
 *   JvmRuntimeInfoMetric.builder().register();
 * }</pre>
 *
 * <pre>
 * # TYPE jvm_runtime info
 * # HELP jvm_runtime JVM runtime info
 * jvm_runtime_info{runtime="OpenJDK Runtime Environment",vendor="Oracle Corporation",version="1.8.0_382-b05"} 1
 * </pre>
 */
public class JvmRuntimeInfoMetric {

    private static final String JVM_RUNTIME_INFO = "jvm_runtime_info";

    private final PrometheusProperties config;
    private final String version;
    private final String vendor;
    private final String runtime;

    private JvmRuntimeInfoMetric(String version, String vendor, String runtime, PrometheusProperties config) {
        this.config = config;
        this.version = version;
        this.vendor = vendor;
        this.runtime = runtime;
    }

    private void register(PrometheusRegistry registry) {

        Info jvmInfo = Info.builder(config)
                .name(JVM_RUNTIME_INFO)
                .help("JVM runtime info")
                .labelNames("version", "vendor", "runtime")
                .register(registry);

        jvmInfo.setLabelValues(version, vendor, runtime);
    }

    public static Builder builder() {
        return new Builder(PrometheusProperties.get());
    }

    public static Builder builder(PrometheusProperties config) {
        return new Builder(config);
    }

    public static class Builder {

        private final PrometheusProperties config;
        private String version;
        private String vendor;
        private String runtime;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        /**
         * Package private. For testing only.
         */
        Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Package private. For testing only.
         */
        Builder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        /**
         * Package private. For testing only.
         */
        Builder runtime(String runtime) {
            this.runtime = runtime;
            return this;
        }

        public void register() {
            register(PrometheusRegistry.defaultRegistry);
        }

        public void register(PrometheusRegistry registry) {
            String version = this.version != null ? this.version : System.getProperty("java.runtime.version", "unknown");
            String vendor = this.vendor != null ? this.vendor : System.getProperty("java.vm.vendor", "unknown");
            String runtime = this.runtime != null ? this.runtime : System.getProperty("java.runtime.name", "unknown");
            new JvmRuntimeInfoMetric(version, vendor, runtime, config).register(registry);
        }
    }
}
