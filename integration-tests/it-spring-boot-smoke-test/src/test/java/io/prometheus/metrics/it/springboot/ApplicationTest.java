package io.prometheus.metrics.it.springboot;

import io.prometheus.client.it.common.ExporterTest;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_28_3.Metrics;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplicationTest {
  @Test
  public void testPrometheusProtobufFormat() throws IOException {
    ExporterTest.Response response =
            ExporterTest.scrape(
            "GET",
            new URL("http://localhost:8080/actuator/prometheus"),
            "Accept",
            "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");
    assertThat(response.status).isEqualTo(200);

    List<Metrics.MetricFamily> metrics = response.protoBody();
    Optional<Metrics.MetricFamily> metric = metrics.stream()
            .filter(m -> m.getName().equals("application_started_time_seconds"))
            .findFirst();
    assertThat(metric).isPresent();
  }
}
