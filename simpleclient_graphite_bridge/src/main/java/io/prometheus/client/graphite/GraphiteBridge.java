package io.prometheus.client.graphite;

import io.prometheus.client.exporter.common.bridge.AbstractBridge;
import io.prometheus.client.exporter.common.bridge.BridgeConfig;

/**
 * Export metrics in the Graphite plaintext format.
 */
public class GraphiteBridge extends AbstractBridge<BridgeConfig> {
  /**
   * Construct a Graphite Bridge with the given host:port.
   * @param host The host name.
   * @param port The port number.
   */
  public GraphiteBridge(final String host, final int port) {
    super(BridgeConfig.builder()
        .host(host)
        .port(port)
        .formatter(new GraphiteMetricFamilySamplesTextFormatter())
        .build());
  }

  /**
   * Constructs the bridge with the default host:port.
   */
   public GraphiteBridge() {
     this("localhost", 2003);
   }
}
