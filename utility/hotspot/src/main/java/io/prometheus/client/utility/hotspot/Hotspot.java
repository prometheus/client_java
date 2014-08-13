/*
 * Copyright 2013 Prometheus Team Licensed under the Apache License, Version 2.0
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

package io.prometheus.client.utility.hotspot;

import io.prometheus.client.Prometheus;
import io.prometheus.client.metrics.Gauge;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * <p>
 * {@link Hotspot} provides HotSpot-specific JVM metrics.
 * </p>
 *
 * <p>
 * TODO(matt): There is a lot that can be added here with a day's work:
 * <ul>
 *     <li>Managed Memory by Generation</li>
 *     <li>Which Garbage Collector is Used by What Generation</li>
 *     <li>Concurrent Mode Failures</li>
 *     <li>Promotion Failures</li>
 *     <li>Thread Statistics</li>
 *     <li>Lock Statistics</li>
 * </ul>
 * </p>
 *
 * @author Matt T. Proud (matt.proud@gmail.com)
 * @see Prometheus#addPreexpositionHook(io.prometheus.client.Prometheus.ExpositionHook)
 */
public class Hotspot implements Prometheus.ExpositionHook {
  private static final MemoryMXBean allocationBean = ManagementFactory.getMemoryMXBean();

  private static final Gauge initialAllocation = Gauge.newBuilder()
          .namespace("hotspot")
          .subsystem("allocation")
          .name("initial_size_bytes")
          .labelNames("region")
          .documentation("Initial memory allocation partitioned by region type")
          .build();
  private static final Gauge committedAllocation = Gauge.newBuilder()
          .namespace("hotspot")
          .subsystem("allocation")
          .name("committed_size_bytes")
          .labelNames("region")
          .documentation("Commited memory allocation partitioned by region type")
          .build();
  private static final Gauge usedAllocation = Gauge.newBuilder()
          .namespace("hotspot")
          .subsystem("allocation")
          .name("used_size_bytes")
          .labelNames("region")
          .documentation("Used memory allocation partitioned by region type")
          .build();
  private static final Gauge maxAllocation = Gauge.newBuilder()
          .namespace("hotspot")
          .subsystem("allocation")
          .name("max_size_bytes")
          .labelNames("region")
          .documentation("Max memory allocation partitioned by region type")
          .build();

  @Override
  public void run() {
    final MemoryUsage heapUsage = allocationBean.getHeapMemoryUsage();
    initialAllocation.newPartial()
            .labelPair("region", "heap")
            .apply()
            .set(heapUsage.getInit());
    committedAllocation.newPartial()
            .labelPair("region", "heap")
            .apply()
            .set(heapUsage.getCommitted());
    usedAllocation.newPartial()
            .labelPair("region", "heap")
            .apply()
            .set(heapUsage.getUsed());
    maxAllocation.newPartial()
            .labelPair("region", "heap")
            .apply()
            .set(heapUsage.getMax());

    final MemoryUsage nonHeapUsage = allocationBean.getNonHeapMemoryUsage();
    initialAllocation.newPartial()
            .labelPair("region", "nonheap")
            .apply()
            .set(nonHeapUsage.getInit());
    committedAllocation.newPartial()
            .labelPair("region", "nonheap")
            .apply()
            .set(nonHeapUsage.getCommitted());
    usedAllocation.newPartial()
            .labelPair("region", "nonheap")
            .apply()
            .set(nonHeapUsage.getUsed());
    maxAllocation.newPartial()
            .labelPair("region", "nonheap")
            .apply()
            .set(nonHeapUsage.getMax());
  }
}
