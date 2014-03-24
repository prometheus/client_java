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
class Telemetry {
  @Register
  static final Gauge initializeTime = Gauge.newBuilder()
          .namespace("telemetry")
          .name("initialization_time_ms")
          .documentation("The time it took for the telemetry system to initialize.")
          .build();

  @Register
  static final Gauge serverStartTime = Gauge.newBuilder()
          .namespace("telemetry")
          .name("server_start_time_ms")
          .documentation("The time at which the server started.")
          .build();

  @Register
  static final Counter telemetryRequests = Counter.newBuilder()
          .namespace("telemetry")
          .name("requests_metrics_total")
          .documentation("A counter of the total requests made against the telemetry system.")
          .build();

  @Register
  static final Summary telemetryGenerationLatencies = Summary.newBuilder()
          .namespace("telemetry")
          .name("generation_latency_ms")
          .documentation("A histogram of telemetry generation latencies.")
          .targetQuantile(0.01, 0.05)
          .targetQuantile(0.05, 0.05)
          .targetQuantile(0.5, 0.05)
          .targetQuantile(0.9, 0.01)
          .targetQuantile(0.99, 0.001)
          .build();

  static {
    serverStartTime.newPartial().apply().set(System.currentTimeMillis() / 1000);
  }
}
