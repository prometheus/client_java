package io.prometheus.metrics.exporter.httpserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
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

  @Test
  void handleExceptionReturnsGenericMessageWithoutStackTrace() throws Exception {
    HttpExchange httpExchange = mock(HttpExchange.class);
    Headers headers = new Headers();
    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(httpExchange.getResponseHeaders()).thenReturn(headers);
    when(httpExchange.getResponseBody()).thenReturn(responseBody);
    HttpExchangeAdapter adapter = new HttpExchangeAdapter(httpExchange);

    adapter.handleException(new IllegalStateException("secret failure"));

    String body = new String(responseBody.toByteArray(), StandardCharsets.UTF_8);
    assertThat(body)
        .isEqualTo(
            "An internal error occurred while scraping metrics. "
                + "Configure an HTTP error reporter for details.\n");
    assertThat(body).doesNotContain("IllegalStateException");
    assertThat(body).doesNotContain("secret failure");
    assertThat(body).doesNotContain("at ");
    assertThat(headers.getFirst("Content-Type")).isEqualTo("text/plain; charset=utf-8");
    verify(httpExchange).sendResponseHeaders(500, body.getBytes(StandardCharsets.UTF_8).length);
  }

  @Test
  void handleExceptionInvokesConfiguredReporter() {
    HttpExchange httpExchange = mock(HttpExchange.class);
    Headers headers = new Headers();
    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(httpExchange.getResponseHeaders()).thenReturn(headers);
    when(httpExchange.getResponseBody()).thenReturn(responseBody);
    AtomicReference<Throwable> reportedError = new AtomicReference<>();
    HttpExchangeAdapter adapter =
        new HttpExchangeAdapter(
            httpExchange,
            HttpErrorHandlingPolicy.builder().errorReporter(reportedError::set).build());
    IllegalStateException scrapeException = new IllegalStateException("secret failure");

    adapter.handleException(scrapeException);

    assertThat(reportedError.get()).isSameAs(scrapeException);
  }

  @Test
  void reporterRunsAfterErrorResponseWasSent() throws Exception {
    HttpExchange httpExchange = mock(HttpExchange.class);
    Headers headers = new Headers();
    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(httpExchange.getResponseHeaders()).thenReturn(headers);
    when(httpExchange.getResponseBody()).thenReturn(responseBody);
    AtomicBoolean reporterRanAfterResponse = new AtomicBoolean();
    HttpExchangeAdapter adapter =
        new HttpExchangeAdapter(
            httpExchange,
            HttpErrorHandlingPolicy.builder()
                .errorReporter(ignored -> reporterRanAfterResponse.set(responseBody.size() > 0))
                .build());

    adapter.handleException(new IllegalStateException("secret failure"));

    assertThat(reporterRanAfterResponse).isTrue();
  }

  @Test
  void reporterFailureDoesNotPreventGenericResponse() {
    HttpExchange httpExchange = mock(HttpExchange.class);
    Headers headers = new Headers();
    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(httpExchange.getResponseHeaders()).thenReturn(headers);
    when(httpExchange.getResponseBody()).thenReturn(responseBody);
    HttpExchangeAdapter adapter =
        new HttpExchangeAdapter(
            httpExchange,
            HttpErrorHandlingPolicy.builder()
                .errorReporter(
                    ignored -> {
                      throw new IllegalStateException("reporter failed");
                    })
                .build());

    adapter.handleException(new IllegalStateException("secret failure"));

    String body = new String(responseBody.toByteArray(), StandardCharsets.UTF_8);
    assertThat(body)
        .contains("Configure an HTTP error reporter for details.")
        .doesNotContain("secret failure")
        .doesNotContain("reporter failed");
  }

  @Test
  void unsafeDebugResponseIncludesStackTraceAndInvokesReporter() {
    HttpExchange httpExchange = mock(HttpExchange.class);
    Headers headers = new Headers();
    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(httpExchange.getResponseHeaders()).thenReturn(headers);
    when(httpExchange.getResponseBody()).thenReturn(responseBody);
    AtomicReference<Throwable> reportedError = new AtomicReference<>();
    HttpExchangeAdapter adapter =
        new HttpExchangeAdapter(
            httpExchange,
            HttpErrorHandlingPolicy.builder()
                .unsafeDebugResponse(true)
                .errorReporter(reportedError::set)
                .build());

    IllegalStateException scrapeException = new IllegalStateException("diagnostic detail");
    adapter.handleException(scrapeException);

    String body = new String(responseBody.toByteArray(), StandardCharsets.UTF_8);
    assertThat(body)
        .contains("An Exception occurred while scraping metrics:")
        .contains("IllegalStateException: diagnostic detail")
        .contains("at ");
    assertThat(reportedError.get()).isSameAs(scrapeException);
  }

  @Test
  void configuredReporterHandlesExceptionAfterResponseHeadersWereSent() throws Exception {
    HttpExchange httpExchange = mock(HttpExchange.class);
    Headers headers = new Headers();
    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    when(httpExchange.getResponseHeaders()).thenReturn(headers);
    when(httpExchange.getResponseBody()).thenReturn(responseBody);
    AtomicReference<Exception> reportedError = new AtomicReference<>();
    HttpExchangeAdapter adapter =
        new HttpExchangeAdapter(
            httpExchange,
            HttpErrorHandlingPolicy.builder().errorReporter(reportedError::set).build());

    adapter.getResponse().sendHeadersAndGetBody(200, 0);
    IllegalStateException scrapeException = new IllegalStateException("secret failure");
    adapter.handleException(scrapeException);

    assertThat(reportedError.get()).isSameAs(scrapeException);
    verify(httpExchange).sendResponseHeaders(200, 0);
  }

  @Test
  void julReporterLogsAtSevere() {
    Logger logger = Logger.getLogger(HttpErrorHandlingPolicy.class.getName());
    AtomicReference<LogRecord> record = new AtomicReference<>();
    Handler handler =
        new Handler() {
          @Override
          public void publish(LogRecord logRecord) {
            record.set(logRecord);
          }

          @Override
          public void flush() {}

          @Override
          public void close() {}
        };
    boolean useParentHandlers = logger.getUseParentHandlers();
    logger.setUseParentHandlers(false);
    logger.addHandler(handler);
    try {
      IllegalStateException scrapeException = new IllegalStateException("secret failure");
      HttpErrorHandlingPolicy.julReporter().accept(scrapeException);
      assertThat(record.get()).isNotNull();
      assertThat(record.get().getLevel()).isEqualTo(Level.SEVERE);
      assertThat(record.get().getThrown()).isSameAs(scrapeException);
    } finally {
      logger.removeHandler(handler);
      logger.setUseParentHandlers(useParentHandlers);
    }
  }
}
