package io.prometheus.metrics.it.exporter.test;

import java.io.IOException;
import java.net.URISyntaxException;

public class HttpServerIT extends ExporterIT {
  public HttpServerIT() throws IOException, URISyntaxException {
    super("exporter-httpserver-sample");
  }
}
