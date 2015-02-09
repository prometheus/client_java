package io.prometheus.client.hotspot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import com.sun.management.UnixOperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.FileNotFoundException;
import java.util.Vector;
import java.lang.management.RuntimeMXBean;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class StandardExportsTest {

  static class StatusReaderTest extends StandardExports.StatusReader {
    BufferedReader procSelfStatusReader() throws FileNotFoundException {
      return new BufferedReader(new StringReader("Name:   cat\nVmSize:\t5900 kB\nVmRSS:\t   360 kB\n"));
    }
  }

  UnixOperatingSystemMXBean osBean;
  RuntimeMXBean runtimeBean;

  @Before
  public void setUp() {
    osBean = Mockito.mock(UnixOperatingSystemMXBean.class);
    when(osBean.getName()).thenReturn("Linux");
    when(osBean.getProcessCpuTime()).thenReturn(123L);
    when(osBean.getOpenFileDescriptorCount()).thenReturn(10L);
    when(osBean.getMaxFileDescriptorCount()).thenReturn(20L);
    runtimeBean = Mockito.mock(RuntimeMXBean.class);
    when(runtimeBean.getStartTime()).thenReturn(456L);
  }

  @Test
  public void testStandardExports() {
    CollectorRegistry registry = new CollectorRegistry();
    new StandardExports(new StatusReaderTest(), osBean, runtimeBean).register(registry);

    assertEquals(123 / 1.0E9,
        registry.getSampleValue("process_cpu_seconds_total", new String[]{}, new String[]{}), .0000001);
    assertEquals(10,
        registry.getSampleValue("process_open_fds", new String[]{}, new String[]{}), .001);
    assertEquals(20,
        registry.getSampleValue("process_max_fds", new String[]{}, new String[]{}), .001);
    assertEquals(456 / 1.0E3,
        registry.getSampleValue("process_start_time_seconds", new String[]{}, new String[]{}), .0000001);
    assertEquals(5900 * 1024,
        registry.getSampleValue("process_virtual_memory_bytes", new String[]{}, new String[]{}), .001);
    assertEquals(360 * 1024,
        registry.getSampleValue("process_resident_memory_bytes", new String[]{}, new String[]{}), .001);
  }

  @Test
  public void testBrokenProcStatusReturnsOtherStats() {
    class StatusReaderBroken extends StandardExports.StatusReader {
      BufferedReader procSelfStatusReader() throws FileNotFoundException {
        return new BufferedReader(new StringReader("Name:   cat\nVmSize:\n"));
      }
    }

    CollectorRegistry registry = new CollectorRegistry();
    new StandardExports(new StatusReaderBroken(), osBean, runtimeBean).register(registry);

    assertEquals(123 / 1.0E9,
      registry.getSampleValue("process_cpu_seconds_total", new String[]{}, new String[]{}), .0000001);
    assertNull(registry.getSampleValue("process_resident_memory_bytes", new String[]{}, new String[]{}));
  }
}
