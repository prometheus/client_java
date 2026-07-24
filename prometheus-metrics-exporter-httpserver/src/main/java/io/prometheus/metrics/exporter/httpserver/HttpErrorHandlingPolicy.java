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
 * <p>The default policy built by {@link #builder()} does not expose exception details and does not
 * report the exception. Configure the builder to route diagnostic details to an
 * application-appropriate sink.
 */
@StableApi
public final class HttpErrorHandlingPolicy {

  private static final byte[] GENERIC_RESPONSE =
      ("An internal error occurred while scraping metrics. "
              + "Configure an HTTP error reporter for details.\n")
          .getBytes(StandardCharsets.UTF_8);

  private final boolean unsafeDebugResponse;
  @Nullable private final Consumer<Throwable> errorReporter;

  private HttpErrorHandlingPolicy(
      boolean unsafeDebugResponse, @Nullable Consumer<Throwable> errorReporter) {
    this.unsafeDebugResponse = unsafeDebugResponse;
    this.errorReporter = errorReporter;
  }

  /**
   * Returns a builder for configuring scrape error handling.
   *
   * <p>The builder defaults to a generic HTTP 500 response with no error reporter. This avoids
   * exposing exception details to scrape clients or adding an implicit dependency on an
   * application's logging configuration.
   */
  public static Builder builder() {
    return new Builder();
  }

  byte[] getErrorResponse(Exception exception) {
    if (!unsafeDebugResponse) {
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

  /** Builder for {@link HttpErrorHandlingPolicy}. */
  public static final class Builder {

    private boolean unsafeDebugResponse = false;
    @Nullable private Consumer<Throwable> errorReporter;

    private Builder() {}

    /**
     * Pass scrape exceptions to {@code errorReporter}.
     *
     * <p>The reporter runs synchronously on the HTTP request thread. It should return promptly and
     * must be safe to call concurrently. Runtime exceptions thrown by the reporter are isolated
     * from HTTP response handling.
     */
    public Builder errorReporter(Consumer<Throwable> errorReporter) {
      if (errorReporter == null) {
        throw new NullPointerException("errorReporter");
      }
      this.errorReporter = errorReporter;
      return this;
    }

    /**
     * Configure whether the HTTP 500 response includes the full exception stack trace.
     *
     * <p><strong>Security warning:</strong> Setting this to {@code true} exposes internal exception
     * information to scrape clients. Do not enable it for endpoints reachable by untrusted clients.
     *
     * <p>This setting is independent of {@link #errorReporter(Consumer)}.
     */
    public Builder unsafeDebugResponse(boolean unsafeDebugResponse) {
      this.unsafeDebugResponse = unsafeDebugResponse;
      return this;
    }

    /** Build the policy. */
    public HttpErrorHandlingPolicy build() {
      return new HttpErrorHandlingPolicy(unsafeDebugResponse, errorReporter);
    }
  }
}
