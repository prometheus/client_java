package io.prometheus.metrics.it.exporter.test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.prometheus.client.it.common.LogConsumer;
import io.prometheus.client.it.common.Volume;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_3_25_3.Metrics;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

@RunWith(Parameterized.class)
public class ExporterIT {

  private final GenericContainer<?> sampleAppContainer;
  private final Volume sampleAppVolume;
  private final String sampleApp;

  @Parameterized.Parameters(name = "{0}")
  public static String[] sampleApps() {
    return new String[] {
      "exporter-httpserver-sample",
      "exporter-servlet-tomcat-sample",
      "exporter-servlet-jetty-sample",
    };
  }

  public ExporterIT(String sampleApp) throws IOException, URISyntaxException {
    this.sampleApp = sampleApp;
    this.sampleAppVolume =
        Volume.create("it-exporter")
            .copy("../../it-" + sampleApp + "/target/" + sampleApp + ".jar");
    this.sampleAppContainer =
        new GenericContainer<>("openjdk:17")
            .withFileSystemBind(sampleAppVolume.getHostPath(), "/app", BindMode.READ_ONLY)
            .withWorkingDirectory("/app")
            .withLogConsumer(LogConsumer.withPrefix(sampleApp))
            .withExposedPorts(9400);
  }

  @AfterEach
  public void tearDown() throws IOException {
    sampleAppContainer.stop();
    sampleAppVolume.remove();
  }

  @Test
  public void testOpenMetricsTextFormat() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
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
    String bodyString = new String(response.body);
    assertThat(bodyString)
        .contains("integration_test_info{test_name=\"" + sampleApp + "\"} 1")
        .contains("temperature_celsius{location=\"inside\"} 23.0")
        .contains("temperature_celsius{location=\"outside\"} 27.0")
        .contains("uptime_seconds_total 17.0")
        // OpenMetrics text format has a UNIT.
        .contains("# UNIT uptime_seconds seconds");
  }

  @Test
  public void testPrometheusTextFormat() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
    Response response = scrape("GET", "");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "text/plain; version=0.0.4; charset=utf-8", response.getHeader("Content-Type"));
    assertThat(response.getHeader("Content-Encoding")).isNull();
    assertThat(response.getHeader("Transfer-Encoding")).isNull();
    assertThat(response.getHeader("Content-Length"))
        .isEqualTo(Integer.toString(response.body.length));
    String bodyString = new String(response.body);
    assertThat(bodyString)
        .contains("integration_test_info{test_name=\"" + sampleApp + "\"} 1")
        .contains("temperature_celsius{location=\"inside\"} 23.0")
        .contains("temperature_celsius{location=\"outside\"} 27.0")
        .contains("uptime_seconds_total 17.0")
        // Prometheus text format does not have a UNIT.
        .doesNotContain("# UNIT uptime_seconds seconds");
  }

  @Test
  public void testPrometheusProtobufFormat() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
    Response response =
        scrape(
            "GET",
            "",
            "Accept",
            "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited",
        response.getHeader("Content-Type"));
    assertThat(response.getHeader("Content-Encoding")).isNull();
    assertThat(response.getHeader("Transfer-Encoding")).isNull();
    assertThat(response.getHeader("Content-Length"))
        .isEqualTo(Integer.toString(response.body.length));
    List<Metrics.MetricFamily> metrics = new ArrayList<>();
    InputStream in = new ByteArrayInputStream(response.body);
    while (in.available() > 0) {
      metrics.add(Metrics.MetricFamily.parseDelimitedFrom(in));
    }
    assertThat(metrics.size()).isEqualTo(3);
    // metrics are sorted by name
    assertThat(metrics.get(0).getName()).isEqualTo("integration_test_info");
    assertThat(metrics.get(1).getName()).isEqualTo("temperature_celsius");
    assertThat(metrics.get(2).getName()).isEqualTo("uptime_seconds_total");
  }

  @Test
  public void testCompression() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
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
    String body =
        new String(
            IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.body))),
            UTF_8);
    assertThat(body).contains("uptime_seconds_total 17.0");
  }

  @Test
  public void testErrorHandling() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "error")
        .start();
    Response response = scrape("GET", "");
    assertThat(response.status).isEqualTo(500);
    assertThat(new String(response.body, UTF_8)).contains("Simulating an error.");
  }

  @Test
  public void testHeadRequest() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
    Response fullResponse = scrape("GET", "");
    int size = fullResponse.body.length;
    assertThat(size > 0).isTrue();
    Response headResponse = scrape("HEAD", "");
    assertThat(headResponse.status).isEqualTo(200);
    assertThat(headResponse.getHeader("Content-Length")).isEqualTo(Integer.toString(size));
    assertThat(headResponse.body.length).isZero();
  }

  @Test
  public void testDebug() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
    Response response = scrape("GET", "debug=openmetrics");
    assertThat(response.status).isEqualTo(200);
    assertContentType("text/plain; charset=utf-8", response.getHeader("Content-Type"));
    String bodyString = new String(response.body, UTF_8);
    assertThat(bodyString)
        .contains("uptime_seconds_total 17.0")
        .contains("# UNIT uptime_seconds seconds");
  }

  @Test
  public void testNameFilter() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
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
    String bodyString = new String(response.body, UTF_8);
    assertThat(bodyString)
        .contains("integration_test_info{test_name=\"" + sampleApp + "\"} 1")
        .contains("uptime_seconds_total 17.0")
        .doesNotContain("temperature_celsius");
  }

  @Test
  public void testEmptyResponseOpenMetrics() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
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
    assertThat(new String(response.body, UTF_8)).isEqualTo("# EOF\n");
  }

  @Test
  public void testEmptyResponseText() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
    Response response = scrape("GET", nameParam("none_existing"));
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "text/plain; version=0.0.4; charset=utf-8", response.getHeader("Content-Type"));
    if (response.getHeader("Content-Length")
        != null) { // HTTPServer does not send a zero content length, which is ok
      assertThat(response.getHeader("Content-Length")).isEqualTo("0");
    }
    assertThat(response.body.length).isZero();
  }

  @Test
  public void testEmptyResponseProtobuf() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
    Response response =
        scrape(
            "GET",
            nameParam("none_existing"),
            "Accept",
            "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited");
    assertThat(response.status).isEqualTo(200);
    assertContentType(
        "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited",
        response.getHeader("Content-Type"));
    assertThat(response.body.length).isZero();
  }

  @Test
  public void testEmptyResponseGzipOpenMetrics() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
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
    String body =
        new String(
            IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.body))),
            UTF_8);
    assertThat(body).isEqualTo("# EOF\n");
  }

  @Test
  public void testEmptyResponseGzipText() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
    Response response = scrape("GET", nameParam("none_existing"), "Accept-Encoding", "gzip");
    assertThat(response.status).isEqualTo(200);
    assertThat(response.getHeader("Content-Encoding")).isEqualTo("gzip");
    String body =
        new String(
            IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.body))),
            UTF_8);
    assertThat(body.length()).isZero();
  }

  private String nameParam(String name) throws UnsupportedEncodingException {
    return URLEncoder.encode("name[]", UTF_8.name()) + "=" + URLEncoder.encode(name, UTF_8.name());
  }

  @Test
  public void testDebugUnknown() throws IOException {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", "success")
        .start();
    Response response = scrape("GET", "debug=unknown");
    assertThat(response.status).isEqualTo(500);
    assertContentType("text/plain; charset=utf-8", response.getHeader("Content-Type"));
  }

  private void assertContentType(String expected, String actual) {
    if (!expected.replace(" ", "").equals(actual)) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  private Response scrape(String method, String queryString, String... requestHeaders)
      throws IOException {
    long timeoutMillis = TimeUnit.SECONDS.toMillis(5);
    URL url =
        new URL(
            "http://localhost:"
                + sampleAppContainer.getMappedPort(9400)
                + "/metrics?"
                + queryString);
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod(method);
    for (int i = 0; i < requestHeaders.length; i += 2) {
      con.setRequestProperty(requestHeaders[i], requestHeaders[i + 1]);
    }
    long start = System.currentTimeMillis();
    Exception exception = null;
    while (System.currentTimeMillis() - start < timeoutMillis) {
      try {
        if (con.getResponseCode() == 200) {
          return new Response(
              con.getResponseCode(),
              con.getHeaderFields(),
              IOUtils.toByteArray(con.getInputStream()));
        } else {
          return new Response(
              con.getResponseCode(),
              con.getHeaderFields(),
              IOUtils.toByteArray(con.getErrorStream()));
        }
      } catch (Exception e) {
        exception = e;
        try {
          Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
      }
    }
    if (exception != null) {
      exception.printStackTrace();
    }
    fail("timeout while getting metrics from " + url);
    return null; // will not happen
  }

  private static class Response {
    private final int status;
    private final Map<String, String> headers;
    private final byte[] body;

    private Response(int status, Map<String, List<String>> headers, byte[] body) {
      this.status = status;
      this.headers = new HashMap<>(headers.size());
      this.body = body;
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        if (entry.getKey()
            != null) { // HttpUrlConnection uses pseudo key "null" for the status line
          this.headers.put(entry.getKey().toLowerCase(), entry.getValue().get(0));
        }
      }
    }

    private String getHeader(String name) {
      // HTTP headers are case-insensitive
      return headers.get(name.toLowerCase());
    }
  }
}
