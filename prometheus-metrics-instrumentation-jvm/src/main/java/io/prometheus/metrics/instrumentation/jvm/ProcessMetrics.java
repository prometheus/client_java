package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.CounterWithCallback;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Process metrics.
 * <p>
 * These metrics are defined in the <a href="https://prometheus.io/docs/instrumenting/writing_clientlibs/#process-metrics">process metrics</a>
 * section of the Prometheus client library documentation, and they are implemented across client libraries in multiple programming languages.
 * <p>
 * Technically, some of them are OS-level metrics and not JVM-level metrics. However, I'm still putting them
 * in the {@code prometheus-metrics-instrumentation-jvm} module, because first it seems overkill to create a separate
 * Maven module just for the {@link ProcessMetrics} class, and seconds some of these metrics are coming from the JVM via JMX anyway.
 * <p>
 * The {@link ProcessMetrics} are registered as part of the {@link JvmMetrics} like this:
 * <pre>{@code
 *   JvmMetrics.builder().register();
 * }</pre>
 * However, if you want only the {@link ProcessMetrics} you can also register them directly:
 * <pre>{@code
 *   ProcessMetrics.builder().register();
 * }</pre>
 * Example metrics being exported:
 * <pre>
 * # HELP process_cpu_seconds_total Total user and system CPU time spent in seconds.
 * # TYPE process_cpu_seconds_total counter
 * process_cpu_seconds_total 1.63
 * # HELP process_max_fds Maximum number of open file descriptors.
 * # TYPE process_max_fds gauge
 * process_max_fds 524288.0
 * # HELP process_open_fds Number of open file descriptors.
 * # TYPE process_open_fds gauge
 * process_open_fds 28.0
 * # HELP process_resident_memory_bytes Resident memory size in bytes.
 * # TYPE process_resident_memory_bytes gauge
 * process_resident_memory_bytes 7.8577664E7
 * # HELP process_start_time_seconds Start time of the process since unix epoch in seconds.
 * # TYPE process_start_time_seconds gauge
 * process_start_time_seconds 1.693829439767E9
 * # HELP process_virtual_memory_bytes Virtual memory size in bytes.
 * # TYPE process_virtual_memory_bytes gauge
 * process_virtual_memory_bytes 1.2683624448E10
 * </pre>
 */
public class ProcessMetrics {

    private static final String PROCESS_CPU_SECONDS_TOTAL = "process_cpu_seconds_total";
    private static final String PROCESS_START_TIME_SECONDS = "process_start_time_seconds";
    private static final String PROCESS_OPEN_FDS = "process_open_fds";
    private static final String PROCESS_MAX_FDS = "process_max_fds";
    private static final String PROCESS_VIRTUAL_MEMORY_BYTES = "process_virtual_memory_bytes";
    private static final String PROCESS_RESIDENT_MEMORY_BYTES = "process_resident_memory_bytes";

    private static final File PROC_SELF_STATUS = new File("/proc/self/status");

    private final PrometheusProperties config;
    private final OperatingSystemMXBean osBean;
    private final RuntimeMXBean runtimeBean;
    private final Grepper grepper;
    private final boolean linux;

    private ProcessMetrics(OperatingSystemMXBean osBean, RuntimeMXBean runtimeBean, Grepper grepper, PrometheusProperties config) {
        this.osBean = osBean;
        this.runtimeBean = runtimeBean;
        this.grepper = grepper;
        this.config = config;
        this.linux = PROC_SELF_STATUS.canRead();
    }

    private void register(PrometheusRegistry registry) {

        CounterWithCallback.builder(config)
                .name(PROCESS_CPU_SECONDS_TOTAL)
                .help("Total user and system CPU time spent in seconds.")
                .unit(Unit.SECONDS)
                .callback(callback -> {
                    try {
                        // There exist at least 2 similar but unrelated UnixOperatingSystemMXBean interfaces, in
                        // com.sun.management and com.ibm.lang.management. Hence use reflection and recursively go
                        // through implemented interfaces until the method can be made accessible and invoked.
                        Long processCpuTime = callLongGetter("getProcessCpuTime", osBean);
                        if (processCpuTime != null) {
                            callback.call(Unit.nanosToSeconds(processCpuTime));
                        }
                    } catch (Exception ignored) {
                    }
                })
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(PROCESS_START_TIME_SECONDS)
                .help("Start time of the process since unix epoch in seconds.")
                .unit(Unit.SECONDS)
                .callback(callback -> callback.call(Unit.millisToSeconds(runtimeBean.getStartTime())))
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(PROCESS_OPEN_FDS)
                .help("Number of open file descriptors.")
                .callback(callback -> {
                    try {
                        Long openFds = callLongGetter("getOpenFileDescriptorCount", osBean);
                        if (openFds != null) {
                            callback.call(openFds);
                        }
                    } catch (Exception ignored) {
                    }
                })
                .register(registry);

        GaugeWithCallback.builder(config)
                .name(PROCESS_MAX_FDS)
                .help("Maximum number of open file descriptors.")
                .callback(callback -> {
                    try {
                        Long maxFds = callLongGetter("getMaxFileDescriptorCount", osBean);
                        if (maxFds != null) {
                            callback.call(maxFds);
                        }
                    } catch (Exception ignored) {
                    }
                })
                .register(registry);

        if (linux) {

            GaugeWithCallback.builder(config)
                    .name(PROCESS_VIRTUAL_MEMORY_BYTES)
                    .help("Virtual memory size in bytes.")
                    .unit(Unit.BYTES)
                    .callback(callback -> {
                        try {
                            String line = grepper.lineStartingWith(PROC_SELF_STATUS, "VmSize:");
                            callback.call(Unit.kiloBytesToBytes(Double.parseDouble(line.split("\\s+")[1])));
                        } catch (Exception ignored) {
                        }
                    })
                    .register(registry);

            GaugeWithCallback.builder(config)
                    .name(PROCESS_RESIDENT_MEMORY_BYTES)
                    .help("Resident memory size in bytes.")
                    .unit(Unit.BYTES)
                    .callback(callback -> {
                        try {
                            String line = grepper.lineStartingWith(PROC_SELF_STATUS, "VmRSS:");
                            callback.call(Unit.kiloBytesToBytes(Double.parseDouble(line.split("\\s+")[1])));
                        } catch (Exception ignored) {
                        }
                    })
                    .register(registry);
        }
    }

    private Long callLongGetter(String getterName, Object obj) throws NoSuchMethodException, InvocationTargetException {
        return callLongGetter(obj.getClass().getMethod(getterName), obj);
    }

    /**
     * Attempts to call a method either directly or via one of the implemented interfaces.
     * <p>
     * A Method object refers to a specific method declared in a specific class. The first invocation
     * might happen with method == SomeConcreteClass.publicLongGetter() and will fail if
     * SomeConcreteClass is not public. We then recurse over all interfaces implemented by
     * SomeConcreteClass (or extended by those interfaces and so on) until we eventually invoke
     * callMethod() with method == SomePublicInterface.publicLongGetter(), which will then succeed.
     * <p>
     * There is a built-in assumption that the method will never return null (or, equivalently, that
     * it returns the primitive data type, i.e. {@code long} rather than {@code Long}). If this
     * assumption doesn't hold, the method might be called repeatedly and the returned value will be
     * the one produced by the last call.
     */
    private Long callLongGetter(Method method, Object obj) throws InvocationTargetException  {
        try {
            return (Long) method.invoke(obj);
        } catch (IllegalAccessException e) {
            // Expected, the declaring class or interface might not be public.
        }

        // Iterate over all implemented/extended interfaces and attempt invoking the method with the
        // same name and parameters on each.
        for (Class<?> clazz : method.getDeclaringClass().getInterfaces()) {
            try {
                Method interfaceMethod = clazz.getMethod(method.getName(), method.getParameterTypes());
                Long result = callLongGetter(interfaceMethod, obj);
                if (result != null) {
                    return result;
                }
            } catch (NoSuchMethodException e) {
                // Expected, class might implement multiple, unrelated interfaces.
            }
        }
        return null;
    }

    interface Grepper {
        String lineStartingWith(File file, String prefix) throws IOException;
    }

    private static class FileGrepper implements Grepper {

        @Override
        public String lineStartingWith(File file, String prefix) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                while (line != null) {
                    if (line.startsWith(prefix)) {
                        return line;
                    }
                    line = reader.readLine();
                }
            }
            return null;
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
        private OperatingSystemMXBean osBean;
        private RuntimeMXBean runtimeBean;
        private Grepper grepper;

        private Builder(PrometheusProperties config) {
            this.config = config;
        }

        /**
         * Package private. For testing only.
         */
        Builder osBean(OperatingSystemMXBean osBean) {
            this.osBean = osBean;
            return this;
        }

        /**
         * Package private. For testing only.
         */
        Builder runtimeBean(RuntimeMXBean runtimeBean) {
            this.runtimeBean = runtimeBean;
            return this;
        }

        /**
         * Package private. For testing only.
         */
        Builder grepper(Grepper grepper) {
            this.grepper = grepper;
            return this;
        }

        public void register() {
            register(PrometheusRegistry.defaultRegistry);
        }

        public void register(PrometheusRegistry registry) {
            OperatingSystemMXBean osBean = this.osBean != null ? this.osBean : ManagementFactory.getOperatingSystemMXBean();
            RuntimeMXBean runtimeMXBean = this.runtimeBean != null ? this.runtimeBean : ManagementFactory.getRuntimeMXBean();
            Grepper grepper = this.grepper != null ? this.grepper : new FileGrepper();
            new ProcessMetrics(osBean, runtimeMXBean, grepper, config).register(registry);
        }
    }
}
