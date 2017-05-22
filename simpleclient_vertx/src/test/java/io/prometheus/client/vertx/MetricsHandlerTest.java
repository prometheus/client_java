package io.prometheus.client.vertx;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.vertx.MetricsHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsHandlerTest {

  private static Vertx vertx;
  private static Integer port;
  private static CollectorRegistry registry;

  @BeforeClass
  public static void setUp() throws IOException {
    vertx = Vertx.vertx();
    final Vertx vertx = Vertx.vertx();
    final Router router = Router.router(vertx);

    registry = new CollectorRegistry();
    router.route("/metrics").handler(new MetricsHandler(registry));

    ServerSocket socket = new ServerSocket(0);
    port = socket.getLocalPort();
    socket.close();
    vertx.createHttpServer().requestHandler(router::accept).listen(port);

    Gauge.build("a", "a help").register(registry);
    Gauge.build("b", "b help").register(registry);
    Gauge.build("c", "c help").register(registry);
  }

  @AfterClass
  public static void tearDown() {
    vertx.close();
  }

  @Test
  public void metricsRequest_shouldReturnMetrics() throws IOException {
    String out = makeRequest("/metrics");

    assertThat(out).contains("a 0.0");
    assertThat(out).contains("b 0.0");
    assertThat(out).contains("c 0.0");
  }

  @Test
  public void metricsRequest_shouldAllowFilteringMetrics() throws IOException {
    String out = makeRequest("/metrics?name[]=b&name[]=c");

    assertThat(out).doesNotContain("a 0.0");
    assertThat(out).contains("b 0.0");
    assertThat(out).contains("c 0.0");
  }


  private String makeRequest(String url) throws IOException {
    Scanner scanner = new Scanner(new URL("http://localhost:" + port + url).openStream(), "UTF-8")
            .useDelimiter("\\A");
    String out = scanner.next();
    scanner.close();
    return out;
  }
}
