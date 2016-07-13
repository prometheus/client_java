package io.prometheus.client.exporter;


import static org.junit.Assert.fail;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import java.io.IOException;;
import java.util.TreeMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.junit.MockServerRule;
import org.mockserver.client.server.MockServerClient;

public class PushGatewayTest {

  static final Vertx vertx = Vertx.vertx();
  static final AsyncResultHandler<Void> NOOP_HANDLER = new AsyncResultHandler<Void>() {
    @Override
    public void handle(AsyncResult<Void> result) {

    }
  };

  @Rule
  public MockServerRule mockServerRule = new MockServerRule(this);
  private MockServerClient mockServerClient;

  CollectorRegistry registry;
  Gauge gauge;
  AsyncPushGateway pg;
  Map groupingKey;

  @Before
  public void setUp() {
    registry = new CollectorRegistry();
    gauge = (Gauge) Gauge.build().name("g").help("help").create();
    pg = new AsyncPushGateway(vertx, "localhost:" + mockServerRule.getPort());
    groupingKey = new TreeMap<String, String>();
    groupingKey.put("l", "v");
  }

  @Test
  public void testPush() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(202));
    pg.push(registry, "j", NOOP_HANDLER);
  }

  @Test(expected=IOException.class)
  public void testNon202ResponseThrows() throws IOException {
    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicBoolean error = new AtomicBoolean(false);
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(500));
    pg.push(registry, "j", new AsyncResultHandler<Void>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        error.set(result.failed());
        latch.countDown();
      }
    });
    try {
      latch.await();
      // the call is async so if the latch was open it means
      // the exception was thrown
      throw new IOException("Not 202");
    } catch (InterruptedException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testPushCollector() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(202));
    pg.push(gauge, "j", NOOP_HANDLER);
  }

  @Test
  public void testPushWithGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j/l/v")
      ).respond(response().withStatusCode(202));
    pg.push(registry, "j", groupingKey, NOOP_HANDLER);
  }

  @Test
  public void testPushWithMultiGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j/l/v/l2/v2")
      ).respond(response().withStatusCode(202));
    groupingKey.put("l2", "v2");
    pg.push(registry, "j", groupingKey, NOOP_HANDLER);
  }

  @Test
  public void testPushWithGroupingKeyWithSlashes() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/a%2Fb/l/v/l2/v%2F2")
      ).respond(response().withStatusCode(202));
    groupingKey.put("l2", "v/2");
    pg.push(registry, "a/b", groupingKey, NOOP_HANDLER);
  }

  @Test
  public void testPushCollectorWithGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("POST")
          .withPath("/metrics/job/j/l/v")
      ).respond(response().withStatusCode(202));
    pg.push(gauge, "j", groupingKey, NOOP_HANDLER);
  }

  @Test
  public void testPushAdd() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(registry, "j", NOOP_HANDLER);
  }

  @Test
  public void testPushAddCollector() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(gauge, "j", NOOP_HANDLER);
  }

  @Test
  public void testPushAddWithGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j/l/v")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(registry, "j", groupingKey, NOOP_HANDLER);
  }

  @Test
  public void testPushAddCollectorWithGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("PUT")
          .withPath("/metrics/job/j/l/v")
      ).respond(response().withStatusCode(202));
    pg.pushAdd(gauge, "j", groupingKey, NOOP_HANDLER);
  }

  @Test
  public void testDelete() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("DELETE")
          .withPath("/metrics/job/j")
      ).respond(response().withStatusCode(202));
    pg.delete("j", NOOP_HANDLER);
  }

  @Test
  public void testDeleteWithGroupingKey() throws IOException {
    mockServerClient.when(
        request()
          .withMethod("DELETE")
          .withPath("/metrics/job/j/l/v")
      ).respond(response().withStatusCode(202));
    pg.delete("j", groupingKey, NOOP_HANDLER);
  }

  @Test
  public void testInstanceIPGroupingKey() throws IOException {
    groupingKey = AsyncPushGateway.instanceIPGroupingKey();
    Assert.assertTrue(!groupingKey.get("instance").equals(""));
  }
}
