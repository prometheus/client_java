package io.prometheus.metrics.exporter.httpserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpsConfigurator;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.registry.PrometheusScrapeRequest;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.security.auth.Subject;
import org.junit.jupiter.api.Test;

class HTTPServerTest {

  @Test
  @SuppressWarnings({"removal"})
  public void testSubjectDoAs() throws Exception {

    final String user = "joe";
    final Subject subject = new Subject();
    subject.getPrincipals().add(() -> user);

    Authenticator authenticator =
        new Authenticator() {
          @Override
          public Result authenticate(HttpExchange exchange) {
            exchange.setAttribute("aa", subject);
            return new Success(new HttpPrincipal(user, "/"));
          }
        };

    HttpHandler handler =
        exchange -> {
          boolean found = false;
          Subject current = getCurrentSubject();
          for (Principal p : current.getPrincipals()) {
            if (user.equals(p.getName())) {
              found = true;
              break;
            }
          }
          if (!found) {
            throw new IOException("Expected validated user joe!");
          }
          exchange.sendResponseHeaders(204, -1);
        };
    HTTPServer server =
        HTTPServer.builder()
            .port(0)
            .authenticator(authenticator)
            .defaultHandler(handler)
            .authenticatedSubjectAttributeName("aa")
            .buildAndStart();

    run(server, "204", "/");
  }

  private static void run(HTTPServer server, String expected, String path) throws IOException {
    try (Socket socket = new Socket()) {
      socket.connect(new InetSocketAddress("localhost", server.getPort()));

      socket
          .getOutputStream()
          .write(("GET " + path + " HTTP/1.1 \r\n").getBytes(StandardCharsets.UTF_8));
      socket.getOutputStream().write("HOST: localhost \r\n\r\n".getBytes(StandardCharsets.UTF_8));
      socket.getOutputStream().flush();

      String actualResponse = "";
      byte[] resp = new byte[500];
      int read = socket.getInputStream().read(resp, 0, resp.length);
      if (read > 0) {
        actualResponse = new String(resp, 0, read, StandardCharsets.UTF_8);
      }
      assertThat(actualResponse).contains(expected);
    }
  }

  @Test
  void defaultHandler() throws IOException {
    run(HTTPServer.builder().port(0).buildAndStart(), "200", "/");
  }

  @Test
  void metrics() throws IOException {
    run(
        HTTPServer.builder()
            .port(0)
            .registry(new PrometheusRegistry())
            .executorService(Executors.newFixedThreadPool(1))
            .buildAndStart(),
        "200",
        "/metrics");
  }

  @Test
  void metricsCustomPath() throws IOException {
    run(
        HTTPServer.builder()
            .port(0)
            .registry(new PrometheusRegistry())
            .metricsHandlerPath("/my-metrics")
            .executorService(Executors.newFixedThreadPool(1))
            .buildAndStart(),
        "200",
        "/my-metrics");
  }

  @Test
  void registryThrows() throws IOException {
    HTTPServer server =
        HTTPServer.builder()
            .port(0)
            .registry(
                new PrometheusRegistry() {
                  @Override
                  public MetricSnapshots scrape(PrometheusScrapeRequest scrapeRequest) {
                    throw new IllegalStateException("test");
                  }
                })
            .buildAndStart();
    run(server, "500", "/metrics");
  }

  @Test
  void config() throws NoSuchAlgorithmException, IOException {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                HTTPServer.builder()
                    .port(0)
                    .hostname("localhost")
                    .inetAddress(InetAddress.getByName("localhost"))
                    .buildAndStart())
        .withMessage("cannot configure 'inetAddress' and 'hostname' at the same time");

    // ssl doesn't work without in tests
    run(
        HTTPServer.builder()
            .port(0)
            .httpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()))
            .buildAndStart(),
        "",
        "/");
  }

  @Test
  void health() throws IOException {
    run(HTTPServer.builder().port(0).buildAndStart(), "200", "/-/healthy");
  }

  @Test
  void healthEnabled() throws IOException {
    HttpHandler handler = exchange -> exchange.sendResponseHeaders(204, -1);
    run(
        HTTPServer.builder()
            .port(0)
            .defaultHandler(handler)
            .registerHealthHandler(true)
            .buildAndStart(),
        "200",
        "/-/healthy");
  }

  @Test
  void healthDisabled() throws IOException {
    HttpHandler handler = exchange -> exchange.sendResponseHeaders(204, -1);
    run(
        HTTPServer.builder()
            .port(0)
            .defaultHandler(handler)
            .registerHealthHandler(false)
            .buildAndStart(),
        "204",
        "/-/healthy");
  }

  /**
   * Get current Subject using reflection to support both Java 17 and Java 18+.
   *
   * <p>Java 18+ has Subject.current(), but Java 17 and earlier require
   * Subject.getSubject(AccessController.getContext()).
   */
  @SuppressWarnings({"removal"})
  private static Subject getCurrentSubject() {
    try {
      Method currentMethod = Subject.class.getMethod("current");
      return (Subject) currentMethod.invoke(null);
    } catch (NoSuchMethodException e) {
      // Fall back to pre-Java 18 API
      try {
        return Subject.getSubject(java.security.AccessController.getContext());
      } catch (Exception ex) {
        throw new RuntimeException("Failed to get current Subject", ex);
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to invoke Subject.current()", e);
    }
  }
}
