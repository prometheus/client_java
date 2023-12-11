package io.prometheus.client.servlet.common.exporter;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.servlet.common.adapter.HttpServletRequestAdapter;
import io.prometheus.client.servlet.common.adapter.HttpServletResponseAdapter;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;

public class ExporterTest {

  private HttpServletRequestAdapter mockHttpServletRequest() {
    return mockHttpServletRequest(null, false);
  }

  private HttpServletRequestAdapter mockHttpServletRequest(final String[] nameParam, final boolean openMetrics) {
    return new HttpServletRequestAdapter() {
      @Override
      public String getHeader(String name) {
        if (openMetrics && "Accept".equals(name)) {
          return "application/openmetrics-text; version=0.0.1,text/plain;version=0.0.4;q=0.5,*/*;q=0.1";
        }
        return null;
      }

      @Override
      public String getMethod() {
        return null;
      }

      @Override
      public String getRequestURI() {
        return null;
      }

      @Override
      public String[] getParameterValues(String name) {
        if ("name[]".equals(name)) {
          return nameParam;
        }
        return null;
      }

      @Override
      public String getContextPath() {
        return "";
      }
    };
  }

  private HttpServletResponseAdapter mockHttpServletResponse(final PrintWriter writer) {
    return new HttpServletResponseAdapter() {
      @Override
      public int getStatus() {
        return 0;
      }

      @Override
      public void setStatus(int httpStatusCode) {
      }

      @Override
      public void setContentType(String contentType) {
      }

      @Override
      public PrintWriter getWriter() {
        return writer;
      }
    };
  }

  @Test
  public void testWriterFiltersBasedOnParameter() throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    Gauge.build("a", "a help").register(registry);
    Gauge.build("b", "a help").register(registry);
    Gauge.build("c", "a help").register(registry);

    HttpServletRequestAdapter req = mockHttpServletRequest(new String[]{"a", "b", "oneTheDoesntExist", ""}, false);
    StringWriter responseBody = new StringWriter();
    HttpServletResponseAdapter resp = mockHttpServletResponse(new PrintWriter(responseBody));

    new Exporter(registry, null).doGet(req, resp);

    assertThat(responseBody.toString()).contains("a 0.0");
    assertThat(responseBody.toString()).contains("b 0.0");
    assertThat(responseBody.toString()).doesNotContain("c 0.0");
  }

  @Test
  public void testWriterIsClosedNormally() throws IOException {
    final AtomicBoolean closed = new AtomicBoolean(false);
    StringWriter responseBody = new StringWriter() {
      @Override
      public void close() {
        closed.set(true);
      }
    };
    HttpServletRequestAdapter req = mockHttpServletRequest();
    HttpServletResponseAdapter resp = mockHttpServletResponse(new PrintWriter(responseBody));
    CollectorRegistry registry = new CollectorRegistry();
    Gauge a = Gauge.build("a", "a help").register(registry);

    new Exporter(registry, null).doGet(req, resp);
    Assert.assertTrue(closed.get());
  }

  @Test
  public void testWriterIsClosedOnException() {
    final AtomicBoolean closed = new AtomicBoolean(false);
    StringWriter responseBody = new StringWriter() {
      @Override
      public void write(char cbuf[], int off, int len) {
        throw new RuntimeException();
      }
      @Override
      public void close() {
        closed.set(true);
      }
    };
    HttpServletRequestAdapter req = mockHttpServletRequest();
    HttpServletResponseAdapter resp = mockHttpServletResponse(new PrintWriter(responseBody));
    CollectorRegistry registry = new CollectorRegistry();
    Gauge a = Gauge.build("a", "a help").register(registry);

    try {
      new Exporter(registry, null).doGet(req, resp);
      fail("Exception expected");
    } catch (Exception e) {
    }

    Assert.assertTrue(closed.get());
  }

  @Test
  public void testOpenMetricsNegotiated() throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    Gauge.build("a", "a help").register(registry);

    HttpServletRequestAdapter req = mockHttpServletRequest(null, true);
    StringWriter responseBody = new StringWriter();
    HttpServletResponseAdapter resp = mockHttpServletResponse(new PrintWriter(responseBody));

    new Exporter(registry, null).doGet(req, resp);

    assertThat(responseBody.toString()).contains("a 0.0");
    assertThat(responseBody.toString()).contains("# EOF");
  }
}
