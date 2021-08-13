package io.prometheus.client.exporter;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpServer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TestHTTPServerBasicAuthentication {

  private static final String HTTP_USER = "prometheus";
  private static final String HTTP_PASSWORD = "some_password";

  HTTPServer s;

  @Before
  public void init() throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    Gauge.build("a", "a help").register(registry);
    Gauge.build("b", "a help").register(registry);
    Gauge.build("c", "a help").register(registry);

    Authenticator authenticator = new BasicAuthenticator("/") {
      @Override
      public boolean checkCredentials(String user, String password) {
        return HTTP_USER.equals(user) && HTTP_PASSWORD.equals(password);
      }
    };

    SSLContext sslContext = null;

    HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 3);
    s = new HTTPServer(httpServer, authenticator, registry, false);
  }

  @After
  public void cleanup() {
    s.stop();
  }

  String request(String context, String suffix) throws IOException {
    return request(context, suffix, HTTP_USER, HTTP_PASSWORD);
  }

  String request(String context, String suffix, String user, String password) throws IOException {
    String url = "http://localhost:" + s.server.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    if (user != null && password != null) {
      connection.setRequestProperty("Authorization", encodeCredentials(user, password));
    }
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
    return requestWithCompression(context, suffix, HTTP_USER, HTTP_PASSWORD);
  }

  String requestWithCompression(String context, String suffix, String user, String password) throws IOException {
    String url = "http://localhost:" + s.server.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.setDoInput(true);
    if (user != null && password != null) {
      connection.setRequestProperty("Authorization", encodeCredentials(user, password));
    }
    connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
    connection.connect();
    GZIPInputStream gzs = new GZIPInputStream(connection.getInputStream());
    Scanner s = new Scanner(gzs).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  String requestWithAccept(String accept) throws IOException {
    return requestWithAccept(accept, HTTP_USER, HTTP_PASSWORD);
  }

  String requestWithAccept(String accept, String user, String password) throws IOException {
    String url = "http://localhost:" + s.server.getAddress().getPort();
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.setDoInput(true);
    if (user != null && password != null) {
      connection.setRequestProperty("Authorization", encodeCredentials(user, password));
    }
    connection.setRequestProperty("Accept", accept);
    Scanner s = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }

  private static String encodeCredentials(String user, String password) {
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
  public void testOpenMetrics() throws IOException {
    String response = requestWithAccept("application/openmetrics-text; version=0.0.1,text/plain;version=0.0.4;q=0.5,*/*;q=0.1");
    assertThat(response).contains("# EOF");
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

  @Test(expected = IOException.class)
  public void testHealthyBasicAuthenticationFailure() throws IOException {
    String response = request("/-/healthy", "", null, null);
    fail("Expected IOException");
  }
}
