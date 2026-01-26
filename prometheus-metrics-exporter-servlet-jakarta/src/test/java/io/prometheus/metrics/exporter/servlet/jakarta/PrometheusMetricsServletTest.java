package io.prometheus.metrics.exporter.servlet.jakarta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrometheusMetricsServletTest {

  private PrometheusRegistry registry;
  private Counter testCounter;

  @BeforeEach
  public void setUp() {
    registry = new PrometheusRegistry();
    testCounter = Counter.builder().name("test_counter").help("Test counter").register(registry);
    testCounter.inc(42);
  }

  @Test
  public void testDoGetWritesMetrics() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getQueryString()).thenReturn(null);
    when(request.getMethod()).thenReturn("GET");
    when(request.getHeaders("Accept-Encoding")).thenReturn(Collections.emptyEnumeration());
    when(request.getHeaders("Accept")).thenReturn(Collections.emptyEnumeration());
    when(request.getContextPath()).thenReturn("");
    when(request.getServletPath()).thenReturn("/metrics");
    when(request.getPathInfo()).thenReturn(null);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    when(response.getOutputStream())
        .thenReturn(
            new ServletOutputStream() {
              @Override
              public void write(int b) throws IOException {
                outputStream.write(b);
              }

              @Override
              public boolean isReady() {
                return true;
              }

              @Override
              public void setWriteListener(WriteListener writeListener) {}
            });

    PrometheusMetricsServlet servlet = new PrometheusMetricsServlet(registry);
    servlet.doGet(request, response);

    String output = outputStream.toString(StandardCharsets.UTF_8.name());
    assertThat(output).contains("test_counter");
    assertThat(output).contains("42.0");
  }

  @Test
  public void testServletUsesDefaultRegistry() {
    PrometheusMetricsServlet servlet = new PrometheusMetricsServlet();
    assertThat(servlet).isNotNull();
  }

  @Test
  public void testServletWithCustomRegistry() {
    PrometheusMetricsServlet servlet = new PrometheusMetricsServlet(registry);
    assertThat(servlet).isNotNull();
  }
}
