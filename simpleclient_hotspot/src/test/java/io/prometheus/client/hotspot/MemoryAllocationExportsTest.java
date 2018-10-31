package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import org.junit.Test;

import java.lang.management.GarbageCollectorMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MemoryAllocationExportsTest {

  @Test
  public void testCollectEmpty() {
    MemoryAllocationExports exports = new MemoryAllocationExports(
            Collections.<GarbageCollectorMXBean>emptyList(),
            Collections.<String, AtomicLong>emptyMap()
    );

    List<Collector.MetricFamilySamples> initialCollect = exports.collect();
    assertNotNull(initialCollect);
    assertEquals(1, initialCollect.size());
    Collector.MetricFamilySamples metricFamilySamples = initialCollect.get(0);
    assertEquals(Collector.Type.COUNTER, metricFamilySamples.type);
    assertTrue(metricFamilySamples.samples.isEmpty());
  }

  @Test
  public void testCollectSimple() {
    MemoryAllocationExports exports = new MemoryAllocationExports(
            Collections.<GarbageCollectorMXBean>emptyList(),
            Collections.singletonMap("TestPool", new AtomicLong(123))
    );

    List<Collector.MetricFamilySamples> initialCollect = exports.collect();
    assertNotNull(initialCollect);
    assertEquals(1, initialCollect.size());
    Collector.MetricFamilySamples metricFamilySamples = initialCollect.get(0);
    assertEquals(Collector.Type.COUNTER, metricFamilySamples.type);
    assertEquals(1, metricFamilySamples.samples.size());
    Collector.MetricFamilySamples.Sample sample = metricFamilySamples.samples.get(0);
    assertEquals(1, sample.labelNames.size());
    assertEquals("pool", sample.labelNames.get(0));
    assertEquals(1, sample.labelValues.size());
    assertEquals("TestPool", sample.labelValues.get(0));
    assertEquals(123, sample.value, 0.001);
  }

  @Test
  public void testListenerLogic() {
    HashMap<String, AtomicLong> allocatedMap = new HashMap<String, AtomicLong>();
    MemoryAllocationExports.AllocationCountingNotificationListener listener =
            new MemoryAllocationExports.AllocationCountingNotificationListener(allocatedMap);

    // Increase by 123
    listener.handleMemoryPool("TestPool", 0, 123);
    assertTrue(allocatedMap.containsKey("TestPool"));
    assertEquals(123, allocatedMap.get("TestPool").get());

    // No increase
    listener.handleMemoryPool("TestPool", 123, 123);
    assertEquals(123, allocatedMap.get("TestPool").get());

    // No increase, then decrease to 0
    listener.handleMemoryPool("TestPool", 123, 0);
    assertEquals(123, allocatedMap.get("TestPool").get());

    // No increase, then increase by 7
    listener.handleMemoryPool("TestPool", 0, 7);
    assertEquals(130, allocatedMap.get("TestPool").get());

    // Increase by 10, then decrease to 10
    listener.handleMemoryPool("TestPool", 17, 10);
    assertEquals(140, allocatedMap.get("TestPool").get());

    // Increase by 7, then increase by 3
    listener.handleMemoryPool("TestPool", 17, 20);
    assertEquals(150, allocatedMap.get("TestPool").get());

    // Decrease to 17, then increase by 3
    listener.handleMemoryPool("TestPool", 17, 20);
    assertEquals(153, allocatedMap.get("TestPool").get());
  }
}
