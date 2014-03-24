package io.prometheus.client.examples.jvmstat;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import io.prometheus.client.Prometheus;
import io.prometheus.client.utility.jvmstat.JvmstatMonitor;

public class Module extends AbstractModule {
  @Override
  protected void configure() {
    // See io.prometheus.client.examples.guice.Server#configure.
    final Multibinder<Prometheus.ExpositionHook> hooks = Multibinder.newSetBinder(binder(),
        Prometheus.ExpositionHook.class);
    hooks.addBinding().to(JvmstatMonitor.class).in(Singleton.class);
  }
}
