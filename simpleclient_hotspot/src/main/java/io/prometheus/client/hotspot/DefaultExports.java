package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * If some collector is undesirable for any reason
 * (e.g. {@link DeadlockExports} for potential performance penalty)
 * it may be explicitly excluded by using alternative registration approach:
 * <pre>
 * {@code
 *   DefaultExports.build().exclude(DeadlockExports.class).register();
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
    register(registry, Collections.<Class<? extends HotspotCollector>>emptySet());
  }

  private static void register(CollectorRegistry registry, Set<Class<? extends HotspotCollector>> exclusions) {
    List<? extends HotspotCollector> collectors = Arrays.asList(
            new StandardExports(),
            new MemoryPoolsExports(),
            new MemoryAllocationExports(),
            new BufferPoolsExports(),
            new GarbageCollectorExports(),
            new ThreadExports(),
            new DeadlockExports(),
            new ClassLoadingExports(),
            new VersionInfoExports());

    for (HotspotCollector collector : collectors) {
      if (!exclusions.contains(collector.getClass())) {
        collector.register(registry);
      }
    }
  }

  /**
   *  Return a Builder to allow configuration of a DefaultExports.
   */
  public static Builder build() {
    return new Builder();
  }

  public static class Builder {

    private final Set<Class<? extends HotspotCollector>> exclusions = new HashSet<Class<? extends HotspotCollector>>();

    private Builder() {
    }

    /**
     * Exclude provided Hotspot collector from registration.
     */
    public Builder exclude(Class<? extends HotspotCollector> exclusion) {
      exclusions.add(exclusion);
      return this;
    }

    /**
     * Register the default Hotspot collectors (except provided exclusions) with the default registry.
     */
    public void register() {
      register(CollectorRegistry.defaultRegistry);
    }

    /**
     * Register the default Hotspot collectors (except provided exclusions) with the given registry.
     */
    public void register(CollectorRegistry registry) {
      DefaultExports.register(registry, exclusions);
    }
  }

}
