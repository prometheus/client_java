package io.prometheus.metrics.exporter.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrometheusScrapeHandlerTest {

  private PrometheusRegistry registry;
  private PrometheusScrapeHandler handler;
  private Counter testCounter;

  @BeforeEach
  void setUp() {
    registry = new PrometheusRegistry();
    handler = new PrometheusScrapeHandler(registry);
    testCounter = Counter.builder().name("test_counter").help("Test counter").register(registry);
    testCounter.inc(5);
  }

  @Test
  void testBasicScrape() throws IOException {
    TestHttpExchange exchange = new TestHttpExchange("GET", null);
    handler.handleRequest(exchange);

    assertThat(exchange.getResponseCode()).isEqualTo(200);
    assertThat(exchange.getResponseBody()).contains("test_counter");
    assertThat(exchange.getResponseBody()).contains("5.0");
  }

  @Test
  void testOpenMetricsFormat() throws IOException {
    TestHttpExchange exchange =
        new TestHttpExchange("GET", null).withHeader("Accept", "application/openmetrics-text");
    handler.handleRequest(exchange);

    assertThat(exchange.getResponseCode()).isEqualTo(200);
    assertThat(exchange.getResponseHeaders().get("Content-Type"))
        .contains("application/openmetrics-text");
    assertThat(exchange.getResponseBody()).contains("test_counter");
  }

  @Test
  void testPrometheusTextFormat() throws IOException {
    TestHttpExchange exchange =
        new TestHttpExchange("GET", null).withHeader("Accept", "text/plain");
    handler.handleRequest(exchange);

    assertThat(exchange.getResponseCode()).isEqualTo(200);
    assertThat(exchange.getResponseHeaders().get("Content-Type")).contains("text/plain");
    assertThat(exchange.getResponseBody()).contains("test_counter");
  }

  @Test
  void testGzipCompression() throws IOException {
    TestHttpExchange exchange =
        new TestHttpExchange("GET", null).withHeader("Accept-Encoding", "gzip");
    handler.handleRequest(exchange);

    assertThat(exchange.getResponseCode()).isEqualTo(200);
    assertThat(exchange.getResponseHeaders().get("Content-Encoding")).isEqualTo("gzip");
    assertThat(exchange.isGzipCompressed()).isTrue();

    // Decompress and verify content
    String decompressed = exchange.getDecompressedBody();
    assertThat(decompressed).contains("test_counter");
  }

  @Test
  void testMultipleAcceptEncodingHeaders() throws IOException {
    TestHttpExchange exchange =
        new TestHttpExchange("GET", null)
            .withHeader("Accept-Encoding", "deflate")
            .withHeader("Accept-Encoding", "gzip, br");
    handler.handleRequest(exchange);

    assertThat(exchange.getResponseCode()).isEqualTo(200);
    assertThat(exchange.getResponseHeaders().get("Content-Encoding")).isEqualTo("gzip");
  }

  @Test
  void testHeadRequest() throws IOException {
    TestHttpExchange exchange = new TestHttpExchange("HEAD", null);
    handler.handleRequest(exchange);

    assertThat(exchange.getResponseCode()).isEqualTo(200);
    assertThat(exchange.getResponseHeaders().get("Content-Length")).isNotNull();
    // For HEAD requests, body should be empty even though Content-Length is set
    assertThat(exchange.rawResponseBody.size()).isEqualTo(0);
  }

  @Test
  void testDebugOpenMetrics() throws IOException {
    TestHttpExchange exchange = new TestHttpExchange("GET", "debug=openmetrics");
    handler.handleRequest(exchange);

    assertThat(exchange.getResponseCode()).isEqualTo(200);
    assertThat(exchange.getResponseHeaders().get("Content-Type"))
        .isEqualTo("text/plain; charset=utf-8");
    assertThat(exchange.getResponseBody()).contains("test_counter");
  }

  @Test
  void testDebugText() throws IOException {
    TestHttpExchange exchange = new TestHttpExchange("GET", "debug=text");
    handler.handleRequest(exchange);

    assertThat(exchange.getResponseCode()).isEqualTo(200);
    assertThat(exchange.getResponseBody()).contains("test_counter");
  }

  @Test
  void testDebugInvalidParameter() throws IOException {
    TestHttpExchange exchange = new TestHttpExchange("GET", "debug=invalid");
    handler.handleRequest(exchange);

    assertThat(exchange.getResponseCode()).isEqualTo(500);
    assertThat(exchange.getResponseBody()).contains("debug=invalid: Unsupported query parameter");
  }

  @Test
  void testMetricNameFilter() throws IOException {
    Counter anotherCounter =
        Counter.builder().name("another_counter").help("Another counter").register(registry);
    anotherCounter.inc(10);

    TestHttpExchange exchange = new TestHttpExchange("GET", "name[]=test_counter");
    handler.handleRequest(exchange);

    assertThat(exchange.getResponseCode()).isEqualTo(200);
    assertThat(exchange.getResponseBody()).contains("test_counter");
    assertThat(exchange.getResponseBody()).doesNotContain("another_counter");
  }

  @Test
  void testMultipleMetricNameFilters() throws IOException {
    Counter counter1 = Counter.builder().name("metric_one").help("Metric one").register(registry);
    Counter counter2 = Counter.builder().name("metric_two").help("Metric two").register(registry);
    Counter counter3 =
        Counter.builder().name("metric_three").help("Metric three").register(registry);
    counter1.inc();
    counter2.inc();
    counter3.inc();

    TestHttpExchange exchange = new TestHttpExchange("GET", "name[]=metric_one&name[]=metric_two");
    handler.handleRequest(exchange);

    assertThat(exchange.getResponseCode()).isEqualTo(200);
    String body = exchange.getResponseBody();
    assertThat(body).contains("metric_one");
    assertThat(body).contains("metric_two");
    assertThat(body).doesNotContain("metric_three");
  }

  /** Test implementation of PrometheusHttpExchange for testing. */
  private static class TestHttpExchange implements PrometheusHttpExchange {
    private final TestHttpRequest request;
    private final TestHttpResponse response;
    private boolean closed = false;

    ByteArrayOutputStream rawResponseBody = new ByteArrayOutputStream();

    TestHttpExchange(String method, String queryString) {
      this.request = new TestHttpRequest(method, queryString);
      this.response = new TestHttpResponse(rawResponseBody);
    }

    TestHttpExchange withHeader(String name, String value) {
      request.addHeader(name, value);
      return this;
    }

    @Override
    public PrometheusHttpRequest getRequest() {
      return request;
    }

    @Override
    public PrometheusHttpResponse getResponse() {
      return response;
    }

    @Override
    public void handleException(IOException e) throws IOException {
      throw e;
    }

    @Override
    public void handleException(RuntimeException e) {
      throw e;
    }

    @Override
    public void close() {
      closed = true;
    }

    public int getResponseCode() {
      return response.statusCode;
    }

    public Map<String, String> getResponseHeaders() {
      return response.headers;
    }

    public String getResponseBody() {
      return rawResponseBody.toString(StandardCharsets.UTF_8);
    }

    public boolean isGzipCompressed() {
      return "gzip".equals(response.headers.get("Content-Encoding"));
    }

    public String getDecompressedBody() throws IOException {
      if (!isGzipCompressed()) {
        return getResponseBody();
      }
      byte[] compressed = rawResponseBody.toByteArray();
      try (GZIPInputStream gzipInputStream =
          new GZIPInputStream(new java.io.ByteArrayInputStream(compressed))) {
        ByteArrayOutputStream decompressed = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buffer)) > 0) {
          decompressed.write(buffer, 0, len);
        }
        return decompressed.toString(StandardCharsets.UTF_8.name());
      }
    }
  }

  /** Test implementation of PrometheusHttpRequest. */
  private static class TestHttpRequest implements PrometheusHttpRequest {
    private final String method;
    private final String queryString;
    private final Map<String, java.util.List<String>> headers = new HashMap<>();

    TestHttpRequest(String method, String queryString) {
      this.method = method;
      this.queryString = queryString;
    }

    void addHeader(String name, String value) {
      headers.computeIfAbsent(name, k -> new java.util.ArrayList<>()).add(value);
    }

    @Override
    public String getQueryString() {
      return queryString;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      java.util.List<String> values = headers.get(name);
      return values == null ? null : Collections.enumeration(values);
    }

    @Override
    public String getMethod() {
      return method;
    }

    @Override
    public String getRequestPath() {
      return "/metrics";
    }
  }

  /** Test implementation of PrometheusHttpResponse. */
  private static class TestHttpResponse implements PrometheusHttpResponse {
    private final Map<String, String> headers = new HashMap<>();
    private final ByteArrayOutputStream outputStream;
    private int statusCode;

    TestHttpResponse(ByteArrayOutputStream outputStream) {
      this.outputStream = outputStream;
    }

    @Override
    public void setHeader(String name, String value) {
      headers.put(name, value);
    }

    @Override
    public OutputStream sendHeadersAndGetBody(int statusCode, int contentLength)
        throws IOException {
      this.statusCode = statusCode;
      return outputStream;
    }
  }
}
