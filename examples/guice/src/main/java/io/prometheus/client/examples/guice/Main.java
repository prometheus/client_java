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

package io.prometheus.client.examples.guice;


import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;


/**
 * <p>This Maven module demonstrates Prometheus integration with Google's Guice framework as
 * well as more advanced use of prepopulated {@link io.prometheus.client.metrics.Metric.Builder}
 * and {@link io.prometheus.client.metrics.Metric.Partial} constructs.</p>
 *
 * <p>
 * In this case, we demonstrate optional provisioning of {@link io.prometheus.client.metrics.Metric}
 * through Guice {@link com.google.inject.Provider} in {@link com.google.inject.Module}.  What is
 * interesting to note is that we have a fictional HTTP server with <em>two HTTP {@link
 * javax.servlet.Servlet} handlers that share the same two fundamental metrics defined in {@link
 * io.prometheus.client.examples.guice.Module.HandlerCounterProvider} and {@link
 * io.prometheus.client.examples.guice.Module.HandlerLatencyProvider}</em>.  These two metrics
 * track a typical use case of measuring request counts and latency quantiles.  The metrics share
 * the same schema and metric name, but <em>the handlers can be differentiated by label values
 * </em>.
 * </p>
 *
 * <p>
 * This example is unremarkable in the sense that manual passing of partially-fabricated metrics
 * is possible to do by hand as well, and it is easy.  In the interests of minimizing static state
 * and promoting testability, teams may decide to use dependency injection for provisioning of
 * metrics that are shared between multiple classes, <em>though this is discouraged since except
 * for a few rare cases it demonstrates violation of law of demeter and separation of concerns
 * </em>.
 * </p>
 * @author matt.proud@gmail.com (Matt T. Proud)
 */
@Singleton
public class Main {
  private final Server server;

  @Inject
  public Main(final Server server) {
    this.server = server;
  }

  public void run() {
    server.configure();
    server.run();
  }

  public static void main(final String[] arguments) {
    final Injector injector = Guice.createInjector(new Module());
    final Main main = injector.getInstance(Main.class);

    main.run();
  }
}
