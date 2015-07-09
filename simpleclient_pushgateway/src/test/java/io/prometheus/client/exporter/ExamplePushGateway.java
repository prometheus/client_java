package io.prometheus.client.exporter;

import io.prometheus.client.Gauge;
import io.prometheus.client.CollectorRegistry;


public class ExamplePushGateway {
  static final CollectorRegistry pushRegistry = new CollectorRegistry();
  static final Gauge g = (Gauge) Gauge.build().name("gauge").help("blah").register(pushRegistry);

  /**
   * Example of how to use the pushgateway, pass in the host:port of a pushgateway.
   */
  public static void main(String[] args) throws Exception {
    PushGateway pg = new PushGateway(args[0]);
    g.set(42);
    pg.push(pushRegistry, "job");
  }
}

