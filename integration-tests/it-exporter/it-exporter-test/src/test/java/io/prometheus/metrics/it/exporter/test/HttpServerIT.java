package io.prometheus.metrics.it.exporter.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;

class HttpServerIT extends ExporterIT {
  public HttpServerIT() throws IOException, URISyntaxException {
    super("exporter-httpserver-sample");
  }

  @Override
  protected void assertErrorResponseBody(String body) {
    assertThat(body)
        .isEqualTo(
            "An internal error occurred while scraping metrics. "
                + "Configure an HTTP error reporter for details.\n")
        .doesNotContain("Simulating an error.");
  }
}
