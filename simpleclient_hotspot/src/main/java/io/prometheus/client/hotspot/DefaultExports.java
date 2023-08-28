package io.prometheus.client.hotspot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.prometheus.client.Collector;
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
  
  private final static List<Collector> defaultCollectors = Collections.unmodifiableList(Arrays.asList(
        new BufferPoolsExports(),
        new ClassLoadingExports(),
        new CompilationExports(),
        new GarbageCollectorExports(),
        new MemoryAllocationExports(),
        new MemoryPoolsExports(),
        new StandardExports(),
        new ThreadExports(),
        new VersionInfoExports()
      ));

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
   * Release the resources used by the default Hotspot
   * collectors. It is safe to call this method multiple times,
   * as this will unregister all collectors once.
   */
  public static synchronized void terminate() {
    if (initialized) {
      unregister(CollectorRegistry.defaultRegistry);
      initialized = false;
    }
  }

  /**
   * Register the default Hotspot collectors with the given registry.
   * 
   * @param registry to which the collector is added. Null values ​​are
   * not allowed.
   */
  public static void register(CollectorRegistry registry) {
    for (Collector collector : defaultCollectors) {
      collector.register(registry);
      
      if (collector instanceof MemoryAllocationExports) {
        ((MemoryAllocationExports)collector).addGarbageCollectorListener();
      }
    }
  }
  
  /**
   * Unregister the default Hotspot collectors from the given registry.
   * 
   * @param registry from that the default collectors are removed. Null
   * values ​​are not allowed.
   */
  public static void unregister(CollectorRegistry registry) {
    for (Collector collector : defaultCollectors) {
      registry.unregister(collector);
      
      if (collector instanceof MemoryAllocationExports) {
        ((MemoryAllocationExports)collector).removeGarbageCollectorListener();
      }
    }
  }
}
