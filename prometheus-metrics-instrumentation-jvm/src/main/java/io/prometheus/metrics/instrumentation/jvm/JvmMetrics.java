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
   * Register the default Hotspot collectors with the default
   * registry. It is safe to call this method multiple times, as
   * this will only register the collectors once.
   */
  public static synchronized void initialize() {
    if (!initialized) {
      register(CollectorRegistry.defaultRegistry);
      initialized = true;
    }
  }

  /**
   * Register the default Hotspot collectors with the given registry.
   */
  public static void register(CollectorRegistry registry) {
    new BufferPoolsExports().register(registry);
    new ClassLoadingExports().register(registry);
    new CompilationExports().register(registry);
    new GarbageCollectorExports().register(registry);
    new MemoryAllocationExports().register(registry);
    new MemoryPoolsExports().register(registry);
    new StandardExports().register(registry);
    new ThreadExports().register(registry);
    new VersionInfoExports().register(registry);
  }
}
