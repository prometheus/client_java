package io.prometheus.metrics.exporter.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Enumeration;
import org.junit.jupiter.api.Test;

class PrometheusHttpRequestTest {

  @Test
  void testGetHeaderReturnsFirstValue() {
    PrometheusHttpRequest request =
        new TestPrometheusHttpRequest("name[]=metric1&name[]=metric2", "gzip");
    assertThat(request.getHeader("Accept-Encoding")).isEqualTo("gzip");
  }

  @Test
  void testGetHeaderReturnsNullWhenNoHeaders() {
    PrometheusHttpRequest request = new TestPrometheusHttpRequest("", null);
    assertThat(request.getHeader("Accept-Encoding")).isNull();
  }

  @Test
  void testGetParameterReturnsFirstValue() {
    PrometheusHttpRequest request = new TestPrometheusHttpRequest("name[]=metric1&name[]=metric2");
    assertThat(request.getParameter("name[]")).isEqualTo("metric1");
  }

  @Test
  void testGetParameterReturnsNullWhenNotPresent() {
    PrometheusHttpRequest request = new TestPrometheusHttpRequest("other=value");
    assertThat(request.getParameter("name[]")).isNull();
  }

  @Test
  void testGetParameterValuesReturnsMultipleValues() {
    PrometheusHttpRequest request = new TestPrometheusHttpRequest("name[]=metric1&name[]=metric2");
    String[] values = request.getParameterValues("name[]");
    assertThat(values).containsExactly("metric1", "metric2");
  }

  @Test
  void testGetParameterValuesReturnsNullWhenNotPresent() {
    PrometheusHttpRequest request = new TestPrometheusHttpRequest("other=value");
    assertThat(request.getParameterValues("name[]")).isNull();
  }

  @Test
  void testGetParameterValuesWithEmptyQueryString() {
    PrometheusHttpRequest request = new TestPrometheusHttpRequest("");
    assertThat(request.getParameterValues("name[]")).isNull();
  }

  @Test
  void testGetParameterValuesWithNullQueryString() {
    PrometheusHttpRequest request = new TestPrometheusHttpRequest(null);
    assertThat(request.getParameterValues("name[]")).isNull();
  }

  @Test
  void testGetParameterValuesWithUrlEncodedValues() {
    PrometheusHttpRequest request = new TestPrometheusHttpRequest("name=hello%20world");
    String[] values = request.getParameterValues("name");
    assertThat(values).containsExactly("hello world");
  }

  @Test
  void testGetParameterValuesWithSpecialCharacters() {
    PrometheusHttpRequest request = new TestPrometheusHttpRequest("name=%2Ffoo%2Fbar");
    String[] values = request.getParameterValues("name");
    assertThat(values).containsExactly("/foo/bar");
  }

  @Test
  void testGetParameterValuesIgnoresParametersWithoutEquals() {
    PrometheusHttpRequest request =
        new TestPrometheusHttpRequest("name[]=value1&invalid&name[]=value2");
    String[] values = request.getParameterValues("name[]");
    assertThat(values).containsExactly("value1", "value2");
  }

  /** Test implementation of PrometheusHttpRequest for testing default methods. */
  private static class TestPrometheusHttpRequest implements PrometheusHttpRequest {
    private final String queryString;
    private final String acceptEncoding;

    TestPrometheusHttpRequest(String queryString) {
      this(queryString, null);
    }

    TestPrometheusHttpRequest(String queryString, String acceptEncoding) {
      this.queryString = queryString;
      this.acceptEncoding = acceptEncoding;
    }

    @Override
    public String getQueryString() {
      return queryString;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      if (acceptEncoding != null && name.equals("Accept-Encoding")) {
        return Collections.enumeration(Collections.singletonList(acceptEncoding));
      }
      return null;
    }

    @Override
    public String getMethod() {
      return "GET";
    }

    @Override
    public String getRequestPath() {
      return "/metrics";
    }
  }
}
