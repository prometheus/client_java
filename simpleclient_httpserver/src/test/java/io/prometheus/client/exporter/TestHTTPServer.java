package io.prometheus.client.exporter;

import com.sun.net.httpserver.HttpServer;
import io.prometheus.client.Gauge;
import io.prometheus.client.CollectorRegistry;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHTTPServer {

  HTTPServer s;
  private final CollectorRegistry registry = new CollectorRegistry();

  @Before
  public void init() throws IOException {
    Gauge.build("a", "a help").register(registry);
    Gauge.build("b", "a help").register(registry);
    Gauge.build("c", "a help").register(registry);

    s = new HTTPServer(new InetSocketAddress(0), registry);
  }

  @After
  public void cleanup() {
    s.stop();
  }


  String request(String context, String suffix) throws IOException {
    String url = "http://localhost:" + s.server.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.connect();
    Scanner s = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  String request(String suffix) throws IOException {
    return request("/metrics", suffix);
  }

  String requestWithCompression(String suffix) throws IOException {
    return requestWithCompression("/metrics", suffix);
  }

  String requestWithCompression(String context, String suffix) throws IOException {
    String url = "http://localhost:" + s.server.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
    connection.connect();
    GZIPInputStream gzs = new GZIPInputStream(connection.getInputStream());
    Scanner s = new Scanner(gzs).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRefuseUsingUnbound() throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    HTTPServer s = new HTTPServer(HttpServer.create(), registry, true);
    s.stop();
  }

  @Test
  public void testSimpleRequest() throws IOException {
    String response = request("");
    assertThat(response).contains("a 0.0");
    assertThat(response).contains("b 0.0");
    assertThat(response).contains("c 0.0");
  }

  @Test
  public void testBadParams() throws IOException {
    String response = request("?x");
    assertThat(response).contains("a 0.0");
    assertThat(response).contains("b 0.0");
    assertThat(response).contains("c 0.0");
  }

  @Test
  public void testSingleName() throws IOException {
    String response = request("?name[]=a");
    assertThat(response).contains("a 0.0");
    assertThat(response).doesNotContain("b 0.0");
    assertThat(response).doesNotContain("c 0.0");
  }

  @Test
  public void testMultiName() throws IOException {
    String response = request("?name[]=a&name[]=b");
    assertThat(response).contains("a 0.0");
    assertThat(response).contains("b 0.0");
    assertThat(response).doesNotContain("c 0.0");
  }

  @Test
  public void testDecoding() throws IOException {
    String response = request("?n%61me[]=%61");
    assertThat(response).contains("a 0.0");
    assertThat(response).doesNotContain("b 0.0");
    assertThat(response).doesNotContain("c 0.0");
  }

  @Test
  public void testGzipCompression() throws IOException {
    String response = requestWithCompression("");
    assertThat(response).contains("a 0.0");
    assertThat(response).contains("b 0.0");
    assertThat(response).contains("c 0.0");
  }

  @Test
  public void testHealth() throws IOException {
    String response = request("/-/healthy", "");
    assertThat(response).contains("Exporter is Healthy");
  }

  @Test
  public void testHealthGzipCompression() throws IOException {
    String response = requestWithCompression("/-/healthy", "");
    assertThat(response).contains("Exporter is Healthy");
  }

  @Test
  public void testMetricsContext() throws IOException {
    final String metricsContext = "/metricz";
    s = new HTTPServer(new InetSocketAddress(0), registry, true, metricsContext);
    String response = request(metricsContext, "");
    assertThat(response).contains("a 0.0");
    assertThat(response).contains("b 0.0");
    assertThat(response).contains("c 0.0");
  }

  @Test
  public void testMetricsContextGzipCompression() throws IOException {
    final String metricsContext = "/metricz";
    s = new HTTPServer(new InetSocketAddress(0), registry, metricsContext);
    String response = requestWithCompression(metricsContext, "");
    assertThat(response).contains("a 0.0");
    assertThat(response).contains("b 0.0");
    assertThat(response).contains("c 0.0");
  }
}
