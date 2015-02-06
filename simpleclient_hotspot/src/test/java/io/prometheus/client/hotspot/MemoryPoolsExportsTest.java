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


public class MemoryPoolsExportsTest {

  private MemoryPoolMXBean mockPoolsBean1 = Mockito.mock(MemoryPoolMXBean.class);
  private MemoryPoolMXBean mockPoolsBean2 = Mockito.mock(MemoryPoolMXBean.class);
  private MemoryMXBean mockMemoryBean = Mockito.mock(MemoryMXBean.class);
  private MemoryUsage mockUsage1 = Mockito.mock(MemoryUsage.class);
  private MemoryUsage mockUsage2 = Mockito.mock(MemoryUsage.class);
  private List<MemoryPoolMXBean> mockList = Arrays.asList(mockPoolsBean1, mockPoolsBean2);
  private CollectorRegistry registry = new CollectorRegistry();
  private MemoryPoolsExports collectorUnderTest;

  @Before
  public void setUp() {
    Mockito.when(mockPoolsBean1.getName()).thenReturn("PS Eden Space");
    Mockito.when(mockPoolsBean1.getUsage()).thenReturn(mockUsage1);
    Mockito.when(mockPoolsBean2.getName()).thenReturn("PS Old Gen");
    Mockito.when(mockPoolsBean2.getUsage()).thenReturn(mockUsage2);
    Mockito.when(mockMemoryBean.getHeapMemoryUsage()).thenReturn(mockUsage1);
    Mockito.when(mockMemoryBean.getNonHeapMemoryUsage()).thenReturn(mockUsage2);
    Mockito.when(mockUsage1.getUsed()).thenReturn(500000L);
    Mockito.when(mockUsage1.getMax()).thenReturn(1000000L);
    Mockito.when(mockUsage2.getUsed()).thenReturn(10000L);
    Mockito.when(mockUsage2.getMax()).thenReturn(-1L);  // will use committed instead
    Mockito.when(mockUsage2.getCommitted()).thenReturn(20000L);
    collectorUnderTest = new MemoryPoolsExports(mockMemoryBean, mockList).register(registry);
  }

  @Test
  public void testMemoryPools() {
    assertEquals(
        500000L,
        registry.getSampleValue(
            MemoryPoolsExports.POOLS_USED_METRIC,
            new String[]{"pool"},
            new String[]{"PS-Eden-Space"}),
        .0000001);
    assertEquals(
        1000000L,
        registry.getSampleValue(
            MemoryPoolsExports.POOLS_LIMIT_METRIC,
            new String[]{"pool"},
            new String[]{"PS-Eden-Space"}),
        .0000001);
    assertEquals(
        10000L,
        registry.getSampleValue(
            MemoryPoolsExports.POOLS_USED_METRIC,
            new String[]{"pool"},
            new String[]{"PS-Old-Gen"}),
        .0000001);
    assertEquals(
        20000L,
        registry.getSampleValue(
            MemoryPoolsExports.POOLS_LIMIT_METRIC,
            new String[]{"pool"},
            new String[]{"PS-Old-Gen"}),
        .0000001);
  }

  @Test
  public void testMemoryAreas() {
    assertEquals(
        500000L,
        registry.getSampleValue(
            MemoryPoolsExports.MEMORY_USED_METRIC,
            new String[]{"area"},
            new String[]{"heap"}),
        .0000001);
    assertEquals(
        1000000L,
        registry.getSampleValue(
            MemoryPoolsExports.MEMORY_LIMIT_METRIC,
            new String[]{"area"},
            new String[]{"heap"}),
        .0000001);
    assertEquals(
        10000L,
        registry.getSampleValue(
            MemoryPoolsExports.MEMORY_USED_METRIC,
            new String[]{"area"},
            new String[]{"nonheap"}),
        .0000001);
    assertEquals(
        20000L,
        registry.getSampleValue(
            MemoryPoolsExports.MEMORY_LIMIT_METRIC,
            new String[]{"area"},
            new String[]{"nonheap"}),
        .0000001);
  }
}
