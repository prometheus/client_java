package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.ClassLoadingMXBean;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ClassLoadingExportsTest {

  private ClassLoadingMXBean mockClassLoadingsBean = Mockito.mock(ClassLoadingMXBean.class);
  private CollectorRegistry registry = new CollectorRegistry();
  private ClassLoadingExports collectorUnderTest;

  private static final String[] EMPTY_LABEL = new String[0];

  @Before
  public void setUp() {
    when(mockClassLoadingsBean.getLoadedClassCount()).thenReturn(1000);
    when(mockClassLoadingsBean.getTotalLoadedClassCount()).thenReturn(2000L);
    when(mockClassLoadingsBean.getUnloadedClassCount()).thenReturn(500L);
    collectorUnderTest = new ClassLoadingExports(mockClassLoadingsBean).register(registry);
  }

  @Test
  public void testClassLoading() {
    assertEquals(
            1000,
            registry.getSampleValue(
                    "jvm_classes_loaded", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
    assertEquals(
            2000L,
            registry.getSampleValue(
                    "jvm_classes_loaded_total", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
    assertEquals(
            500L,
            registry.getSampleValue(
                    "jvm_classes_unloaded_total", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
  }
}
