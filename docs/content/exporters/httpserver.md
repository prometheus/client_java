---
title: HTTPServer
weight: 4
---

The `HTTPServer` is a standalone server for exposing a metric endpoint. A minimal example
application for `HTTPServer` can be found in
the [examples](https://github.com/prometheus/client_java/tree/1.0.x/examples) directory.

```java
HTTPServer server = HTTPServer.builder()
  .port(9400)
  .buildAndStart();
```

By default, `HTTPServer` binds to any IP address, you can change this with
[hostname()](</client_java/api/io/prometheus/metrics/exporter/httpserver/HTTPServer.Builder.html#hostname(java.lang.String)>)
or [inetAddress()](</client_java/api/io/prometheus/metrics/exporter/httpserver/HTTPServer.Builder.html#inetAddress(java.net.InetAddress)>).

`HTTPServer` is configured with three endpoints:

- `/metrics` for Prometheus scraping.
- `/-/healthy` for simple health checks.
- `/` the default handler is a static HTML page.

The default handler can be changed
with [defaultHandler()](</client_java/api/io/prometheus/metrics/exporter/httpserver/HTTPServer.Builder.html#defaultHandler(com.sun.net.httpserver.HttpHandler)>).

## Scrape error handling

By default, scrape failures return a generic HTTP 500 response. Exception details are not
included in the response or logged, because the server may run inside an application or a
Java agent with its own diagnostic pipeline.

Configure a reporter to send exception details to an appropriate logging or telemetry sink:

```java
HTTPServer server = HTTPServer.builder()
  .port(9400)
  .errorHandlingPolicy(
    HttpErrorHandlingPolicy.genericResponseWithReporter(
      error -> logger.log(Level.SEVERE, "Prometheus scrape failed", error)))
  .buildAndStart();
```

The reporter runs synchronously on the request thread and may be called concurrently. Reporter
runtime exceptions do not prevent the generic HTTP 500 response from being sent. Rate limiting
or deduplication can be implemented in the reporter when needed.

`HttpErrorHandlingPolicy.legacyDetailedResponse()` restores the previous response containing the
full exception stack trace. This can disclose application internals and must not be used for an
endpoint reachable by untrusted clients.

## Authentication and HTTPS

- [authenticator()](</client_java/api/io/prometheus/metrics/exporter/httpserver/HTTPServer.Builder.html#authenticator(com.sun.net.httpserver.Authenticator)>)
  is for configuring authentication.
- [httpsConfigurator()](</client_java/api/io/prometheus/metrics/exporter/httpserver/HTTPServer.Builder.html#httpsConfigurator(com.sun.net.httpserver.HttpsConfigurator)>)
  is for configuring HTTPS.

You can find an example of authentication and SSL in the
[jmx_exporter](https://github.com/prometheus/jmx_exporter).

## Properties

See _config_ section (_todo_) on runtime configuration options.

- `io.prometheus.exporter.http_server.port`: The port to bind to.
