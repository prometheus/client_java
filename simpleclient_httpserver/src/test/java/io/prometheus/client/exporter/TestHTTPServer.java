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

import io.prometheus.client.SampleNameFilter;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TestHTTPServer {

  CollectorRegistry registry;

  @Before
  public void init() throws IOException {
    registry = new CollectorRegistry();
    Gauge.build("a", "a help").register(registry);
    Gauge.build("b", "a help").register(registry);
    Gauge.build("c", "a help").register(registry);
  }

  String request(HTTPServer s, String context, String suffix) throws IOException {
    String url = "http://localhost:" + s.server.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.connect();
    Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
    return scanner.hasNext() ? scanner.next() : "";
  }

  String request(HTTPServer s, String suffix) throws IOException {
    return request(s, "/metrics", suffix);
  }

  String requestWithCompression(HTTPServer s, String suffix) throws IOException {
    return requestWithCompression(s, "/metrics", suffix);
  }

  String requestWithCompression(HTTPServer s, String context, String suffix) throws IOException {
    String url = "http://localhost:" + s.server.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
    connection.connect();
    GZIPInputStream gzs = new GZIPInputStream(connection.getInputStream());
    Scanner scanner = new Scanner(gzs).useDelimiter("\\A");
    return scanner.hasNext() ? scanner.next() : "";
  }

  String requestWithAccept(HTTPServer s, String accept) throws IOException {
    String url = "http://localhost:" + s.server.getAddress().getPort();
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setRequestProperty("Accept", accept);
    Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
    return scanner.hasNext() ? scanner.next() : "";
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRefuseUsingUnbound() throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    HTTPServer s = new HTTPServer(HttpServer.create(), registry, true);
    s.stop();
  }

  @Test
  public void testSimpleRequest() throws IOException {
    HTTPServer s = new HTTPServer(new InetSocketAddress(0), registry);
    try {
      String response = request(s, "");
      assertThat(response).contains("a 0.0");
      assertThat(response).contains("b 0.0");
      assertThat(response).contains("c 0.0");
    } finally {
      s.close();
    }
  }

  @Test
  public void testBadParams() throws IOException {
    HTTPServer s = new HTTPServer(new InetSocketAddress(0), registry);
    try {
      String response = request(s, "?x");
      assertThat(response).contains("a 0.0");
      assertThat(response).contains("b 0.0");
      assertThat(response).contains("c 0.0");
    } finally {
      s.close();
    }
  }

  @Test
  public void testSingleName() throws IOException {
    HTTPServer s = new HTTPServer(new InetSocketAddress(0), registry);
    try {
      String response = request(s, "?name[]=a");
      assertThat(response).contains("a 0.0");
      assertThat(response).doesNotContain("b 0.0");
      assertThat(response).doesNotContain("c 0.0");
    } finally {
      s.close();
    }
  }

  @Test
  public void testMultiName() throws IOException {
    HTTPServer s = new HTTPServer(new InetSocketAddress(0), registry);
    try {
      String response = request(s, "?name[]=a&name[]=b");
      assertThat(response).contains("a 0.0");
      assertThat(response).contains("b 0.0");
      assertThat(response).doesNotContain("c 0.0");
    } finally {
      s.close();
    }
  }

  @Test
  public void testSampleNameFilter() throws IOException {
    HTTPServer s = new HTTPServer.Builder()
            .withRegistry(registry)
            .withSampleNameFilter(new SampleNameFilter.Builder()
                    .nameMustNotStartWith("a")
                    .build())
            .build();
    try {
      String response = request(s, "?name[]=a&name[]=b");
      assertThat(response).doesNotContain("a 0.0");
      assertThat(response).contains("b 0.0");
      assertThat(response).doesNotContain("c 0.0");
    } finally {
      s.close();
    }
  }

  @Test
  public void testDecoding() throws IOException {
    HTTPServer s = new HTTPServer(new InetSocketAddress(0), registry);
    try {
      String response = request(s, "?n%61me[]=%61");
      assertThat(response).contains("a 0.0");
      assertThat(response).doesNotContain("b 0.0");
      assertThat(response).doesNotContain("c 0.0");
    } finally {
      s.close();
    }
  }

  @Test
  public void testGzipCompression() throws IOException {
    HTTPServer s = new HTTPServer(new InetSocketAddress(0), registry);
    try {
      String response = requestWithCompression(s, "");
      assertThat(response).contains("a 0.0");
      assertThat(response).contains("b 0.0");
      assertThat(response).contains("c 0.0");
    } finally {
      s.close();
    }
  }

  @Test
  public void testOpenMetrics() throws IOException {
    HTTPServer s = new HTTPServer(new InetSocketAddress(0), registry);
    try {
      String response = requestWithAccept(s, "application/openmetrics-text; version=0.0.1,text/plain;version=0.0.4;q=0.5,*/*;q=0.1");
      assertThat(response).contains("# EOF");
    } finally {
      s.close();
    }
  }

  @Test
  public void testHealth() throws IOException {
    HTTPServer s = new HTTPServer(new InetSocketAddress(0), registry);
    try {
      String response = request(s, "/-/healthy", "");
      assertThat(response).contains("Exporter is Healthy");
    } finally {
      s.close();
    }
  }

  @Test
  public void testHealthGzipCompression() throws IOException {
    HTTPServer s = new HTTPServer(new InetSocketAddress(0), registry);
    try {
      String response = requestWithCompression(s, "/-/healthy", "");
      assertThat(response).contains("Exporter is Healthy");
    } finally {
      s.close();
    }
  }
}
