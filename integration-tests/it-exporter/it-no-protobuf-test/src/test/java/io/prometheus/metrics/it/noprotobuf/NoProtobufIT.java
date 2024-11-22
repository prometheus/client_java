package io.prometheus.metrics.it.noprotobuf;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.client.it.common.ExporterTest;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;

class NoProtobufIT extends ExporterTest {

  public NoProtobufIT() throws IOException, URISyntaxException {
    super("exporter-no-protobuf");
  }

  @Test
  public void testPrometheusProtobufDebugFormat() throws IOException {
    start();
    assertThat(scrape("GET", "debug=text").status).isEqualTo(200);
    // protobuf is not supported
    assertThat(scrape("GET", "debug=prometheus-protobuf").status).isEqualTo(500);
  }
}
