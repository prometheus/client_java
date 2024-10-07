package io.prometheus.metrics.exporter.pushgateway;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

class BasicAuthPushGatewayTest {
  private MockServerClient mockServerClient;

  PrometheusRegistry registry;
  Gauge gauge;
  PushGateway pushGateway;

  @BeforeEach
  public void setUp() {
    mockServerClient = ClientAndServer.startClientAndServer(0);
    registry = new PrometheusRegistry();
    gauge = Gauge.builder().name("g").help("help").build();
    pushGateway =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .basicAuth("testUser", "testPwd")
            .registry(registry)
            .job("j")
            .build();
  }

  @AfterEach
  void tearDown() {
    mockServerClient.stop();
  }

  @Test
  public void testAuthorizedPush() throws IOException {
    mockServerClient
        .when(
            request()
                .withMethod("PUT")
                .withHeader("Authorization", "Basic dGVzdFVzZXI6dGVzdFB3ZA==")
                .withPath("/metrics/job/j"))
        .respond(response().withStatusCode(202));
    pushGateway.push();
  }
}
