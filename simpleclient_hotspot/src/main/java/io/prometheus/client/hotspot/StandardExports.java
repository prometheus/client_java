package io.prometheus.client.hotspot;

import com.sun.management.OperatingSystemMXBean;
import com.sun.management.UnixOperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import io.prometheus.client.Collector;

/**
 * Exports the standard exports common across all prometheus clients.
 * <p>
 * This includes stats like CPU time spent and memory usage.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new StandardExports().register();
 * }
 * </pre>
 */
public class StandardExports extends Collector {
  private static final Logger LOGGER = Logger.getLogger(StandardExports.class.getName());

  private final StatusReader statusReader;
  private final OperatingSystemMXBean osBean;
  private final RuntimeMXBean runtimeBean;
  private final boolean unix;
  private final boolean linux;

  public StandardExports() {
    this(new StatusReader(),
         (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean(),
         ManagementFactory.getRuntimeMXBean());
  }

  StandardExports(StatusReader statusReader, OperatingSystemMXBean osBean, RuntimeMXBean runtimeBean) {
      this.statusReader = statusReader;
      this.osBean = osBean;
      this.runtimeBean = runtimeBean;
      this.unix = (osBean instanceof UnixOperatingSystemMXBean);
      this.linux = (osBean.getName().indexOf("Linux") == 0);
  }

  private static MetricFamilySamples singleMetric(String name, Type type, String help, double value) {
    List<MetricFamilySamples.Sample> samples = Collections.singletonList(
        new MetricFamilySamples.Sample(name, Collections.<String>emptyList(), Collections.<String>emptyList(), value));
    return new MetricFamilySamples(name, type, help, samples);
  }

  private final static double KB = 1024;

  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();

    mfs.add(singleMetric("process_cpu_seconds_total", Type.COUNTER, "Total user and system CPU time spent in seconds.",
        osBean.getProcessCpuTime() / NANOSECONDS_PER_SECOND));

    mfs.add(singleMetric("process_start_time_seconds", Type.GAUGE, "Start time of the process since unix epoch in seconds.",
        runtimeBean.getStartTime() / MILLISECONDS_PER_SECOND));

    if (unix) {
      UnixOperatingSystemMXBean unixBean = (UnixOperatingSystemMXBean) osBean;
      mfs.add(singleMetric("process_open_fds", Type.GAUGE, "Number of open file descriptors.",
          unixBean.getOpenFileDescriptorCount()));
      mfs.add(singleMetric("process_max_fds", Type.GAUGE, "Maximum number of open file descriptors.",
          unixBean.getMaxFileDescriptorCount()));
    }

    // There's no standard Java or POSIX way to get memory stats,
    // so add support for just Linux for now.
    if (linux) {
      try {
      collectMemoryMetricsLinux(mfs);
      } catch (Exception e) {
        // If the format changes, log a warning and return what we can.
        LOGGER.warning(e.toString());
      }
    }
    return mfs;
  }

  void collectMemoryMetricsLinux(List<MetricFamilySamples> mfs) {
    // statm/stat report in pages, and it's non-trivial to get pagesize from Java
    // so we parse status instead.
    BufferedReader br = null;
    try {
      br = statusReader.procSelfStatusReader();
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("VmSize:")) {
          mfs.add(singleMetric("process_virtual_memory_bytes", Type.GAUGE,
              "Virtual memory size in bytes.",
              Float.parseFloat(line.split("\\s+")[1]) * KB));
        } else if (line.startsWith("VmRSS:")) {
          mfs.add(singleMetric("process_resident_memory_bytes", Type.GAUGE,
              "Resident memory size in bytes.",
              Float.parseFloat(line.split("\\s+")[1]) * KB));
        }
      }
    } catch (IOException e) {
      LOGGER.fine(e.toString());
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          LOGGER.fine(e.toString());
        }
      }
    }
  }

  static class StatusReader {
    BufferedReader procSelfStatusReader() throws FileNotFoundException {
      return new BufferedReader(new FileReader("/proc/self/status"));
    }
  }
}
