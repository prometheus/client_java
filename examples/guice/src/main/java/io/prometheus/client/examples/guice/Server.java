package io.prometheus.client.examples.guice;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.prometheus.client.Prometheus;
import io.prometheus.client.examples.guice.servlet.BarServlet;
import io.prometheus.client.examples.guice.servlet.FooServlet;
import io.prometheus.client.utility.servlet.MetricsServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.Set;


/**
 * <p>An example encapsulation for a full end-to-end server.</p>
 */
@Singleton
public class Server {
  private final org.eclipse.jetty.server.Server server;
  private final FooServlet foo;
  private final BarServlet bar;
  private final Set<Prometheus.ExpositionHook> hooks;

  @Inject
  public Server(org.eclipse.jetty.server.Server server, final FooServlet foo,
      final BarServlet bar, final Set<Prometheus.ExpositionHook> hooks) {
    this.server = server;
    this.foo = foo;
    this.bar = bar;
    this.hooks = hooks;
  }

  /**
   * <p>
   * We are optionally demonstrating the ability to dynamically add extra {@link
   * io.prometheus.client.Prometheus.ExpositionHook} via Guice.  It is possible to add
   * registrations in static blocks, but here we are using Guice's {@link
   * com.google.inject.multibindings.Multibinder} capability to provision some hooks.
   *</p>
   */
  public void configure() {
    for (final Prometheus.ExpositionHook hook : hooks) {
      Prometheus.defaultAddPreexpositionHook(hook);
    }

    Prometheus.defaultInitialize();

    final ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    server.setHandler(context);
    context.addServlet(new ServletHolder(foo), "/foo");
    context.addServlet(new ServletHolder(bar), "/bar");
    context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
  }

  public void run() {
    try {
      server.start();  // Throws Exception — really?
      server.join();
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
