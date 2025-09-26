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

public class HTTPServerTest {

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
          Subject current = Subject.current();
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
}
