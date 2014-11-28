package io.prometheus.client.exporter;


import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import java.io.IOException;;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.junit.MockServerRule;
import org.mockserver.client.server.MockServerClient;

public class PushGatewayTest {

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);
  private MockServerClient mockServerClient;

  CollectorRegistry registry;
  Gauge gauge;
  PushGateway pg;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    gauge = (Gauge) Gauge.build().name("g").help("help").create();
    pg = new PushGateway("localhost:" + mockServerRule.getHttpPort());
  }

  @Test
  public void testPushWithoutInstance() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/jobs/j")
      ).respond(response().withStatusCode(202));
    pg.push(registry, "j", "");
  }

  @Test
  public void testPushWithInstance() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/jobs/j/instances/i")
      ).respond(response().withStatusCode(202));
    pg.push(registry, "j", "i");
  }

  @Test(expected=IOException.class)
  public void testNon202ResponseThrows() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/jobs/j/instances/i")
      ).respond(response().withStatusCode(500));
    pg.push(registry,"j", "i");
  }

  @Test
  public void testPushWithSlashes() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/jobs/a%2Fb/instances/c%2Fd")
      ).respond(response().withStatusCode(202));
    pg.push(registry, "a/b", "c/d");
  }

  @Test
  public void testPushCollector() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/jobs/j/instances/i")
      ).respond(response().withStatusCode(202));
    pg.push(gauge, "j", "i");
  }

  @Test
  public void testPushAdd() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/jobs/j/instances/i")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(registry, "j", "i");
  }

  @Test
  public void testPushAddCollector() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/jobs/j/instances/i")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(gauge, "j", "i");
  }

  @Test
  public void testDelete() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("DELETE")
          .withPath("/metrics/jobs/j/instances/i")
      ).respond(response().withStatusCode(202));
    pg.delete("j", "i");
  }
}
