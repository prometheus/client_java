package io.prometheus.client.exporter;

import static org.mockito.Matchers.anyChar;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.prometheus.client.Gauge;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

public class MetricsServletTest {

  @Test
  public void testWriterFiltersBasedOnParameter() throws IOException, ServletException {
    Gauge.build("a","a help").register();
    Gauge.build("b","a help").register();
    Gauge.build("c","a help").register();

    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameter("names[]")).thenReturn("a,b,oneThatDoesntExist,,b");
    HttpServletResponse resp = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(resp.getWriter()).thenReturn(writer);

    new MetricsServlet().doGet(req, resp);

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
    new MetricsServlet().doGet(req, resp);
    verify(writer).close();
  }

  @Test
  public void testWriterIsClosedOnException() throws IOException, ServletException {
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse resp = mock(HttpServletResponse.class);
    PrintWriter writer = mock(PrintWriter.class);
    when(resp.getWriter()).thenReturn(writer);
    doThrow(new RuntimeException()).when(writer).write(anyChar());
    new MetricsServlet().doGet(req, resp);
    verify(writer).close();
  }
}
