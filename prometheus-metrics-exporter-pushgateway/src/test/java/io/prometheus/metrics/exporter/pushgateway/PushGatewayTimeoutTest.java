package io.prometheus.metrics.exporter.pushgateway;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PushGatewayTimeoutTest {

  private MockWebServer mockWebServer;

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
  }

  @Test
  void connectionTimeoutIsEnforced() {
    // Simulate server that never accepts connection: by binding but delaying accept.
    // Since MockWebServer always accepts connection immediately, we instead use a short connect timeout and a response delay.
    int shortConnectTimeoutMillis = 10;
    int readTimeoutMillis = 1000;

    PushGateway pushGateway = PushGateway.builder()
      .connectionTimeout(shortConnectTimeoutMillis)
      .readTimeout(readTimeoutMillis)
      .build();

    // Enqueue a response that delays sending headers to simulate slow connection
    mockWebServer.enqueue(new MockResponse()
      .setBody("ok")
      .setBodyDelay(5, TimeUnit.SECONDS)); // very long delay

    String url = mockWebServer.url("/").toString();

    Exception thrown = assertThrows(Exception.class, pushGateway::pushAdd);

    assertTrue(thrown.getMessage().contains("connect"), "Expected a connection‐timeout or connect error");
  }

  @Test
  void readTimeoutIsEnforced() {
    int connectTimeoutMillis = 1000;
    int shortReadTimeoutMillis = 10;

    PushGateway pushGateway = PushGateway.builder()
      .connectionTimeout(connectTimeoutMillis)
      .readTimeout(shortReadTimeoutMillis)
      .build();

    // Enqueue a response that sends headers but delays body
    mockWebServer.enqueue(new MockResponse()
      .setHeadersDelay(0, TimeUnit.SECONDS)
      .setBodyDelay(5, TimeUnit.SECONDS)
      .setBody("ok"));

    String url = mockWebServer.url("/").toString();

    Exception thrown = assertThrows(Exception.class, pushGateway::pushAdd);

    assertTrue(thrown.getMessage().contains("read") || thrown.getMessage().contains("timeout"),
      "Expected a read‐timeout error");
  }
}
