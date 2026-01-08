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
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.security.auth.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HTTPServerTest {

  private PrometheusRegistry registry;

  @BeforeEach
  void setUp() {
    final MetricMetadata metadata = new MetricMetadata("my-counter");
    final CounterDataPointSnapshot dataPointSnapshot =
        new CounterDataPointSnapshot(1.0, Labels.EMPTY, null, System.currentTimeMillis());

    registry = new PrometheusRegistry();
    registry.register(() -> new CounterSnapshot(metadata, List.of(dataPointSnapshot)));
  }

  @Test
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

    run(server, "/", 204, "");
  }

  @Test
  void defaultHandler() throws Exception {
    run(
        HTTPServer.builder().port(0).buildAndStart(),
        "/",
        200,
        "<title>Prometheus Java Client</title>");
  }

  @Test
  void metrics() throws Exception {
    run(
        HTTPServer.builder()
            .port(0)
            .registry(registry)
            .executorService(Executors.newFixedThreadPool(1))
            .buildAndStart(),
        "/metrics",
        200,
        "my_counter_total 1.0");
  }

  @Test
  void metricsCustomPath() throws Exception {
    run(
        HTTPServer.builder()
            .port(0)
            .registry(registry)
            .metricsHandlerPath("/my-metrics")
            .executorService(Executors.newFixedThreadPool(1))
            .buildAndStart(),
        "/my-metrics",
        200,
        "my_counter_total 1.0");
  }

  @Test
  void metricsCustomRootPath() throws Exception {
    run(
        HTTPServer.builder()
            .port(0)
            .registry(registry)
            .metricsHandlerPath("/")
            .executorService(Executors.newFixedThreadPool(1))
            .buildAndStart(),
        "/",
        200,
        "my_counter_total 1.0");
  }

  @Test
  void registryThrows() throws Exception {
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
    run(server, "/metrics", 500, "An Exception occurred while scraping metrics");
  }

  @Test
  @SuppressWarnings("resource")
  void config() {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                HTTPServer.builder()
                    .port(0)
                    .hostname("localhost")
                    .inetAddress(InetAddress.getByName("localhost"))
                    .buildAndStart())
        .withMessage("cannot configure 'inetAddress' and 'hostname' at the same time");

    // SSL doesn't work in this simple test configuration
    assertThatExceptionOfType(IOException.class)
        .isThrownBy(
            () ->
                run(
                    HTTPServer.builder()
                        .port(0)
                        .httpsConfigurator(new HttpsConfigurator(SSLContext.getDefault()))
                        .buildAndStart(),
                    "/",
                    0,
                    "ignored"));
  }

  @Test
  void health() throws Exception {
    run(HTTPServer.builder().port(0).buildAndStart(), "/-/healthy", 200, "Exporter is healthy.");
  }

  @Test
  void healthEnabled() throws Exception {
    HttpHandler handler = exchange -> exchange.sendResponseHeaders(204, -1);
    run(
        HTTPServer.builder()
            .port(0)
            .defaultHandler(handler)
            .registerHealthHandler(true)
            .buildAndStart(),
        "/-/healthy",
        200,
        "Exporter is healthy.");
  }

  @Test
  void healthDisabled() throws Exception {
    HttpHandler handler = exchange -> exchange.sendResponseHeaders(204, -1);
    run(
        HTTPServer.builder()
            .port(0)
            .defaultHandler(handler)
            .registerHealthHandler(false)
            .buildAndStart(),
        "/-/healthy",
        204,
        "");
  }

  private static void run(
      HTTPServer server, String path, int expectedStatusCode, String expectedBody)
      throws Exception {
    // we cannot use try-with-resources or even client.close(), or the test will fail with Java 17
    @SuppressWarnings("resource")
    final HttpClient client = HttpClient.newBuilder().build();
    try {
      final URI uri = URI.create("http://localhost:%s%s".formatted(server.getPort(), path));
      final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

      final HttpResponse<String> response =
          client.send(request, HttpResponse.BodyHandlers.ofString());
      assertThat(response.statusCode()).isEqualTo(expectedStatusCode);
      assertThat(response.body()).contains(expectedBody);
    } finally {
      server.stop();
    }
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
