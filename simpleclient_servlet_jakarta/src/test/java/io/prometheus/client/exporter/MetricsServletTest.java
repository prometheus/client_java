package io.prometheus.client.exporter;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetricsServletTest {

  @Test
  public void testWriterFiltersBasedOnParameter() throws IOException, ServletException {
    CollectorRegistry registry = new CollectorRegistry();
    Gauge.build("a", "a help").register(registry);
    Gauge.build("b", "a help").register(registry);
    Gauge.build("c", "a help").register(registry);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameterValues("name[]")).thenReturn(new String[]{"a", "b", "oneTheDoesntExist", ""});
    HttpServletResponse resp = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(resp.getWriter()).thenReturn(writer);

    new MetricsServlet(registry).doGet(req, resp);

    assertThat(stringWriter.toString()).contains("a 0.0");
    assertThat(stringWriter.toString()).contains("b 0.0");
    assertThat(stringWriter.toString()).doesNotContain("c 0.0");
  }

  @Test
  public void testWriterIsClosedNormally() throws IOException, ServletException {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    PrintWriter writer = mock(PrintWriter.class);
    when(resp.getWriter()).thenReturn(writer);
    CollectorRegistry registry = new CollectorRegistry();
    Gauge a = Gauge.build("a", "a help").register(registry);

    new MetricsServlet(registry).doGet(req, resp);
    verify(writer).close();
  }

  @Test
  public void testWriterIsClosedOnException() throws IOException, ServletException {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    PrintWriter writer = mock(PrintWriter.class);
    when(resp.getWriter()).thenReturn(writer);
    doThrow(new RuntimeException()).when(writer).write(any(char[].class), anyInt(), anyInt());
    CollectorRegistry registry = new CollectorRegistry();
    Gauge a = Gauge.build("a", "a help").register(registry);

    try {
      new MetricsServlet(registry).doGet(req, resp);
      fail("Exception expected");
    } catch (Exception e) {
    }

    verify(writer).close();
  }

  @Test
  public void testOpenMetricsNegotiated() throws IOException, ServletException {
    CollectorRegistry registry = new CollectorRegistry();
    Gauge.build("a", "a help").register(registry);

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getHeader("Accept")).thenReturn("application/openmetrics-text; version=0.0.1,text/plain;version=0.0.4;q=0.5,*/*;q=0.1");
    HttpServletResponse resp = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(resp.getWriter()).thenReturn(writer);

    new MetricsServlet(registry).doGet(req, resp);

    assertThat(stringWriter.toString()).contains("a 0.0");
    assertThat(stringWriter.toString()).contains("# EOF");
  }

}
