package io.prometheus.metrics.it.exporter.test;

import java.io.IOException;
import java.net.URISyntaxException;

class TomcatIT extends ExporterIT {
  public TomcatIT() throws IOException, URISyntaxException {
    super("exporter-servlet-tomcat-sample");
  }
}
