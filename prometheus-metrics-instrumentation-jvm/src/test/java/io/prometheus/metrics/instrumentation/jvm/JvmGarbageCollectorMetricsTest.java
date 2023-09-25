package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.GarbageCollectorMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


public class GarbageCollectorExportsTest {

  private GarbageCollectorMXBean mockGcBean1 = Mockito.mock(GarbageCollectorMXBean.class);
  private GarbageCollectorMXBean mockGcBean2 = Mockito.mock(GarbageCollectorMXBean.class);
  private List<GarbageCollectorMXBean> mockList = Arrays.asList(mockGcBean1, mockGcBean2);
  private CollectorRegistry registry = new CollectorRegistry();
  private GarbageCollectorExports collectorUnderTest;

  @Before
  public void setUp() {
    when(mockGcBean1.getName()).thenReturn("MyGC1");
    when(mockGcBean1.getCollectionCount()).thenReturn(100L);
    when(mockGcBean1.getCollectionTime()).thenReturn(TimeUnit.SECONDS.toMillis(10));
    when(mockGcBean2.getName()).thenReturn("MyGC2");
    when(mockGcBean2.getCollectionCount()).thenReturn(200L);
    when(mockGcBean2.getCollectionTime()).thenReturn(TimeUnit.SECONDS.toMillis(20));
    collectorUnderTest = new GarbageCollectorExports(mockList).register(registry);
  }

  @Test
  public void testGarbageCollectorExports() {
    assertEquals(
        100L,
        registry.getSampleValue(
            "jvm_gc_collection_seconds_count",
            new String[]{"gc"},
            new String[]{"MyGC1"}),
        .0000001);
    assertEquals(
        10d,
        registry.getSampleValue(
            "jvm_gc_collection_seconds_sum",
            new String[]{"gc"},
            new String[]{"MyGC1"}),
        .0000001);
    assertEquals(
        200L,
        registry.getSampleValue(
            "jvm_gc_collection_seconds_count",
            new String[]{"gc"},
            new String[]{"MyGC2"}),
        .0000001);
    assertEquals(
        20d,
        registry.getSampleValue(
            "jvm_gc_collection_seconds_sum",
            new String[]{"gc"},
            new String[]{"MyGC2"}),
        .0000001);
  }
}
