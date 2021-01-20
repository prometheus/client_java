package io.prometheus.client.discourse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.bridge.AbstractBridge;
import io.prometheus.client.exporter.common.bridge.BridgeConfig;

/**
 * Export metrics in the Discourse's PrometheusExporter JSON format.
 *
 * @see {@link https://github.com/discourse/prometheus_exporter}
 */
public class DiscourseBridge extends AbstractBridge<BridgeConfig> {
  /**
   * Construct a bridge with the given host:port.
   * @param host The host name.
   * @param port The port number.
   */
  public DiscourseBridge(final String host, final int port) {
    super(BridgeConfig.builder()
        .host(host)
        .port(port)
        .formatter(new DiscourseChunkedMetricFamilySamplesTextFormatter(host))
        .build());
  }

  /**
   * Constructs the bridge with the default discourse host:port.
   */
   public DiscourseBridge() {
     this("localhost", 9394);
   }
}
