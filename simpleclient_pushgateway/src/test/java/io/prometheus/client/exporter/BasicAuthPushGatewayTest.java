package io.prometheus.client.exporter;


import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;

public class BasicAuthPushGatewayTest {

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);
  private MockServerClient mockServerClient;

  CollectorRegistry registry;
  Gauge gauge;
  PushGateway pushGateway;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    gauge = Gauge.build().name("g").help("help").create();
    pushGateway = new PushGateway("localhost:" + mockServerRule.getPort());
    pushGateway.setConnectionFactory(new BasicAuthHttpConnectionFactory("testUser", "testPwd"));
  }

  @Test
  public void testAuthorizedPush() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withHeader("Authorization", "Basic dGVzdFVzZXI6dGVzdFB3ZA==")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(202));
    pushGateway.push(registry, "j");
  }
}
