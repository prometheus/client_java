package io.prometheus.client.exporter;


import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import java.io.IOException;;
import java.util.TreeMap;
import java.util.Map;
import org.junit.Assert;
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
  Map groupingKey;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    gauge = (Gauge) Gauge.build().name("g").help("help").create();
    pg = new PushGateway("localhost:" + mockServerRule.getHttpPort());
    groupingKey = new TreeMap<String, String>();
    groupingKey.put("l", "v");
  }

  @Test
  public void testPush() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(202));
    pg.push(registry, "j");
  }

  @Test(expected=IOException.class)
  public void testNon202ResponseThrows() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(500));
    pg.push(registry, "j");
  }

  @Test
  public void testPushCollector() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(202));
    pg.push(gauge, "j");
  }

  @Test
  public void testPushWithGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j/l/v")
      ).respond(response().withStatusCode(202));
    pg.push(registry, "j", groupingKey);
  }

  @Test
  public void testPushWithMultiGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j/l/v/l2/v2")
      ).respond(response().withStatusCode(202));
    groupingKey.put("l2", "v2");
    pg.push(registry, "j", groupingKey);
  }

  @Test
  public void testPushWithGroupingKeyWithSlashes() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/a%2Fb/l/v/l2/v%2F2")
      ).respond(response().withStatusCode(202));
    groupingKey.put("l2", "v/2");
    pg.push(registry, "a/b", groupingKey);
  }

  @Test
  public void testPushCollectorWithGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j/l/v")
      ).respond(response().withStatusCode(202));
    pg.push(gauge, "j", groupingKey);
  }

  @Test
  public void testPushAdd() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(registry, "j");
  }

  @Test
  public void testPushAddCollector() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(gauge, "j");
  }

  @Test
  public void testPushAddWithGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j/l/v")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(registry, "j", groupingKey);
  }

  @Test
  public void testPushAddCollectorWithGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j/l/v")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(gauge, "j", groupingKey);
  }

  @Test
  public void testDelete() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("DELETE")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(202));
    pg.delete("j");
  }

  @Test
  public void testDeleteWithGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("DELETE")
          .withPath("/metrics/job/j/l/v")
      ).respond(response().withStatusCode(202));
    pg.delete("j", groupingKey);
  }



  @Test
  public void testOldPushWithoutInstance() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j/instance/")
      ).respond(response().withStatusCode(202));
    pg.push(registry, "j", "");
  }

  @Test
  public void testOldPushWithInstance() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j/instance/i")
      ).respond(response().withStatusCode(202));
    pg.push(registry, "j", "i");
  }

  @Test(expected=IOException.class)
  public void testOldNon202ResponseThrows() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j/instance/i")
      ).respond(response().withStatusCode(500));
    pg.push(registry,"j", "i");
  }

  @Test
  public void testOldPushWithSlashes() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/a%2Fb/instance/c%2Fd")
      ).respond(response().withStatusCode(202));
    pg.push(registry, "a/b", "c/d");
  }

  @Test
  public void testOldPushCollector() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j/instance/i")
      ).respond(response().withStatusCode(202));
    pg.push(gauge, "j", "i");
  }

  @Test
  public void testOldPushAdd() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j/instance/i")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(registry, "j", "i");
  }

  @Test
  public void testOldPushAddCollector() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j/instance/i")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(gauge, "j", "i");
  }

  @Test
  public void testOldDelete() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("DELETE")
          .withPath("/metrics/job/j/instance/i")
      ).respond(response().withStatusCode(202));
    pg.delete("j", "i");
  }

  @Test
  public void testInstanceIPGroupingKey() throws IOException {
    groupingKey = PushGateway.instanceIPGroupingKey();
    Assert.assertTrue(!groupingKey.get("instance").equals(""));
  }
}
