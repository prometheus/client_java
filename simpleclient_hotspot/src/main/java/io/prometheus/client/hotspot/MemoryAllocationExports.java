package io.prometheus.client.hotspot;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;
import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryAllocationExports extends Collector {
  private final Map<String, AtomicLong> allocatedMap;

  public MemoryAllocationExports() {
    this(ManagementFactory.getGarbageCollectorMXBeans(), new ConcurrentHashMap<String, AtomicLong>());
  }

  // Visible for testing
  MemoryAllocationExports(List<GarbageCollectorMXBean> garbageCollectorMXBeans, Map<String, AtomicLong> allocatedMap) {
    this.allocatedMap = allocatedMap;
    AllocationCountingNotificationListener listener = new AllocationCountingNotificationListener(allocatedMap);
    for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
      ((NotificationEmitter) garbageCollectorMXBean).addNotificationListener(listener, null, null);
    }
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> sampleFamilies = new ArrayList<MetricFamilySamples>();
    CounterMetricFamily allocated = new CounterMetricFamily(
            "jvm_memory_pool_allocated_bytes",
            "Total bytes allocated in a given JVM memory pool. Only updated after GC, not continuously.",
            Collections.singletonList("pool"));
    sampleFamilies.add(allocated);

    for (Map.Entry<String, AtomicLong> entry : allocatedMap.entrySet()) {
      String memoryPool = entry.getKey();
      AtomicLong bytesAllocated = entry.getValue();
      allocated.addMetric(Collections.singletonList(memoryPool), bytesAllocated.doubleValue());
    }
    return sampleFamilies;
  }

  static class AllocationCountingNotificationListener implements NotificationListener {
    private final Map<String, AtomicLong> lastMemoryUsage = new ConcurrentHashMap<String, AtomicLong>();
    private final Map<String, AtomicLong> bytesAllocatedMap;

    AllocationCountingNotificationListener(Map<String, AtomicLong> bytesAllocatedMap) {
      this.bytesAllocatedMap = bytesAllocatedMap;
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
      AtomicLong last = getOrCreate(lastMemoryUsage, memoryPool);
      long diff1 = before - last.getAndSet(after);
      long diff2 = after - before;
      if (diff1 < 0) {
        diff1 = 0;
      }
      if (diff2 < 0) {
        diff2 = 0;
      }
      long increase = diff1 + diff2;
      if (increase > 0) {
        AtomicLong bytesAllocated = getOrCreate(bytesAllocatedMap, memoryPool);
        bytesAllocated.getAndAdd(increase);
      }
    }

    private AtomicLong getOrCreate(Map<String, AtomicLong> map, String key) {
      AtomicLong result = map.get(key);
      if (result == null) {
        result = new AtomicLong(0);
        map.put(key, result);
      }
      return result;
    }
  }
}
