package io.prometheus.client.exporter;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpServer;
import io.prometheus.client.Gauge;
import io.prometheus.client.CollectorRegistry;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import io.prometheus.client.SampleNameFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

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

  Response request(String requestMethod, HTTPServer s, String context, String suffix) throws IOException {
    String url = "http://localhost:" + s.server.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    ((HttpURLConnection)connection).setRequestMethod(requestMethod);
    connection.setDoOutput(true);
    connection.connect();
    Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
    return new Response(connection.getContentLength(), scanner.hasNext() ? scanner.next() : "");
  }

  Response request(HTTPServer s, String context, String suffix) throws IOException {
    return request("GET", s, context, suffix);
  }

  Response request(HTTPServer s, String suffix) throws IOException {
    return request(s, "/metrics", suffix);
  }

  Response requestWithCompression(HTTPServer s, String suffix) throws IOException {
    return requestWithCompression(s, "/metrics", suffix);
  }

  Response requestWithCompression(HTTPServer s, String context, String suffix) throws IOException {
    String url = "http://localhost:" + s.server.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
    connection.connect();
    GZIPInputStream gzs = new GZIPInputStream(connection.getInputStream());
    Scanner scanner = new Scanner(gzs).useDelimiter("\\A");
    return new Response(connection.getContentLength(), scanner.hasNext() ? scanner.next() : "");
  }

  Response requestWithAccept(HTTPServer s, String accept) throws IOException {
    String url = "http://localhost:" + s.server.getAddress().getPort();
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.setDoInput(true);
    connection.setRequestProperty("Accept", accept);
    Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
    return new Response(connection.getContentLength(), scanner.hasNext() ? scanner.next() : "");
  }

  Response requestWithCredentials(HTTPServer httpServer, String context, String suffix, String user, String password) throws IOException {
    String url = "http://localhost:" + httpServer.server.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    if (user != null && password != null) {
      connection.setRequestProperty("Authorization", encodeCredentials(user, password));
    }
    connection.connect();
    Scanner s = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
    return new Response(connection.getContentLength(), s.hasNext() ? s.next() : "");
  }

  Response request(HttpServer httpServer, String context, String suffix) throws IOException {
    String url = "http://localhost:" + httpServer.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.connect();
    Scanner s = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
    return new Response(connection.getContentLength(), s.hasNext() ? s.next() : "");
  }

  String encodeCredentials(String user, String password) {
    // Per RFC4648 table 2. We support Java 6, and java.util.Base64 was only added in Java 8,
    try {
      byte[] credentialsBytes = (user + ":" + password).getBytes("UTF-8");
      String encoded = DatatypeConverter.printBase64Binary(credentialsBytes);
      encoded = String.format("Basic %s", encoded);
      return encoded;
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  Authenticator createAuthenticator(String realm, final String validUsername, final String validPassword) {
    return new BasicAuthenticator(realm) {
      @Override
      public boolean checkCredentials(String username, String password) {
        return validUsername.equals(username) && validPassword.equals(password);
      }
    };
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
      String response = request(s, "").body;
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
      String response = request(s, "?x").body;
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
      String response = request(s, "?name[]=a").body;
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
      String response = request(s, "?name[]=a&name[]=b").body;
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
      String response = request(s, "?name[]=a&name[]=b").body;
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
      String response = request(s, "?n%61me[]=%61").body;
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
      String response = requestWithCompression(s, "").body;
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
      String response = requestWithAccept(s, "application/openmetrics-text; version=0.0.1,text/plain;version=0.0.4;q=0.5,*/*;q=0.1").body;
      assertThat(response).contains("# EOF");
    } finally {
      s.close();
    }
  }

  @Test
  public void testHealth() throws IOException {
    HTTPServer s = new HTTPServer(new InetSocketAddress(0), registry);
    try {
      String response = request(s, "/-/healthy", "").body;
      assertThat(response).contains("Exporter is Healthy");
    } finally {
      s.close();
    }
  }

  @Test
  public void testHealthGzipCompression() throws IOException {
    HTTPServer s = new HTTPServer(new InetSocketAddress(0), registry);
    try {
      String response = requestWithCompression(s, "/-/healthy", "").body;
      assertThat(response).contains("Exporter is Healthy");
    } finally {
      s.close();
    }
  }

  @Test
  public void testBasicAuthSuccess() throws IOException {
    HTTPServer s = new HTTPServer.Builder()
            .withRegistry(registry)
            .withAuthenticator(createAuthenticator("/", "user", "secret"))
            .build();
    try {
      String response = requestWithCredentials(s, "/metrics","?name[]=a&name[]=b", "user", "secret").body;
      assertThat(response).contains("a 0.0");
    } finally {
      s.close();
    }
  }

  @Test
  public void testBasicAuthCredentialsMissing() throws IOException {
    HTTPServer s = new HTTPServer.Builder()
            .withRegistry(registry)
            .withAuthenticator(createAuthenticator("/", "user", "secret"))
            .build();
    try {
      request(s, "/metrics", "?name[]=a&name[]=b");
      Assert.fail("expected IOException with HTTP 401");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("401"));
    } finally {
      s.close();
    }
  }

  @Test
  public void testBasicAuthWrongCredentials() throws IOException {
    HTTPServer s = new HTTPServer.Builder()
            .withRegistry(registry)
            .withAuthenticator(createAuthenticator("/", "user", "wrong"))
            .build();
    try {
      request(s, "/metrics", "?name[]=a&name[]=b");
      Assert.fail("expected IOException with HTTP 401");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("401"));
    } finally {
      s.close();
    }
  }

  @Test
  public void testHEADRequest() throws IOException {
    HTTPServer s = new HTTPServer.Builder()
            .withRegistry(registry)
            .build();
    try {
      Response response = request("HEAD", s, "/metrics", "?name[]=a&name[]=b");
      Assert.assertTrue(response.contentLength == 74);
      Assert.assertTrue("".equals(response.body));
    } finally {
      s.close();
    }
  }

  @Test
  public void testSimpleRequestHttpServerWithHTTPMetricHandler() throws IOException {
    InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 0);
    HttpServer httpServer = HttpServer.create(inetSocketAddress, 0);
    httpServer.createContext("/metrics", new HTTPServer.HTTPMetricHandler(registry));
    httpServer.start();

    try {
      String response = request(httpServer, "/metrics", null).body;
      assertThat(response).contains("a 0.0");
      assertThat(response).contains("b 0.0");
      assertThat(response).contains("c 0.0");
    } finally {
      httpServer.stop(0);
    }
  }

  class Response {

    public long contentLength;
    public String body;

    public Response(long contentLength, String body) {
      this.contentLength = contentLength;
      this.body = body;
    }
  }
}
