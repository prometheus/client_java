package io.prometheus.metrics.exporter.httpserver;

import io.prometheus.metrics.annotations.StableApi;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import javax.annotation.Nullable;

/**
 * Controls how the {@link HTTPServer} handles exceptions raised while scraping metrics.
 *
 * <p>The default policy is {@link #genericResponse()}, which does not expose exception details and
 * does not report the exception. Use {@link #genericResponseWithReporter(Consumer)} to route
 * diagnostic details to an application-appropriate sink.
 */
@StableApi
public final class HttpErrorHandlingPolicy {

  private static final byte[] GENERIC_RESPONSE =
      ("An internal error occurred while scraping metrics. "
              + "Configure an HTTP error reporter for details.\n")
          .getBytes(StandardCharsets.UTF_8);

  private final boolean detailedResponse;
  @Nullable private final Consumer<Throwable> errorReporter;

  private HttpErrorHandlingPolicy(
      boolean detailedResponse, @Nullable Consumer<Throwable> errorReporter) {
    this.detailedResponse = detailedResponse;
    this.errorReporter = errorReporter;
  }

  /**
   * Returns the secure default policy.
   *
   * <p>Scrape exceptions produce a generic HTTP 500 response and are not reported. This avoids
   * exposing exception details to scrape clients or adding an implicit dependency on an
   * application's logging configuration.
   */
  public static HttpErrorHandlingPolicy genericResponse() {
    return new HttpErrorHandlingPolicy(false, null);
  }

  /**
   * Returns a policy that produces a generic HTTP 500 response and passes scrape exceptions to
   * {@code errorReporter}.
   *
   * <p>The reporter runs synchronously on the HTTP request thread. It should return promptly and
   * must be safe to call concurrently. Runtime exceptions thrown by the reporter are isolated from
   * HTTP response handling.
   */
  public static HttpErrorHandlingPolicy genericResponseWithReporter(
      Consumer<Throwable> errorReporter) {
    if (errorReporter == null) {
      throw new NullPointerException("errorReporter");
    }
    return new HttpErrorHandlingPolicy(false, errorReporter);
  }

  /**
   * Returns a policy that includes the full exception stack trace in the HTTP 500 response.
   *
   * <p><strong>Security warning:</strong> This legacy behavior exposes internal exception
   * information to scrape clients. Do not use it for endpoints reachable by untrusted clients.
   */
  public static HttpErrorHandlingPolicy legacyDetailedResponse() {
    return new HttpErrorHandlingPolicy(true, null);
  }

  byte[] getErrorResponse(Exception exception) {
    if (!detailedResponse) {
      return GENERIC_RESPONSE;
    }
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    printWriter.write("An Exception occurred while scraping metrics: ");
    exception.printStackTrace(printWriter);
    return stringWriter.toString().getBytes(StandardCharsets.UTF_8);
  }

  void report(Throwable error) {
    if (errorReporter != null) {
      errorReporter.accept(error);
    }
  }
}
