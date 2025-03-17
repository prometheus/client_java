package io.prometheus.client.it.common;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_30_1.Metrics;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;

public abstract class ExporterTest {
  private final GenericContainer<?> sampleAppContainer;
  private final Volume sampleAppVolume;
  protected final String sampleApp;

  public ExporterTest(String sampleApp) throws IOException, URISyntaxException {
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

  // @BeforeEach?
  protected void start() {
    start("success");
  }

  protected void start(String outcome) {
    sampleAppContainer
        .withCommand("java", "-jar", "/app/" + sampleApp + ".jar", "9400", outcome)
        .start();
  }

  @AfterEach
  public void tearDown() throws IOException {
    sampleAppContainer.stop();
    sampleAppVolume.remove();
  }

  public static void assertContentType(String expected, String actual) {
    if (!expected.replace(" ", "").equals(actual)) {
      assertThat(actual).isEqualTo(expected);
    }
  }

  protected Response scrape(String method, String queryString, String... requestHeaders)
      throws IOException {
    return scrape(
        method,
        new URL(
            "http://localhost:"
                + sampleAppContainer.getMappedPort(9400)
                + "/metrics?"
                + queryString),
        requestHeaders);
  }

  public static Response scrape(String method, URL url, String... requestHeaders)
      throws IOException {
    long timeoutMillis = TimeUnit.SECONDS.toMillis(5);
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
          // ignore
        }
      }
    }
    if (exception != null) {
      exception.printStackTrace();
    }
    fail("timeout while getting metrics from " + url);
    return null; // will not happen
  }

  public static class Response {
    public final int status;
    private final Map<String, String> headers;
    public final byte[] body;

    private Response(int status, Map<String, List<String>> headers, byte[] body) {
      this.status = status;
      this.headers = new HashMap<>(headers.size());
      this.body = body;
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        if (entry.getKey()
            != null) { // HttpUrlConnection uses pseudo key "null" for the status line
          this.headers.put(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue().get(0));
        }
      }
    }

    public String getHeader(String name) {
      // HTTP headers are case-insensitive
      return headers.get(name.toLowerCase(Locale.ROOT));
    }

    public String stringBody() {
      return new String(body, UTF_8);
    }

    public String gzipBody() throws IOException {
      return new String(
          IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(body))), UTF_8);
    }

    public List<Metrics.MetricFamily> protoBody() throws IOException {
      List<Metrics.MetricFamily> metrics = new ArrayList<>();
      InputStream in = new ByteArrayInputStream(body);
      while (in.available() > 0) {
        metrics.add(Metrics.MetricFamily.parseDelimitedFrom(in));
      }
      return metrics;
    }
  }
}
