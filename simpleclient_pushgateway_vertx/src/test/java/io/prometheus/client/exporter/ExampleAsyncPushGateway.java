package io.prometheus.client.exporter;

import io.prometheus.client.Gauge;
import io.prometheus.client.CollectorRegistry;
import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Vertx;


public class ExampleAsyncPushGateway {
  static final CollectorRegistry pushRegistry = new CollectorRegistry();
  static final Gauge g = (Gauge) Gauge.build().name("gauge").help("blah").register(pushRegistry);

  /**
   * Example of how to use the pushgateway, pass in the host:port of a pushgateway.
   */
  public static void main(String[] args) throws Exception {
    AsyncPushGateway pg = new AsyncPushGateway(Vertx.vertx(), args[0]);
    g.set(42);
    pg.push(pushRegistry, "job", new AsyncResultHandler<Void>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        System.out.println(result.succeeded());
      }
    });
  }
}

