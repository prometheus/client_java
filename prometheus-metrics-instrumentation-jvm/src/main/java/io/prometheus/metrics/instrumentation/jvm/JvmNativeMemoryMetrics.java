package io.prometheus.metrics.instrumentation.jvm;

import io.prometheus.metrics.config.PrometheusProperties;
import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.Unit;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JVM native memory. JVM native memory tracking is disabled by default. You need to enable it by starting your JVM with this flag:
 * <pre>-XX:NativeMemoryTracking=summary</pre>
 * <p>
 * When native memory tracking is disabled the metrics are not registered either.
 * <p>
 * <p>
 * The {@link JvmNativeMemoryMetrics} are registered as part of the {@link JvmMetrics} like this:
 * <pre>{@code
 *   JvmMetrics.builder().register();
 * }</pre>
 * However, if you want only the {@link JvmNativeMemoryMetrics} you can also register them directly:
 * <pre>{@code
 *   JvmNativeMemoryMetrics.builder().register();
 * }</pre>
 * Example metrics being exported:
 * <pre>
 * # HELP jvm_native_memory_committed_bytes Committed bytes of a given JVM. Committed memory represents the amount of memory the JVM is using right now.
 * # TYPE jvm_native_memory_committed_bytes gauge
 * jvm_native_memory_committed_bytes{pool="Arena Chunk"} 58480.0
 * jvm_native_memory_committed_bytes{pool="Arguments"} 25119.0
 * jvm_native_memory_committed_bytes{pool="Class"} 1.00609438E8
 * jvm_native_memory_committed_bytes{pool="Code"} 2.7980888E7
 * jvm_native_memory_committed_bytes{pool="Compiler"} 529922.0
 * jvm_native_memory_committed_bytes{pool="GC"} 515466.0
 * jvm_native_memory_committed_bytes{pool="Internal"} 673194.0
 * jvm_native_memory_committed_bytes{pool="Java Heap"} 4.0923136E7
 * jvm_native_memory_committed_bytes{pool="Logging"} 4596.0
 * jvm_native_memory_committed_bytes{pool="Module"} 96408.0
 * jvm_native_memory_committed_bytes{pool="Native Memory Tracking"} 3929432.0
 * jvm_native_memory_committed_bytes{pool="Other"} 667656.0
 * jvm_native_memory_committed_bytes{pool="Safepoint"} 8192.0
 * jvm_native_memory_committed_bytes{pool="Symbol"} 2.4609808E7
 * jvm_native_memory_committed_bytes{pool="Synchronizer"} 272520.0
 * jvm_native_memory_committed_bytes{pool="Thread"} 3546896.0
 * jvm_native_memory_committed_bytes{pool="Total"} 2.0448392E8
 * jvm_native_memory_committed_bytes{pool="Tracing"} 1.0
 * jvm_native_memory_committed_bytes{pool="Unknown"} 32768.0
 * # HELP jvm_native_memory_reserved_bytes Reserved bytes of a given JVM. Reserved memory represents the total amount of memory the JVM can potentially use.
 * # TYPE jvm_native_memory_reserved_bytes gauge
 * jvm_native_memory_reserved_bytes{pool="Arena Chunk"} 25736.0
 * jvm_native_memory_reserved_bytes{pool="Arguments"} 25119.0
 * jvm_native_memory_reserved_bytes{pool="Class"} 1.162665374E9
 * jvm_native_memory_reserved_bytes{pool="Code"} 2.55386712E8
 * jvm_native_memory_reserved_bytes{pool="Compiler"} 529922.0
 * jvm_native_memory_reserved_bytes{pool="GC"} 1695114.0
 * jvm_native_memory_reserved_bytes{pool="Internal"} 673191.0
 * jvm_native_memory_reserved_bytes{pool="Java Heap"} 4.02653184E8
 * jvm_native_memory_reserved_bytes{pool="Logging"} 4596.0
 * jvm_native_memory_reserved_bytes{pool="Module"} 96408.0
 * jvm_native_memory_reserved_bytes{pool="Native Memory Tracking"} 3929400.0
 * jvm_native_memory_reserved_bytes{pool="Other"} 667656.0
 * jvm_native_memory_reserved_bytes{pool="Safepoint"} 8192.0
 * jvm_native_memory_reserved_bytes{pool="Symbol"} 2.4609808E7
 * jvm_native_memory_reserved_bytes{pool="Synchronizer"} 272520.0
 * jvm_native_memory_reserved_bytes{pool="Thread"} 3.383272E7
 * jvm_native_memory_reserved_bytes{pool="Total"} 1.887108421E9
 * jvm_native_memory_reserved_bytes{pool="Tracing"} 1.0
 * jvm_native_memory_reserved_bytes{pool="Unknown"} 32768.0
 * </pre>
 */
public class JvmNativeMemoryMetrics {
  private static final String JVM_NATIVE_MEMORY_RESERVED_BYTES = "jvm_native_memory_reserved_bytes";
  private static final String JVM_NATIVE_MEMORY_COMMITTED_BYTES = "jvm_native_memory_committed_bytes";

  private static final Pattern pattern = Pattern.compile("\\s*([A-Z][A-Za-z\\s]*[A-Za-z]+).*reserved=(\\d+), committed=(\\d+)");

  /**
   * Package private. For testing only.
   */
  static final AtomicBoolean isEnabled = new AtomicBoolean(true);

  private final PrometheusProperties config;
  private final PlatformMBeanServerAdapter adapter;

  private JvmNativeMemoryMetrics(PrometheusProperties config, PlatformMBeanServerAdapter adapter) {
    this.config = config;
    this.adapter = adapter;
  }

  private void register(PrometheusRegistry registry) {
    // first call will check if enabled and set the flag
    vmNativeMemorySummaryInBytesOrEmpty();
    if (isEnabled.get()) {
      GaugeWithCallback.builder(config)
          .name(JVM_NATIVE_MEMORY_RESERVED_BYTES)
          .help("Reserved bytes of a given JVM. Reserved memory represents the total amount of memory the JVM can potentially use.")
          .unit(Unit.BYTES)
          .labelNames("pool")
          .callback(makeCallback(true))
          .register(registry);

      GaugeWithCallback.builder(config)
          .name(JVM_NATIVE_MEMORY_COMMITTED_BYTES)
          .help("Committed bytes of a given JVM. Committed memory represents the amount of memory the JVM is using right now.")
          .unit(Unit.BYTES)
          .labelNames("pool")
          .callback(makeCallback(false))
          .register(registry);
    }
  }

  private Consumer<GaugeWithCallback.Callback> makeCallback(Boolean reserved) {
    return callback -> {
      String summary = vmNativeMemorySummaryInBytesOrEmpty();
      if (!summary.isEmpty()) {
        Matcher matcher = pattern.matcher(summary);
        while (matcher.find()) {
          String category = matcher.group(1);
          long value;
          if (reserved) {
            value = Long.parseLong(matcher.group(2));
          } else {
            value = Long.parseLong(matcher.group(3));
          }
          callback.call(value, category);
        }
      }
    };
  }

  private String vmNativeMemorySummaryInBytesOrEmpty() {
    if (!isEnabled.get()) {
      return "";
    }
    try {
      // requires -XX:NativeMemoryTracking=summary
      String summary = adapter.vmNativeMemorySummaryInBytes();
      if (summary.isEmpty() || summary.trim().contains("Native memory tracking is not enabled")) {
        isEnabled.set(false);
        return "";
      } else {
        return summary;
      }
    } catch (Exception ex) {
      // ignore errors
      isEnabled.set(false);
      return "";
    }
  }

  interface PlatformMBeanServerAdapter {
    String vmNativeMemorySummaryInBytes();
  }

  static class DefaultPlatformMBeanServerAdapter implements PlatformMBeanServerAdapter {
    @Override
    public String vmNativeMemorySummaryInBytes() {
      try {
        return (String) ManagementFactory.getPlatformMBeanServer().invoke(
            new ObjectName("com.sun.management:type=DiagnosticCommand"),
            "vmNativeMemory",
            new Object[]{new String[]{"summary", "scale=B"}},
            new String[]{"[Ljava.lang.String;"});
      } catch (ReflectionException | MalformedObjectNameException | InstanceNotFoundException | MBeanException e) {
        throw new IllegalStateException("Native memory tracking is not enabled", e);
      }
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
    private final PlatformMBeanServerAdapter adapter;

    private Builder(PrometheusProperties config) {
      this(config, new DefaultPlatformMBeanServerAdapter());
    }

    /**
     * Package private. For testing only.
     */
    Builder(PrometheusProperties config, PlatformMBeanServerAdapter adapter) {
      this.config = config;
      this.adapter = adapter;
    }

    public void register() {
      register(PrometheusRegistry.defaultRegistry);
    }

    public void register(PrometheusRegistry registry) {
      new JvmNativeMemoryMetrics(config, adapter).register(registry);
    }
  }
}
