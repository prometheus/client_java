package io.prometheus.metrics.exporter.httpserver;

import com.sun.net.httpserver.HttpExchange;
import io.prometheus.metrics.exporter.common.PrometheusHttpExchange;
import io.prometheus.metrics.exporter.common.PrometheusHttpRequest;
import io.prometheus.metrics.exporter.common.PrometheusHttpResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpExchangeAdapter implements PrometheusHttpExchange {

  private static final Logger logger = Logger.getLogger(HttpExchangeAdapter.class.getName());
  private static final byte[] ERROR_RESPONSE =
      "An internal error occurred while scraping metrics.\n".getBytes(StandardCharsets.UTF_8);

  private final HttpExchange httpExchange;
  private final HttpRequest request = new HttpRequest();
  private final HttpResponse response = new HttpResponse();
  private volatile boolean responseSent = false;

  public HttpExchangeAdapter(HttpExchange httpExchange) {
    this.httpExchange = httpExchange;
  }

  public class HttpRequest implements PrometheusHttpRequest {

    @Override
    public String getQueryString() {
      return httpExchange.getRequestURI().getRawQuery();
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
      List<String> headers = httpExchange.getRequestHeaders().get(name);
      if (headers == null) {
        return Collections.emptyEnumeration();
      } else {
        return Collections.enumeration(headers);
      }
    }

    @Override
    public String getMethod() {
      return httpExchange.getRequestMethod();
    }

    @Override
    public String getRequestPath() {
      URI requestURI = httpExchange.getRequestURI();
      String uri = requestURI.toString();
      int qx = uri.indexOf('?');
      if (qx != -1) {
        uri = uri.substring(0, qx);
      }
      return uri;
    }
  }

  public class HttpResponse implements PrometheusHttpResponse {

    @Override
    public void setHeader(String name, String value) {
      httpExchange.getResponseHeaders().set(name, value);
    }

    @Override
    public OutputStream sendHeadersAndGetBody(int statusCode, int contentLength)
        throws IOException {
      if (responseSent) {
        throw new IOException("Cannot send multiple HTTP responses for a single HTTP exchange.");
      }
      responseSent = true;
      httpExchange.sendResponseHeaders(statusCode, contentLength);
      return httpExchange.getResponseBody();
    }
  }

  @Override
  public HttpRequest getRequest() {
    return request;
  }

  @Override
  public HttpResponse getResponse() {
    return response;
  }

  @Override
  public void handleException(IOException e) throws IOException {
    sendErrorResponse(e);
  }

  @Override
  public void handleException(RuntimeException e) {
    sendErrorResponse(e);
  }

  private void sendErrorResponse(Exception requestHandlerException) {
    if (!responseSent) {
      responseSent = true;
      logger.log(
          Level.SEVERE,
          "The Prometheus metrics HTTPServer caught an Exception during scrape.",
          requestHandlerException);
      try {
        httpExchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        httpExchange.sendResponseHeaders(500, ERROR_RESPONSE.length);
        httpExchange.getResponseBody().write(ERROR_RESPONSE);
      } catch (IOException errorWriterException) {
        // If we can't even send an error response to the client, logging is the only remaining
        // signal.
        logger.log(
            Level.SEVERE,
            "The Prometheus metrics HTTPServer caught an Exception during scrape and "
                + "failed to send an error response to the client.",
            errorWriterException);
      }
    } else {
      // If the exception occurs after response headers have been sent, it's too late to respond
      // with HTTP 500.
      logger.log(
          Level.SEVERE,
          "The Prometheus metrics HTTPServer caught an Exception while trying to send "
              + "the metrics response.",
          requestHandlerException);
    }
  }

  @Override
  public void close() {
    httpExchange.close();
  }
}
