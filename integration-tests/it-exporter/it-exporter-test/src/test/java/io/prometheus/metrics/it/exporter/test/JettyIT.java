package io.prometheus.metrics.it.exporter.test;

import java.io.IOException;
import java.net.URISyntaxException;

class JettyIT extends ExporterIT {
  public JettyIT() throws IOException, URISyntaxException {
    super("exporter-servlet-jetty-sample");
  }
}
