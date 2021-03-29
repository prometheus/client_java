package io.prometheus.smoketest;

import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;

/**
 * Simple example application that compiles with Java 6.
 */
public class Server {

  public static void main(String[] args) throws IOException, InterruptedException {
    DefaultExports.initialize();
    new HTTPServer(9000);
    Thread.currentThread().join(); // sleep forever
  }
}
