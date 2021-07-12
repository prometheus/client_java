package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.CompilationMXBean;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class CompilationExportsTest {

  private CompilationMXBean mockCompilationsBean = Mockito.mock(CompilationMXBean.class);
  private CollectorRegistry registry = new CollectorRegistry();
  private CompilationExports collectorUnderTest;

  private static final String[] EMPTY_LABEL = new String[0];

  @Before
  public void setUp() {
    when(mockCompilationsBean.getTotalCompilationTime()).thenReturn(TimeUnit.SECONDS.toMillis(10));
    collectorUnderTest = new CompilationExports(mockCompilationsBean).register(registry);
  }

  @Test
  public void testCompilation() {
    assertEquals(
            10d,
            registry.getSampleValue(
                    "jvm_compilation_time_total", EMPTY_LABEL, EMPTY_LABEL),
            .0000001);
  }
}
