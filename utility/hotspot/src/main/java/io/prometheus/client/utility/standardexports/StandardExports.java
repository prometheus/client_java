/*
 * Copyright 2014 Prometheus Team Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.prometheus.client.utility.standardexports;

import com.sun.management.OperatingSystemMXBean;
import com.sun.management.UnixOperatingSystemMXBean;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ManagementFactory;
import java.util.logging.Logger;

import io.prometheus.client.Prometheus;
import io.prometheus.client.metrics.Counter;
import io.prometheus.client.metrics.Gauge;

/**
 * This class exports the standard exports common across all
 * prometheus clients (as far as practical). This includes stats
 * like CPU time and memory.
 *
 */
public class StandardExports implements Prometheus.ExpositionHook {
  private static final Logger log = Logger.getLogger(StandardExports.class.getName());

  private static OperatingSystemMXBean osBean;
  private static RuntimeMXBean runtimeBean;

  private static Counter processCpuSeconds;
  private static Gauge processStartTimeSeconds;
  private static Gauge openFds;
  private static Gauge maxFds;

  private static Gauge virtualMemoryBytes;
  private static Gauge residentMemoryBytes;

  public StandardExports() {
    if (osBean == null) {
      osBean = (com.sun.management.OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
      runtimeBean = ManagementFactory.getRuntimeMXBean();

      processCpuSeconds = Counter.newBuilder()
        .namespace("process")
        .name("cpu_seconds_total")
        .documentation("CPU time used by the process in seconds.")
        .build();
      processStartTimeSeconds = Gauge.newBuilder()
        .namespace("process")
        .name("start_time_seconds")
        .documentation("Start time of the process, in unixtime.")
        .build();
    }

    if (osBean instanceof UnixOperatingSystemMXBean && openFds == null) {
      openFds = Gauge.newBuilder()
        .namespace("process")
        .name("open_fds")
        .documentation("The number of open file descriptors.")
        .build();
      maxFds = Gauge.newBuilder()
        .namespace("process")
        .name("max_fds")
        .documentation("The maximum number of open file descriptors.")
        .build();
    }

    // There's no standard Java or POSIX way to get these,
    // so add support for just Linux for now.
    if (osBean.getName().indexOf("Linux") == 0 && virtualMemoryBytes == null) {
      virtualMemoryBytes = Gauge.newBuilder()
        .namespace("process")
        .name("virtual_memory_bytes")
        .documentation("The virtual memory size of the process, in bytes.")
        .build();
      residentMemoryBytes = Gauge.newBuilder()
        .namespace("process")
        .name("resident_memory_bytes")
        .documentation("The amount of resident memory of the process, in bytes.")
        .build();
    }
  }

  private final static double NANOSECONDS_PER_SECOND = 10E9;
  private final static double MILLISECONDS_PER_SECOND = 10E3;

  @Override
  public void run() {
    processCpuSeconds.newPartial().apply().set(osBean.getProcessCpuTime() / NANOSECONDS_PER_SECOND);
    processStartTimeSeconds.newPartial().apply().set(runtimeBean.getStartTime() / MILLISECONDS_PER_SECOND);

    if (openFds != null) {
      UnixOperatingSystemMXBean unixBean = (UnixOperatingSystemMXBean)osBean;
      openFds.newPartial().apply().set(unixBean.getOpenFileDescriptorCount());
      maxFds.newPartial().apply().set(unixBean.getMaxFileDescriptorCount());
    }

    if (virtualMemoryBytes != null) {
      // statm/stat report in pages, and it's non-trivial to get pagesize from Java
      // so we parse status instead.
      BufferedReader br = null;
      try {
        br = new BufferedReader(new FileReader("/proc/self/status"));
        String line;
        while ((line = br.readLine()) != null) {
          // Values are in kB.
          if (line.startsWith("VmSize:")) { 
            virtualMemoryBytes.newPartial().apply().set(Float.parseFloat(line.split(" +")[1]) * 1024);
          } else if (line.startsWith("VmRSS:")) { 
            residentMemoryBytes.newPartial().apply().set(Float.parseFloat(line.split(" +")[1]) * 1024);
          }
          
        }
      } catch (IOException e) {
        log.fine(e.toString());
      } finally {
        if (br != null) {
          try {
            br.close(); 
          } catch (IOException e) {
          }
        }
      }
    }
  }
}
