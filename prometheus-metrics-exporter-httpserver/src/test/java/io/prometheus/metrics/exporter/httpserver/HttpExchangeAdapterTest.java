package io.prometheus.metrics.exporter.httpserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class HttpExchangeAdapterTest {

  @Test
  void getRequestPath() {
    HttpExchange httpExchange = mock(HttpExchange.class);
    when(httpExchange.getRequestURI()).thenReturn(URI.create("/metrics?name=test"));
    HttpExchangeAdapter adapter = new HttpExchangeAdapter(httpExchange);
    assertThat(adapter.getRequest().getRequestPath()).isEqualTo("/metrics");
  }

  @Test
  void getRequestPathWithoutQueryString() {
    HttpExchange httpExchange = mock(HttpExchange.class);
    when(httpExchange.getRequestURI()).thenReturn(URI.create("/metrics"));
    HttpExchangeAdapter adapter = new HttpExchangeAdapter(httpExchange);
    assertThat(adapter.getRequest().getRequestPath()).isEqualTo("/metrics");
  }

  @Test
  void getHeadersWhenPresent() {
    HttpExchange httpExchange = mock(HttpExchange.class);
    Headers headers = new Headers();
    headers.put("Accept", List.of("text/plain"));
    when(httpExchange.getRequestHeaders()).thenReturn(headers);
    HttpExchangeAdapter adapter = new HttpExchangeAdapter(httpExchange);
    assertThat(adapter.getRequest().getHeaders("Accept").nextElement()).isEqualTo("text/plain");
  }

  @Test
  void getHeadersWhenNotPresent() {
    HttpExchange httpExchange = mock(HttpExchange.class);
    Headers headers = new Headers();
    when(httpExchange.getRequestHeaders()).thenReturn(headers);
    HttpExchangeAdapter adapter = new HttpExchangeAdapter(httpExchange);
    assertThat(adapter.getRequest().getHeaders("Accept").hasMoreElements()).isFalse();
  }
}
