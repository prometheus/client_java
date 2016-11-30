package io.prometheus.client;

/**
 * Listeners for events from the registry. Listeners must be thread-safe.
 */
public interface CollectorRegistryListener {

  /**
   * Called when a {@link Collector} is registered to the registry.
   *
   * @param collector the collector
   */
  void onCollectorRegistered(Collector collector);

  /**
   * Called when a {@link Collector} is unregistered from the registry.
   *
   * @param collector the collector
   */
  void onCollectorUnregistered(Collector collector);

  /**
   * A no-op implementation of {@link CollectorRegistryListener}.
   */
  abstract class BaseListener implements CollectorRegistryListener {
    @Override
    public void onCollectorRegistered(Collector collector) {}

    @Override
    public void onCollectorUnregistered(Collector collector) {}
  }

}
