package io.prometheus.client.it.java_versions;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;

/**
 * Simple example application that compiles with Java 6.
 */
public class ExampleApplication {

  public static void main(String[] args) throws IOException, InterruptedException {
    DefaultExports.initialize();
    Counter counter = Counter.build()
        .name("test")
        .help("test counter")
        .labelNames("path")
        .register();
    counter.labels("/hello-world").inc();
    new HTTPServer(9000);
    Thread.currentThread().join(); // sleep forever
  }
}
