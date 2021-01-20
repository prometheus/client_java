package io.prometheus.client.bridge;

import io.prometheus.client.graphite.GraphiteBridge;

/**
 * Export metrics in the Graphite plaintext format.
 * <p>
 * <pre>
 * {@code
 *  Graphite g = new Graphite("localhost", 2003);
 *  // Push the default registry once.
 *  g.push(CollectorRegistry.defaultRegistry);
 *
 *  // Push the default registry every 60 seconds.
 *  Thread thread = g.start(CollectorRegistry.defaultRegistry, 60);
 *  // Stop pushing.
 *  thread.interrupt();
 *  thread.join();
 * }
 * </pre>
 * <p>
 */
public class Graphite extends GraphiteBridge {
  /**
   * Construct a Graphite Bridge with the given host:port.
   * @param host The host name.
   * @param port The port number.
   */
  public Graphite(final String host, final int port) {
    super(host, port);
  }
}
