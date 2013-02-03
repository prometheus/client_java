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

package io.prometheus.client.examples.random;

import com.google.common.collect.ImmutableMap;
import io.prometheus.client.Register;
import io.prometheus.client.Registry;
import io.prometheus.client.metrics.Counter;
import io.prometheus.client.metrics.Histogram;
import io.prometheus.client.metrics.histogram.buckets.AccumulatingBucket;
import io.prometheus.client.metrics.histogram.buckets.Distributions;
import io.prometheus.client.metrics.histogram.buckets.EvictionPolicies;
import io.prometheus.client.metrics.histogram.buckets.ReductionMethods;
import io.prometheus.client.utility.servlet.MetricsServlet;
import org.apache.commons.math3.random.RandomDataImpl;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class Main {
  @Register(name = "rpc_calls_total", docstring = "RPC calls.", baseLabels = {})
  private static final Counter rpcCalls = new Counter();
  @Register(name = "rpc_latency_microseconds", docstring = "RPC latency.", baseLabels = {})
  private static final Histogram rpcLatency = new Histogram(new float[] {0.01f, 0.05f, 0.5f, 0.90f,
      0.99f}, Distributions.equallySizedBucketsFor(0, 200, 4),
      new AccumulatingBucket.BucketBuilder(EvictionPolicies.evictAndReplaceWith(10,
          ReductionMethods.average), 50));

  public static void main(final String[] arguments) {
    Registry.defaultInitialize();
    final Server server = new Server(8181);
    final ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    server.setHandler(context);
    context.addServlet(new ServletHolder(new MetricsServlet()), "/");

    new Thread() {
      @Override
      public void run() {
        final RandomDataImpl randomData = new RandomDataImpl();

        try {
          while (true) {
            rpcLatency.add(ImmutableMap.of("service", "foo"), randomData.nextLong(0, 200));
            rpcLatency.add(ImmutableMap.of("service", "bar"),
                (float) randomData.nextGaussian(100, 20));
            rpcLatency.add(ImmutableMap.of("service", "zed"),
                (float) randomData.nextExponential(100));
            rpcCalls.increment(ImmutableMap.of("service", "foo"));
            rpcCalls.increment(ImmutableMap.of("service", "bar"));
            rpcCalls.increment(ImmutableMap.of("service", "zed"));
            Thread.sleep(1000);
          }
        } catch (final InterruptedException e) {
        }
      }
    }.start();

    try {
      server.start();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
