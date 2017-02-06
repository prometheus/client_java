package io.prometheus.client.hotspot;

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
   * Register the default Hotspot collectors.
   */
  public static synchronized void initialize() {
    if (!initialized) {
      new StandardExports().register();
      new MemoryPoolsExports().register();
      new GarbageCollectorExports().register();
      new ThreadExports().register();
      new ClassLoadingExports().register();
      new VersionInfoExports().register();
      initialized = true;
    }
  }

}
