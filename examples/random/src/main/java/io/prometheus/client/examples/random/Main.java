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

import io.prometheus.client.Prometheus;
import io.prometheus.client.Register;
import io.prometheus.client.metrics.Counter;
import io.prometheus.client.metrics.Summary;
import io.prometheus.client.utility.servlet.MetricsServlet;
import org.apache.commons.math3.random.RandomDataImpl;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.concurrent.TimeUnit;

/**
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
public class Main {
  private static final Counter rpcCalls = Counter.newBuilder()
          .namespace("rpc")
          .name("calls_total")
          .documentation("The total number of RPC calls partitioned by RPC service.")
          .build();

  private static final Summary rpcLatency = Summary.newBuilder()
          .namespace("rpc")
          .name("latency_microseconds")
          .documentation("RPC latency partitioned by RPC service.")
          .targetQuantile(0.01, 0.001)
          .targetQuantile(0.05, 0.025)
          .targetQuantile(0.50, 0.05)
          .targetQuantile(0.90, 0.01)
          .targetQuantile(0.99, 0.001)
          .purgeInterval(2, TimeUnit.MINUTES)
          .build();

  public static void main(final String[] arguments) {
    Prometheus.defaultInitialize();
    final Server server = new Server(8080);
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
            rpcLatency.newPartial()
                    .labelPair("service", "foo")
                    .apply()
                    .observe((double) randomData.nextLong(0, 200));
            rpcLatency.newPartial()
                    .labelPair("service", "bar")
                    .apply()
                    .observe(randomData.nextGaussian(100, 20));
            rpcLatency.newPartial()
                    .labelPair("service", "zed")
                    .apply()
                    .observe((double) randomData.nextExponential(100));

            rpcCalls.newPartial()
                    .labelPair("service", "foo")
                    .apply()
                    .increment();
            rpcCalls.newPartial()
                    .labelPair("service", "bar")
                    .apply()
                    .increment();
            rpcCalls.newPartial()
                    .labelPair("service", "zed")
                    .apply()
                    .increment();

            Thread.sleep(250);
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
