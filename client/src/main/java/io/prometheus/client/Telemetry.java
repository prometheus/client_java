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

package io.prometheus.client;

import io.prometheus.client.metrics.Counter;
import io.prometheus.client.metrics.Gauge;
import io.prometheus.client.metrics.Summary;

/**
 * <p>
 * Standard telemetry for all Prometheus clients.
 * </p>
 * 
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class Telemetry {
  @Register
  private static final Gauge startTime = Gauge.builder().inNamespace("telemetry")
      .named("initialization_time_ms")
      .documentedAs("The time it took for the telemetry system to initialize (ms).").build();

  @Register
  static final Gauge telemetryInitializationTime = Gauge.builder().inNamespace("telemetry")
      .named("initialization_time_ms")
      .documentedAs("The time it took for the telemetry system to initialize (ms).").build();

  @Register
  static final Counter telemetryRequests = Counter.builder().inNamespace("telemetry")
      .named("requests_metrics_total")
      .documentedAs("A counter of the total requests made against the telemetry system.").build();

  @Register
  static final Summary telemetryGenerationLatencies = Summary.builder().inNamespace("telemetry")
      .named("generation_latency_ms")
      .documentedAs("A histogram of telemetry generation latencies (ms).").withTarget(0.01, 0.05)
      .withTarget(0.05, 0.05).withTarget(0.5, 0.05).withTarget(0.9, 0.01).withTarget(0.99, 0.001)
      .build();

  static {
    startTime.newPartial().apply().set(System.currentTimeMillis() / 1000);
  }
}
