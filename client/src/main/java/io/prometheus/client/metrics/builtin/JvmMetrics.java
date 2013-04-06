package io.prometheus.client.metrics.builtin;

import com.google.common.collect.ImmutableMap;
import io.prometheus.client.Register;
import io.prometheus.client.metrics.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * <p>
 *   Standard JVM metrics via MXBeans. These are registered automatically.
 * </p>
 *
 * @author kim.altintop@gmail.com (Kim Altintop)
 */
public class JvmMetrics {
  private static final Logger log = LoggerFactory.getLogger(JvmMetrics.class);
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
  private static final Pattern CAMEL_CASE = Pattern.compile("([a-z\\d])([A-Z])");

  @Register(name = "jvm_metrics",
            docstring = "JVM Metrics via MXBeans",
            baseLabels = {})
  private static final Gauge jvmMetrics = new Gauge();

  private static final MBeanServer mBeanServer =
      ManagementFactory.getPlatformMBeanServer();
  private static final OperatingSystemMXBean osBean =
      ManagementFactory.getOperatingSystemMXBean();
  private static final Collection<GarbageCollectorMXBean> gcBeans =
      ManagementFactory.getGarbageCollectorMXBeans();
  private static final MemoryMXBean memBean =
      ManagementFactory.getMemoryMXBean();
  private static final Collection<MemoryPoolMXBean> poolBeans =
      ManagementFactory.getMemoryPoolMXBeans();

  private static final String[] BUFFER_POOLS_ATTRS =
      {"Count", "MemoryUsed", "TotalCapacity"};
  private static final String[] BUFFER_POOLS_POOLS =
      {"direct", "mapped"};

  public static void update() {
    jvmMetrics.set(ImmutableMap.of("bean", "memory_usage", "arena", "heap",
        "attribute", "init", "unit", "bytes"),
        memBean.getHeapMemoryUsage().getInit());
    jvmMetrics.set(ImmutableMap.of("bean", "memory_usage", "arena", "heap",
        "attribute", "used", "unit", "bytes"),
        memBean.getHeapMemoryUsage().getUsed());
    jvmMetrics.set(ImmutableMap.of("bean", "memory_usage", "arena", "heap",
        "attribute", "max", "unit", "bytes"),
        memBean.getHeapMemoryUsage().getMax());
    jvmMetrics.set(ImmutableMap.of("bean", "memory_usage", "arena", "heap",
        "attribute", "committed", "unit", "bytes"),
        memBean.getHeapMemoryUsage().getCommitted());

    jvmMetrics.set(ImmutableMap.of("bean", "memory_usage", "arena", "non_heap",
        "attribute", "init", "unit", "bytes"),
        memBean.getNonHeapMemoryUsage().getInit());
    jvmMetrics.set(ImmutableMap.of("bean", "memory_usage", "arena", "non_heap",
        "attribute", "used", "unit", "bytes"),
        memBean.getNonHeapMemoryUsage().getUsed());
    jvmMetrics.set(ImmutableMap.of("bean", "memory_usage", "arena", "non_heap",
        "attribute", "max", "unit", "bytes"),
        memBean.getNonHeapMemoryUsage().getMax());
    jvmMetrics.set(ImmutableMap.of("bean", "memory_usage", "arena", "non_heap",
        "attribute", "committed", "unit", "bytes"),
        memBean.getNonHeapMemoryUsage().getCommitted());

    for (final MemoryPoolMXBean pool : poolBeans) {
      final String name = sanitize(pool.getName());
      jvmMetrics.set(ImmutableMap.of("bean", "memory_pool", "pool", name,
          "attribute", "init", "unit", "bytes"),
          pool.getUsage().getInit());
      jvmMetrics.set(ImmutableMap.of("bean", "memory_pool", "pool", name,
          "attribute", "used", "unit", "bytes"),
          pool.getUsage().getUsed());
      jvmMetrics.set(ImmutableMap.of("bean", "memory_pool", "pool", name,
          "attribute", "max", "unit", "bytes"),
          pool.getUsage().getMax());
      jvmMetrics.set(ImmutableMap.of("bean", "memory_pool", "pool", name,
          "attribute", "committed", "unit", "bytes"),
          pool.getUsage().getCommitted());
    }

    for (final GarbageCollectorMXBean bean : gcBeans) {
      final String name = sanitize(bean.getName());
      jvmMetrics.set(ImmutableMap.of("bean", "garbage_collector",
          "collector", name, "attribute", "count"),
          bean.getCollectionCount());
      jvmMetrics.set(ImmutableMap.of("bean", "garbage_collector",
          "collector", name, "attribute", "time", "unit", "milliseconds"),
          bean.getCollectionTime());
    }

    try {
      jvmMetrics.set(ImmutableMap.of("bean", "operating_system",
          "attribute", "open_file_descriptor_count"),
          osBeanAttribute("getOpenFileDescriptorCount"));
      jvmMetrics.set(ImmutableMap.of("bean", "operating_system",
          "attribute", "max_file_descriptor_count"),
          osBeanAttribute("getMaxFileDescriptorCount"));
    } catch (Exception ignored) {
      log.debug("File descriptor counts unavailable");
    }

    for (String pool : BUFFER_POOLS_POOLS) {
      for (final String attribute : BUFFER_POOLS_ATTRS) {
        try {
          final ObjectName on = new ObjectName("java.nio:type=BufferPool,name=" + pool);
          mBeanServer.getMBeanInfo(on);
          jvmMetrics.set(ImmutableMap.of("bean", "buffer_pool","pool", pool,
              "attribute", sanitize(attribute)),
              (Long) mBeanServer.getAttribute(on, attribute));
        } catch (JMException ignored) {
          log.debug("Buffer Pool metrics unavailable (Java 6?)");
        }
      }
    }
  }

  private static long osBeanAttribute(String name)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    final Method method = osBean.getClass().getDeclaredMethod(name);
    method.setAccessible(true);
    return (Long) method.invoke(osBean);
  }

  private static String sanitize(String label) {
    return CAMEL_CASE.matcher(WHITESPACE.matcher(label).replaceAll("_"))
        .replaceAll("$1_$2").toLowerCase();
  }
}