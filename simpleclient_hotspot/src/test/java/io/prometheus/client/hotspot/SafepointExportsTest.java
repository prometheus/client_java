package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import sun.management.HotspotRuntimeMBean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class SafepointExportsTest {

  private HotspotRuntimeMBean mockHotspotRuntimeBean = Mockito.mock(HotspotRuntimeMBean.class);
  private CollectorRegistry registry = new CollectorRegistry();
  private SafepointExports collectorUnderTest;

  private static final String[] EMPTY_LABEL = new String[0];


  @Before
  public void setUp() {
    when(mockHotspotRuntimeBean.getSafepointCount()).thenReturn(300L);
    when(mockHotspotRuntimeBean.getTotalSafepointTime()).thenReturn(13L);
    when(mockHotspotRuntimeBean.getSafepointSyncTime()).thenReturn(31L);
    collectorUnderTest = new SafepointExports(mockHotspotRuntimeBean).register(registry);
  }

  @Test
  public void testSafepoints() {
    assertEquals(
            300L,
            registry.getSampleValue(
                    "jvm_safepoint_seconds_count", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
    assertEquals(
            0.013,
            registry.getSampleValue(
                    "jvm_safepoint_seconds_sum", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
    assertEquals(
            0.031,
            registry.getSampleValue(
                    "jvm_safepoint_sync_time_seconds", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
  }
}
