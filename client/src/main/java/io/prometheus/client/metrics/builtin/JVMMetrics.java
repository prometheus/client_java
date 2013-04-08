package io.prometheus.client.metrics.builtin;

import com.google.common.collect.ImmutableMap;
import io.prometheus.client.Register;
import io.prometheus.client.Registry;
import static io.prometheus.client.Registry.emptyLabels;
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
public class JVMMetrics {
  private static final Logger log = LoggerFactory.getLogger(JVMMetrics.class);
  private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
  private static final Pattern CAMEL_CASE = Pattern.compile("([a-z\\d])([A-Z])");

  @Register(name = "memory_usage_heap_total", 
      docstring = "Overall JVM heap memory usage", baseLabels = {})
  private static final Gauge heapMemTotal = new Gauge();

  @Register(name = "memory_usage_non_heap_total",
      docstring = "Overall JVM non-heap memory usage", baseLabels = {})
  private static final Gauge nonHeapMemTotal = new Gauge();

  @Register(name = "memory_pools_usage",
      docstring = "JVM memory usage by memory pool", baseLabels = {})
  private static final Gauge memPools = new Gauge();

  @Register(name = "garbage_collectors",
      docstring = "JVM Garbage Collector runs and timing by collector",
      baseLabels = {})
  private static final Gauge gc = new Gauge();

  @Register(name = "open_file_descriptors",
      docstring = "Number of open file descriptors held by this JVM instance",
      baseLabels = {})
  private static final Gauge openFDs = new Gauge();

  @Register(name = "max_file_descriptors",
      docstring = "Maximum number of file descriptors this JVM instance could" +
          " open (constant value)",
      baseLabels = {})
  private final static Gauge maxFDs = new Gauge();

  @Register(name = "buffer_pools_direct_count",
      docstring = "Number of direct NIO buffer pools allocated",
      baseLabels = {})
  private final static Gauge bufferPoolsDirectCount = new Gauge();

  @Register(name = "buffer_pools_mapped_count",
      docstring = "Number of mapped NIO buffer pools allocated",
      baseLabels = {})
  private final static Gauge bufferPoolsMappedCount = new Gauge();

  @Register(name = "buffer_pools_direct_memory_used",
      docstring = "Cumulative memory usage of direct NIO buffer pools",
      baseLabels = {})
  private final static Gauge bufferPoolsDirectMem = new Gauge();

  @Register(name = "buffer_pools_mapped_memory_used",
      docstring = "Cumulative memory usage of mapped NIO buffer pools",
      baseLabels = {})
  private final static Gauge bufferPoolsMappedMem = new Gauge();

  @Register(name = "buffer_pools_direct_total_capacity",
      docstring = "Total capacity of direct NIO buffer pools",
      baseLabels = {})
  private final static Gauge bufferPoolsDirectCapacity = new Gauge();

  @Register(name = "buffer_pools_mapped_total_capacity",
      docstring = "Total capacity of mapped NIO buffer pools",
      baseLabels = {})
  private final static Gauge bufferPoolsMappedCapacity = new Gauge();

  @Register(name = "thread_count",
      docstring = "Number of live threads",
      baseLabels = {})
  private final static Gauge threadCount = new Gauge();

  @Register(name = "thread_daemon_count",
      docstring = "Number of live daemon threads",
      baseLabels = {})
  private final static Gauge daemonThreadCount = new Gauge();

  @Register(name = "thread_deadlocks_count",
      docstring = "Number of deadlocked threads",
      baseLabels = {})
  private final static Gauge deadlockedThreadsCount = new Gauge();


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
  private static final ThreadMXBean threadsBean =
      ManagementFactory.getThreadMXBean();

  public static void update() {
    updateMemoryUsageMetrics();
    updateMemoryPoolMetrics();
    updateGCMetrics();
    updateFileDescriptorMetrics();
    updateBufferPoolsMetrics();
    updateThreadsMetrics();
  }

  private static void updateThreadsMetrics() {
    threadCount.set(emptyLabels(), threadsBean.getThreadCount());
    daemonThreadCount.set(emptyLabels(), threadsBean.getDaemonThreadCount());
    final long[] deadlocked = threadsBean.findDeadlockedThreads();
    final long numDeadlocked = deadlocked == null ? 0L : deadlocked.length;
    deadlockedThreadsCount.set(emptyLabels(), numDeadlocked);
  }

  private static void updateMemoryUsageMetrics() {
    heapMemTotal.set(ImmutableMap.of("attribute", "init"),
        memBean.getHeapMemoryUsage().getInit());
    heapMemTotal.set(ImmutableMap.of("attribute", "used"),
        memBean.getHeapMemoryUsage().getUsed());
    heapMemTotal.set(ImmutableMap.of("attribute", "max"),
        memBean.getHeapMemoryUsage().getMax());
    heapMemTotal.set(ImmutableMap.of("attribute", "committed"),
        memBean.getHeapMemoryUsage().getCommitted());

    nonHeapMemTotal.set(ImmutableMap.of("attribute", "init"),
        memBean.getNonHeapMemoryUsage().getInit());
    nonHeapMemTotal.set(ImmutableMap.of("attribute", "used"),
        memBean.getNonHeapMemoryUsage().getUsed());
    nonHeapMemTotal.set(ImmutableMap.of("attribute", "max"),
        memBean.getNonHeapMemoryUsage().getMax());
    nonHeapMemTotal.set(ImmutableMap.of("attribute", "committed"),
        memBean.getNonHeapMemoryUsage().getCommitted());
  }

  private static void updateMemoryPoolMetrics() {
    for (final MemoryPoolMXBean pool : poolBeans) {
      final String name = sanitize(pool.getName());
      memPools.set(ImmutableMap.of("pool", name, "attribute", "init"),
          pool.getUsage().getInit());
      memPools.set(ImmutableMap.of("pool", name, "attribute", "used"),
          pool.getUsage().getUsed());
      memPools.set(ImmutableMap.of("pool", name, "attribute", "max"),
          pool.getUsage().getMax());
      memPools.set(ImmutableMap.of("pool", name, "attribute", "committed"),
          pool.getUsage().getCommitted());
    }
  }

  private static void updateGCMetrics() {
    for (final GarbageCollectorMXBean bean : gcBeans) {
      final String name = sanitize(bean.getName());
      gc.set(ImmutableMap.of("collector", name, "attribute", "collection_count"),
          bean.getCollectionCount());
      gc.set(ImmutableMap.of("collector", name, "attribute", "collection_time_ms"),
          bean.getCollectionTime());
    }
  }

  private static void updateFileDescriptorMetrics() {
    try {
      openFDs.set(emptyLabels(),
          osBeanAttribute("getOpenFileDescriptorCount"));
      maxFDs.set(emptyLabels(),
          osBeanAttribute("getMaxFileDescriptorCount"));
    } catch (Exception ignored) {
      log.debug("File descriptor counts unavailable");
    }
  }

  private static void updateBufferPoolsMetrics() {
    try {
      final ObjectName onDirect = new ObjectName("java.nio:type=BufferPool," +
          "name=direct");
      final ObjectName onMapped = new ObjectName("java.nio:type=BufferPool," +
          "name=mapped");

      bufferPoolsDirectCapacity.set(emptyLabels(),
          (Long) mBeanServer.getAttribute(onDirect, "TotalCapacity"));
      bufferPoolsDirectCount.set(emptyLabels(),
          (Long) mBeanServer.getAttribute(onDirect, "Count"));
      bufferPoolsDirectMem.set(emptyLabels(),
          (Long) mBeanServer.getAttribute(onDirect, "MemoryUsed"));

      bufferPoolsMappedCapacity.set(emptyLabels(),
          (Long) mBeanServer.getAttribute(onDirect, "TotalCapacity"));
      bufferPoolsMappedCount.set(emptyLabels(),
          (Long) mBeanServer.getAttribute(onDirect, "Count"));
      bufferPoolsMappedMem.set(emptyLabels(),
          (Long) mBeanServer.getAttribute(onDirect, "MemoryUsed"));
    } catch (JMException ignored) {
      log.debug("Buffer Pool metrics unavailable (Java 6?)");
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