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

  private static final Gauge allocations = Gauge.newBuilder()
          .namespace("hotspot")
          .subsystem("allocation")
          .name("size_bytes")
          .labelNames("measure", "region")
          .documentation("Raw memory allocations partitioned by measurement and region type")
          .build();

  @Override
  public void run() {
    final MemoryUsage heapUsage = allocationBean.getHeapMemoryUsage();
    allocations.newPartial()
            .labelPair("measure", "initial")
            .labelPair("region", "heap")
            .apply()
            .set(heapUsage.getInit());
    allocations.newPartial()
            .labelPair("measure", "committed")
            .labelPair("region", "heap")
            .apply()
            .set(heapUsage.getCommitted());
    allocations.newPartial()
            .labelPair("measure", "used")
            .labelPair("region", "heap")
            .apply()
            .set(heapUsage.getUsed());
    allocations.newPartial()
            .labelPair("measure", "max")
            .labelPair("region", "heap")
            .apply()
            .set(heapUsage.getMax());

    final MemoryUsage nonHeap = allocationBean.getNonHeapMemoryUsage();
    allocations.newPartial()
            .labelPair("measure", "initial")
            .labelPair("region", "nonheap")
            .apply()
            .set(nonHeap.getInit());
    allocations.newPartial()
            .labelPair("measure", "committed")
            .labelPair("region", "nonheap")
            .apply()
            .set(nonHeap.getCommitted());
    allocations.newPartial()
            .labelPair("measure", "used")
            .labelPair("region", "nonheap")
            .apply()
            .set(nonHeap.getUsed());
    allocations.newPartial()
            .labelPair("measure", "max")
            .labelPair("region", "nonheap")
            .apply()
            .set(nonHeap.getMax());
  }
}
