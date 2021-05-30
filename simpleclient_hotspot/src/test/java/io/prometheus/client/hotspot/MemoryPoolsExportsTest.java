package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class MemoryPoolsExportsTest {

  private MemoryPoolMXBean mockPoolsBean1 = Mockito.mock(MemoryPoolMXBean.class);
  private MemoryPoolMXBean mockPoolsBean2 = Mockito.mock(MemoryPoolMXBean.class);
  private MemoryMXBean mockMemoryBean = Mockito.mock(MemoryMXBean.class);
  private MemoryUsage mockUsage1 = Mockito.mock(MemoryUsage.class);
  private MemoryUsage mockCollectionUsage1 = Mockito.mock(MemoryUsage.class);
  private MemoryUsage mockUsage2 = Mockito.mock(MemoryUsage.class);
  private MemoryUsage mockCollectionUsage2 = Mockito.mock(MemoryUsage.class);
  private List<MemoryPoolMXBean> mockList = Arrays.asList(mockPoolsBean1, mockPoolsBean2);
  private CollectorRegistry registry = new CollectorRegistry();
  private MemoryPoolsExports collectorUnderTest;

  @Before
  public void setUp() {
    when(mockPoolsBean1.getName()).thenReturn("PS Eden Space");
    when(mockPoolsBean1.getUsage()).thenReturn(mockUsage1);
    when(mockPoolsBean1.getCollectionUsage()).thenReturn(mockCollectionUsage1);
    when(mockPoolsBean2.getName()).thenReturn("PS Old Gen");
    when(mockPoolsBean2.getUsage()).thenReturn(mockUsage2);
    when(mockPoolsBean2.getCollectionUsage()).thenReturn(mockCollectionUsage2);
    when(mockMemoryBean.getObjectPendingFinalizationCount()).thenReturn(10);
    when(mockMemoryBean.getHeapMemoryUsage()).thenReturn(mockUsage1);
    when(mockMemoryBean.getNonHeapMemoryUsage()).thenReturn(mockUsage2);
    when(mockUsage1.getUsed()).thenReturn(500000L);
    when(mockUsage1.getCommitted()).thenReturn(1000000L);
    when(mockUsage1.getMax()).thenReturn(2000000L);
    when(mockUsage1.getInit()).thenReturn(1000L);
    when(mockCollectionUsage1.getUsed()).thenReturn(400000L);
    when(mockCollectionUsage1.getCommitted()).thenReturn(800000L);
    when(mockCollectionUsage1.getMax()).thenReturn(1600000L);
    when(mockCollectionUsage1.getInit()).thenReturn(2000L);
    when(mockUsage2.getUsed()).thenReturn(10000L);
    when(mockUsage2.getCommitted()).thenReturn(20000L);
    when(mockUsage2.getMax()).thenReturn(3000000L);
    when(mockUsage2.getInit()).thenReturn(2000L);
    when(mockCollectionUsage2.getUsed()).thenReturn(20000L);
    when(mockCollectionUsage2.getCommitted()).thenReturn(40000L);
    when(mockCollectionUsage2.getMax()).thenReturn(6000000L);
    when(mockCollectionUsage2.getInit()).thenReturn(4000L);
    collectorUnderTest = new MemoryPoolsExports(mockMemoryBean, mockList).register(registry);
  }

  @Test
  public void testMemoryPools() {
    assertEquals(
        500000L,
        registry.getSampleValue(
            "jvm_memory_pool_bytes_used",
            new String[]{"pool"},
            new String[]{"PS Eden Space"}),
        .0000001);
    assertEquals(
        1000000L,
        registry.getSampleValue(
            "jvm_memory_pool_bytes_committed",
            new String[]{"pool"},
            new String[]{"PS Eden Space"}),
        .0000001);
    assertEquals(
        2000000L,
        registry.getSampleValue(
            "jvm_memory_pool_bytes_max",
            new String[]{"pool"},
            new String[]{"PS Eden Space"}),
        .0000001);
    assertEquals(
        1000L,
        registry.getSampleValue(
            "jvm_memory_pool_bytes_init",
            new String[]{"pool"},
            new String[]{"PS Eden Space"}),
        .0000001);
    assertEquals(
        400000L,
        registry.getSampleValue(
            "jvm_memory_pool_collection_used_bytes",
            new String[]{"pool"},
            new String[]{"PS Eden Space"}),
        .0000001);
    assertEquals(
        800000L,
        registry.getSampleValue(
            "jvm_memory_pool_collection_committed_bytes",
            new String[]{"pool"},
            new String[]{"PS Eden Space"}),
        .0000001);
    assertEquals(
        1600000L,
        registry.getSampleValue(
            "jvm_memory_pool_collection_max_bytes",
            new String[]{"pool"},
            new String[]{"PS Eden Space"}),
        .0000001);
    assertEquals(
        2000L,
        registry.getSampleValue(
            "jvm_memory_pool_collection_init_bytes",
            new String[]{"pool"},
            new String[]{"PS Eden Space"}),
        .0000001);
    assertEquals(
        10000L,
        registry.getSampleValue(
            "jvm_memory_pool_bytes_used",
            new String[]{"pool"},
            new String[]{"PS Old Gen"}),
        .0000001);
    assertEquals(
        20000L,
        registry.getSampleValue(
            "jvm_memory_pool_bytes_committed",
            new String[]{"pool"},
            new String[]{"PS Old Gen"}),
        .0000001);
    assertEquals(
        3000000L,
        registry.getSampleValue(
            "jvm_memory_pool_bytes_max",
            new String[]{"pool"},
            new String[]{"PS Old Gen"}),
        .0000001);
    assertEquals(
        2000L,
        registry.getSampleValue(
            "jvm_memory_pool_bytes_init",
            new String[]{"pool"},
            new String[]{"PS Old Gen"}),
        .0000001);
    assertEquals(
        20000L,
        registry.getSampleValue(
            "jvm_memory_pool_collection_used_bytes",
            new String[]{"pool"},
            new String[]{"PS Old Gen"}),
        .0000001);
    assertEquals(
        40000L,
        registry.getSampleValue(
            "jvm_memory_pool_collection_committed_bytes",
            new String[]{"pool"},
            new String[]{"PS Old Gen"}),
        .0000001);
    assertEquals(
        6000000L,
        registry.getSampleValue(
            "jvm_memory_pool_collection_max_bytes",
            new String[]{"pool"},
            new String[]{"PS Old Gen"}),
        .0000001);
    assertEquals(
        4000L,
        registry.getSampleValue(
            "jvm_memory_pool_collection_init_bytes",
            new String[]{"pool"},
            new String[]{"PS Old Gen"}),
        .0000001);
  }

  @Test
  public void testMemoryAreas() {
    assertEquals(
        10L,
        registry.getSampleValue(
            "jvm_memory_objects_pending_finalization"),
        .0000001);
    assertEquals(
        500000L,
        registry.getSampleValue(
            "jvm_memory_bytes_used",
            new String[]{"area"},
            new String[]{"heap"}),
        .0000001);
    assertEquals(
        1000000L,
        registry.getSampleValue(
            "jvm_memory_bytes_committed",
            new String[]{"area"},
            new String[]{"heap"}),
        .0000001);
    assertEquals(
        2000000L,
        registry.getSampleValue(
            "jvm_memory_bytes_max",
            new String[]{"area"},
            new String[]{"heap"}),
        .0000001);
    assertEquals(
        1000L,
        registry.getSampleValue(
           "jvm_memory_bytes_init",
           new String[]{"area"},
           new String[]{"heap"}),
        .0000001);
    assertEquals(
        10000L,
        registry.getSampleValue(
            "jvm_memory_bytes_used",
            new String[]{"area"},
            new String[]{"nonheap"}),
        .0000001);
    assertEquals(
        20000L,
        registry.getSampleValue(
            "jvm_memory_bytes_committed",
            new String[]{"area"},
            new String[]{"nonheap"}),
        .0000001);
    assertEquals(
        3000000L,
        registry.getSampleValue(
            "jvm_memory_bytes_max",
            new String[]{"area"},
            new String[]{"nonheap"}),
        .0000001);
    assertEquals(
        2000L,
        registry.getSampleValue(
            "jvm_memory_bytes_init",
            new String[]{"area"},
            new String[]{"nonheap"}),
        .0000001);
  }
}
