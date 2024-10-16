package io.prometheus.metrics.it.exporter.test;

import java.io.IOException;
import java.net.URISyntaxException;

class TomcatIT extends ExporterIT {
  public TomcatIT() throws IOException, URISyntaxException {
    super("exporter-servlet-tomcat-sample");
  }

  @Override
  protected boolean headReturnsContentLength() {
    // not any more since
    // https://tomcat.apache.org/tomcat-11.0-doc/changelog.html#Tomcat_11.0.0-M3_(markt)
    return false;
  }
}
