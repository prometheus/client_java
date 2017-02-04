package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;

/**
 * Registers the default Hotspot collectors.
 * <p>
 * This is intended to avoid users having to add in new
 * registrations every time a new exporter is added.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   DefaultExports.initialize();
 * }
 * </pre>
 */
public class DefaultExports {
  private static boolean initialized = false;

  /**
   * Register the default Hotspot collectors with the default registry.
   */
  public static synchronized void initialize() {
    initialize(CollectorRegistry.defaultRegistry);
  }

  /**
   * Register the default Hotspot collectors with the given registry.
   */
  public static synchronized void initialize(CollectorRegistry registry) {
    if (!initialized) {
      new StandardExports().register(registry);
      new MemoryPoolsExports().register(registry);
      new GarbageCollectorExports().register(registry);
      new ThreadExports().register(registry);
      new ClassLoadingExports().register(registry);
      initialized = true;
    }
  }

}
