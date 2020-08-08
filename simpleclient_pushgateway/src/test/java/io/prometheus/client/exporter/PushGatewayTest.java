package io.prometheus.client.exporter;


import static org.junit.rules.ExpectedException.none;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.junit.MockServerRule;
import org.mockserver.client.server.MockServerClient;

public class PushGatewayTest {


  @Rule
  public final ExpectedException thrown = none();

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

  @Test(expected = RuntimeException.class)
  public void testInvalidURLThrowsRuntimeException() {
    new PushGateway("::"); // ":" is interpreted as port number, so parsing fails
  }

  @Test
  public void testMultipleSlashesAreStrippedFromURL() {
    final PushGateway pushGateway = new PushGateway("example.com:1234/context///path//");
    Assert.assertEquals(
        "http://example.com:1234/context/path/metrics/",
        pushGateway.gatewayBaseURL
    );
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

  @Test
  public void testPush200Response() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(200));
    pg.push(registry, "j");
  }

  @Test
  public void testNon202ResponseThrows() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(500));
    thrown.expect(IOException.class);
    thrown.expectMessage(
            "Response code from http://localhost:"
                    + mockServerRule.getHttpPort()
                    + "/metrics/job/j was 500");
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
  public void testPushWithEmptyLabelGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j/l/v/l2@base64/=")
      ).respond(response().withStatusCode(202));
    groupingKey.put("l2", "");
    pg.push(registry, "j", groupingKey);
  }

  @Test
  public void testPushWithGroupingKeyWithSlashes() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job@base64/YS9i/l/v/l2@base64/75-_Lw==")
      ).respond(response().withStatusCode(202));
    groupingKey.put("l2", "\uF7FF/");
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
          .withPath("/metrics/job/j/instance@base64/=")
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

  @Test
  public void testOldNon202ResponseThrows() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j/instance/i")
      ).respond(response().withStatusCode(500));
    thrown.expect(IOException.class);
    thrown.expectMessage(
            "Response code from http://localhost:"
                    + mockServerRule.getHttpPort()
                    + "/metrics/job/j/instance/i was 500");
    pg.push(registry,"j", "i");
  }

  @Test
  public void testOldPushWithSlashes() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job@base64/YS9i/instance@base64/Yy9k")
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
