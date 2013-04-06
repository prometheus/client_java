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
import io.prometheus.client.metrics.Histogram;
import io.prometheus.client.metrics.builtin.JvmMetrics;
import io.prometheus.client.metrics.histogram.Bucket;
import io.prometheus.client.metrics.histogram.BucketBuilder;
import io.prometheus.client.metrics.histogram.buckets.AccumulatingBucket;
import io.prometheus.client.metrics.histogram.buckets.Distributions;
import io.prometheus.client.metrics.histogram.buckets.EvictionPolicies;

/**
 * <p>
 * Standard telemetry for all Prometheus clients.
 * </p>
 * 
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class Telemetry {
  @Register(name = "instance_start_time_seconds", docstring = "The time at which the current "
      + "instance started (seconds UTC).", baseLabels = {})
  private static final Gauge startTime = new Gauge();
  @Register(name = "telemetry_initialization_time_ms", docstring = "The time it took for the "
      + "telemetry system to initialize (ms).", baseLabels = {})
  static final Counter telemetryInitializationTime = new Counter();
  @Register(name = "telemetry_requests_metrics_total", docstring = "A counter of the total "
      + "requests made against the telemetry system.", baseLabels = {})
  static final Counter telemetryRequests = new Counter();
  @Register(name = "telemetry_generation_latency_ms", docstring = "A histogram of telemetry "
      + "generation latencies (ms).", baseLabels = {})
  static final Histogram telemetryGenerationLatencies = new Histogram(new float[] {0.01f, 0.05f,
      0.5f, 0.90f, 0.99f}, Distributions.logarithmicSizedBucketsFor(0, 5000), new BucketBuilder() {
    @Override
    public Bucket newBucket() {
      return new AccumulatingBucket(EvictionPolicies.evictOldest(100), 500);
    }
  });

  static {
    startTime.set(Registry.emptyLabels(), (float) (System.currentTimeMillis() / 1000));
  }

  public static final void updateStandard() {
    JvmMetrics.update();
  }
}
