package io.prometheus.client.it.log4j2;

import io.prometheus.client.exporter.HTTPServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Simple example application using simpleclient_log4j2.
 */
public class ExampleApplication {

  private static final Logger logger = LogManager.getLogger(ExampleApplication.class);

  public static void main(String[] args) throws IOException, InterruptedException {

    logger.debug("some debug message");
    logger.debug("another debug message");
    logger.warn("this is a warning");

    new HTTPServer(9000);
    Thread.currentThread().join(); // sleep forever
  }
}
