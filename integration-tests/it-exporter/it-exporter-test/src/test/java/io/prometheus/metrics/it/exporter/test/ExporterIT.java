package io.prometheus.metrics.it.exporter.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.Resources;
import io.prometheus.client.it.common.ExporterTest;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_31_0.Metrics;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

abstract class ExporterIT extends ExporterTest {

  public ExporterIT(String sampleApp) throws IOException, URISyntaxException {
    super(sampleApp);
  }

  @Test
  public void testOpenMetricsTextFormat() throws IOException {
    start();
    Response response =
        scrape("GET", "", "Accept", "application/openmetrics-text; version=1.0.0; charset=utf-8");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "application/openmetrics-text; version=1.0.0; charset=utf-8",
        response.getHeader("Content-Type"));
    assertThat(response.getHeader("Content-Encoding")).isNull();
    assertThat(response.getHeader("Transfer-Encoding")).isNull();
    assertThat(response.getHeader("Content-Length"))
        .isEqualTo(Integer.toString(response.body.length));
    assertThat(response.stringBody())
        .contains("integration_test_info{test_name=\"" + sampleApp + "\"} 1")
        .contains("temperature_celsius{location=\"inside\"} 23.0")
        .contains("temperature_celsius{location=\"outside\"} 27.0")
        .contains("uptime_seconds_total 17.0")
        // OpenMetrics text format has a UNIT.
        .contains("# UNIT uptime_seconds seconds");
  }

  @Test
  public void testPrometheusTextFormat() throws IOException {
    start();
    Response response = scrape("GET", "");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "text/plain; version=0.0.4; charset=utf-8", response.getHeader("Content-Type"));
    assertThat(response.getHeader("Content-Encoding")).isNull();
    assertThat(response.getHeader("Transfer-Encoding")).isNull();
    assertThat(response.getHeader("Content-Length"))
        .isEqualTo(Integer.toString(response.body.length));
    assertThat(response.stringBody())
        .contains("integration_test_info{test_name=\"" + sampleApp + "\"} 1")
        .contains("temperature_celsius{location=\"inside\"} 23.0")
        .contains("temperature_celsius{location=\"outside\"} 27.0")
        .contains("uptime_seconds_total 17.0")
        // Prometheus text format does not have a UNIT.
        .doesNotContain("# UNIT uptime_seconds seconds");
  }

  @Test
  public void testPrometheusProtobufFormat() throws IOException {
    start();
    Response response =
        scrape(
            "GET",
            "",
            "Accept",
            "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily;"
                + " encoding=delimited");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily;"
            + " encoding=delimited",
        response.getHeader("Content-Type"));
    assertThat(response.getHeader("Content-Encoding")).isNull();
    assertThat(response.getHeader("Transfer-Encoding")).isNull();
    assertThat(response.getHeader("Content-Length"))
        .isEqualTo(Integer.toString(response.body.length));
    List<Metrics.MetricFamily> metrics = response.protoBody();
    assertThat(metrics).hasSize(3);
    // metrics are sorted by name
    assertThat(metrics.get(0).getName()).isEqualTo("integration_test_info");
    assertThat(metrics.get(1).getName()).isEqualTo("temperature_celsius");
    assertThat(metrics.get(2).getName()).isEqualTo("uptime_seconds_total");
  }

  @ParameterizedTest
  @CsvSource({
    "openmetrics,         debug-openmetrics.txt",
    "text,                debug-text.txt",
    "prometheus-protobuf, debug-protobuf.txt",
  })
  public void testPrometheusProtobufDebugFormat(String format, String expected) throws IOException {
    start();
    Response response = scrape("GET", "debug=" + format);
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "text/plain;charset=utf-8", response.getHeader("Content-Type").replace(" ", ""));
    assertThat(response.stringBody().trim())
        .isEqualTo(
            Resources.toString(Resources.getResource(expected), UTF_8)
                .trim()
                .replace("<app>", sampleApp));
  }

  @Test
  public void testCompression() throws IOException {
    start();
    Response response =
        scrape(
            "GET",
            "",
            "Accept",
            "application/openmetrics-text; version=1.0.0; charset=utf-8",
            "Accept-Encoding",
            "gzip");
    assertThat(response.status).isEqualTo(200);
    assertThat(response.getHeader("Content-Encoding")).isEqualTo("gzip");
    if (response.getHeader("Content-Length") != null) {
      // The servlet container might set a content length as the body is very small.
      assertThat(response.getHeader("Content-Length"))
          .isEqualTo(Integer.toString(response.body.length));
      assertThat(response.getHeader("Transfer-Encoding")).isNull();
    } else {
      // If no content length is set, transfer-encoding chunked must be used.
      assertThat(response.getHeader("Transfer-Encoding")).isEqualTo("chunked");
    }
    assertContentType(
        "application/openmetrics-text; version=1.0.0; charset=utf-8",
        response.getHeader("Content-Type"));
    assertThat(response.gzipBody()).contains("uptime_seconds_total 17.0");
  }

  @Test
  public void testErrorHandling() throws IOException {
    start("error");
    Response response = scrape("GET", "");
    assertThat(response.status).isEqualTo(500);
    assertThat(response.stringBody()).contains("Simulating an error.");
  }

  @Test
  public void testHeadRequest() throws IOException {
    start();
    Response fullResponse = scrape("GET", "");
    int size = fullResponse.body.length;
    assertThat(size).isGreaterThan(0);
    Response headResponse = scrape("HEAD", "");
    assertThat(headResponse.status).isEqualTo(200);
    assertThat(headResponse.getHeader("Content-Length")).isEqualTo(Integer.toString(size));
    assertThat(headResponse.body).isEmpty();
  }

  @Test
  public void testDebug() throws IOException {
    start();
    Response response = scrape("GET", "debug=openmetrics");
    assertThat(response.status).isEqualTo(200);
    assertContentType("text/plain; charset=utf-8", response.getHeader("Content-Type"));
    assertThat(response.stringBody())
        .contains("uptime_seconds_total 17.0")
        .contains("# UNIT uptime_seconds seconds");
  }

  @Test
  public void testNameFilter() throws IOException {
    start();
    Response response =
        scrape(
            "GET",
            nameParam("integration_test_info") + "&" + nameParam("uptime_seconds_total"),
            "Accept",
            "application/openmetrics-text; version=1.0.0; charset=utf-8");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "application/openmetrics-text; version=1.0.0; charset=utf-8",
        response.getHeader("Content-Type"));
    assertThat(response.stringBody())
        .contains("integration_test_info{test_name=\"" + sampleApp + "\"} 1")
        .contains("uptime_seconds_total 17.0")
        .doesNotContain("temperature_celsius");
  }

  @Test
  public void testEmptyResponseOpenMetrics() throws IOException {
    start();
    Response response =
        scrape(
            "GET",
            nameParam("none_existing"),
            "Accept",
            "application/openmetrics-text; version=1.0.0; charset=utf-8");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "application/openmetrics-text; version=1.0.0; charset=utf-8",
        response.getHeader("Content-Type"));
    assertThat(response.getHeader("Content-Length"))
        .isEqualTo(Integer.toString(response.body.length));
    assertThat(response.stringBody()).isEqualTo("# EOF\n");
  }

  @Test
  public void testEmptyResponseText() throws IOException {
    start();
    Response response = scrape("GET", nameParam("none_existing"));
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "text/plain; version=0.0.4; charset=utf-8", response.getHeader("Content-Type"));
    if (response.getHeader("Content-Length")
        != null) { // HTTPServer does not send a zero content length, which is ok
      assertThat(response.getHeader("Content-Length")).isEqualTo("0");
    }
    assertThat(response.body).isEmpty();
  }

  @Test
  public void testEmptyResponseProtobuf() throws IOException {
    start();
    Response response =
        scrape(
            "GET",
            nameParam("none_existing"),
            "Accept",
            "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily;"
                + " encoding=delimited");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily;"
            + " encoding=delimited",
        response.getHeader("Content-Type"));
    assertThat(response.body).isEmpty();
  }

  @Test
  public void testEmptyResponseGzipOpenMetrics() throws IOException {
    start();
    Response response =
        scrape(
            "GET",
            nameParam("none_existing"),
            "Accept",
            "application/openmetrics-text; version=1.0.0; charset=utf-8",
            "Accept-Encoding",
            "gzip");
    assertThat(response.status).isEqualTo(200);
    assertThat(response.getHeader("Content-Encoding")).isEqualTo("gzip");
    assertThat(response.gzipBody()).isEqualTo("# EOF\n");
  }

  @Test
  public void testEmptyResponseGzipText() throws IOException {
    start();
    Response response = scrape("GET", nameParam("none_existing"), "Accept-Encoding", "gzip");
    assertThat(response.status).isEqualTo(200);
    assertThat(response.getHeader("Content-Encoding")).isEqualTo("gzip");
    assertThat(response.gzipBody()).isEmpty();
  }

  private String nameParam(String name) {
    return URLEncoder.encode("name[]", UTF_8) + "=" + URLEncoder.encode(name, UTF_8);
  }

  @Test
  public void testDebugUnknown() throws IOException {
    start();
    Response response = scrape("GET", "debug=unknown");
    assertThat(response.status).isEqualTo(500);
    assertContentType("text/plain; charset=utf-8", response.getHeader("Content-Type"));
  }
}
