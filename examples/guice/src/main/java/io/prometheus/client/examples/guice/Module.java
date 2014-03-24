package io.prometheus.client.examples.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import io.prometheus.client.Prometheus;
import io.prometheus.client.metrics.Counter;
import io.prometheus.client.metrics.Summary;
import org.eclipse.jetty.server.Server;

public class Module extends AbstractModule {
  private static final Counter.Builder COUNTER_PROTOTYPE = Counter.newBuilder()
      .namespace("guice_example");
  private static final Summary.Builder SUMMARY_PROTOTYPE = Summary.newBuilder()
      .namespace("guice_example");

  @Override
  protected void configure() {
    bind(Integer.class)
        .annotatedWith(Names.named("port"))
        .toInstance(8080);
    bind(Counter.class)
        .annotatedWith(Names.named("handler"))
        .toProvider(HandlerCounterProvider.class)
        .in(Singleton.class);
    bind(Summary.class)
        .annotatedWith(Names.named("handler"))
        .toProvider(HandlerLatencyProvider.class)
        .in(Singleton.class);

    final Multibinder<Prometheus.ExpositionHook> hooks = Multibinder.newSetBinder(binder(),
        Prometheus.ExpositionHook.class);
  }

  @Provides
  @Singleton
  public Server getServer(final @Named("port") Integer port) {
    return new Server(port);
  }

  public static class HandlerCounterProvider implements Provider<Counter> {
    @Override
    public Counter get() {
      // N.B.: Static registration!
      return COUNTER_PROTOTYPE
          .subsystem("http")
          .name("requests_total")
          .labelNames("handler")
          .labelNames("result")
          .documentation("The total number of requests served.")
          .build();
    }
  }

  public static class HandlerLatencyProvider implements Provider<Summary> {
    @Override
    public Summary get() {
      // N.B.: Static registration!
      return SUMMARY_PROTOTYPE
          .subsystem("http")
          .name("latency_ms")
          .labelNames("handler")
          .labelNames("result")
          .documentation("The latencies of the requests served.")
          .build();
    }
  }
}
