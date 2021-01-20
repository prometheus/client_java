package io.prometheus.client.exporter.common.bridge;

import io.prometheus.client.CollectorRegistry;

import java.io.IOException;

/**
 * Export metrics to an external service/process.
 * <p>
 * <pre>
 * {@code
 *  Bridge b = new MyBridge(config);
 *  // Push the default registry once.
 *  b.push(CollectorRegistry.defaultRegistry);
 *
 *  // Push the default registry every 60 seconds.
 *  Thread thread = b.start(CollectorRegistry.defaultRegistry, 60);
 *  // Stop pushing.
 *  thread.interrupt();
 *  thread.join();
 * }
 * </pre>
 * <p>
 */
public interface Bridge {
  /**
   * Push samples from the given registry to the external process.
   * @param registry The collector registry.
   * @throws IOException If the metrics could not be written.
   */
  void push(CollectorRegistry registry) throws IOException;

  /**
   * Push samples from the given registry to the external process at the given interval.
   * @param registry The collector registry.
   * @param intervalSeconds The interval in seconds.
   * @return The started thread.
   */
  Thread start(CollectorRegistry registry, int intervalSeconds);
}
