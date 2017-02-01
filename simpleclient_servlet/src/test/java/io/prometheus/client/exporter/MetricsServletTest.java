package io.prometheus.client.exporter;

import static org.mockito.Matchers.anyChar;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

public class MetricsServletTest {
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
