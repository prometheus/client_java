package io.prometheus.metrics.exporter.servlet.jakarta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.prometheus.metrics.exporter.common.PrometheusHttpRequest;
import io.prometheus.metrics.exporter.common.PrometheusHttpResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class HttpExchangeAdapterTest {

  @Test
  public void testRequestGetQueryString() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    when(servletRequest.getQueryString()).thenReturn("name[]=test");

    HttpExchangeAdapter adapter = new HttpExchangeAdapter(servletRequest, servletResponse);
    PrometheusHttpRequest request = adapter.getRequest();

    assertThat(request.getQueryString()).isEqualTo("name[]=test");
  }

  @Test
  public void testRequestGetHeaders() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    when(servletRequest.getHeaders("Accept"))
        .thenReturn(Collections.enumeration(Collections.singletonList("text/plain")));

    HttpExchangeAdapter adapter = new HttpExchangeAdapter(servletRequest, servletResponse);
    PrometheusHttpRequest request = adapter.getRequest();

    assertThat(request.getHeaders("Accept").nextElement()).isEqualTo("text/plain");
  }

  @Test
  public void testRequestGetMethod() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    when(servletRequest.getMethod()).thenReturn("GET");

    HttpExchangeAdapter adapter = new HttpExchangeAdapter(servletRequest, servletResponse);
    PrometheusHttpRequest request = adapter.getRequest();

    assertThat(request.getMethod()).isEqualTo("GET");
  }

  @Test
  public void testRequestGetRequestPath() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    when(servletRequest.getContextPath()).thenReturn("/app");
    when(servletRequest.getServletPath()).thenReturn("/metrics");
    when(servletRequest.getPathInfo()).thenReturn(null);

    HttpExchangeAdapter adapter = new HttpExchangeAdapter(servletRequest, servletResponse);
    PrometheusHttpRequest request = adapter.getRequest();

    assertThat(request.getRequestPath()).isEqualTo("/app/metrics");
  }

  @Test
  public void testRequestGetRequestPathWithPathInfo() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    when(servletRequest.getContextPath()).thenReturn("/app");
    when(servletRequest.getServletPath()).thenReturn("/metrics");
    when(servletRequest.getPathInfo()).thenReturn("/extra");

    HttpExchangeAdapter adapter = new HttpExchangeAdapter(servletRequest, servletResponse);
    PrometheusHttpRequest request = adapter.getRequest();

    assertThat(request.getRequestPath()).isEqualTo("/app/metrics/extra");
  }

  @Test
  public void testResponseSetHeader() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);

    HttpExchangeAdapter adapter = new HttpExchangeAdapter(servletRequest, servletResponse);
    PrometheusHttpResponse response = adapter.getResponse();

    response.setHeader("Content-Type", "text/plain");
    verify(servletResponse).setHeader("Content-Type", "text/plain");
  }

  @Test
  public void testResponseSendHeadersAndGetBody() throws IOException {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    ServletOutputStream outputStream = mock(ServletOutputStream.class);
    when(servletResponse.getOutputStream()).thenReturn(outputStream);

    HttpExchangeAdapter adapter = new HttpExchangeAdapter(servletRequest, servletResponse);
    PrometheusHttpResponse response = adapter.getResponse();

    response.sendHeadersAndGetBody(200, 100);

    verify(servletResponse).setContentLength(100);
    verify(servletResponse).setStatus(200);
    verify(servletResponse).getOutputStream();
  }

  @Test
  public void testResponseSendHeadersWithContentLengthAlreadySet() throws IOException {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    ServletOutputStream outputStream = mock(ServletOutputStream.class);
    when(servletResponse.getHeader("Content-Length")).thenReturn("50");
    when(servletResponse.getOutputStream()).thenReturn(outputStream);

    HttpExchangeAdapter adapter = new HttpExchangeAdapter(servletRequest, servletResponse);
    PrometheusHttpResponse response = adapter.getResponse();

    response.sendHeadersAndGetBody(200, 100);

    verify(servletResponse).setStatus(200);
    verify(servletResponse).getOutputStream();
  }

  @Test
  public void testHandleIOException() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);

    HttpExchangeAdapter adapter = new HttpExchangeAdapter(servletRequest, servletResponse);
    IOException exception = new IOException("Test exception");

    assertThatExceptionOfType(IOException.class)
        .isThrownBy(() -> adapter.handleException(exception))
        .withMessage("Test exception");
  }

  @Test
  public void testHandleRuntimeException() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);

    HttpExchangeAdapter adapter = new HttpExchangeAdapter(servletRequest, servletResponse);
    RuntimeException exception = new RuntimeException("Test exception");

    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> adapter.handleException(exception))
        .withMessage("Test exception");
  }

  @Test
  public void testClose() {
    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);

    HttpExchangeAdapter adapter = new HttpExchangeAdapter(servletRequest, servletResponse);
    adapter.close(); // Should not throw
  }
}
