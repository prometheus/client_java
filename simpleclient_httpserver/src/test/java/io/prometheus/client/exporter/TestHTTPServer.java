package io.prometheus.client.exporter;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import io.prometheus.client.Gauge;
import io.prometheus.client.CollectorRegistry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Scanner;;
import java.util.zip.GZIPInputStream;

import io.prometheus.client.SampleNameFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TestHTTPServer {

  CollectorRegistry registry;

  private final static SSLContext SSL_CONTEXT;

  private final static HttpsConfigurator HTTPS_CONFIGURATOR;

  // Code put in a static block due to possible Exceptions
  static {
    try {
      SSL_CONTEXT = createSSLContext(
              "SSL",
              "PKCS12",
              "./src/test/resources/keystore.pkcs12",
              "changeit");
    } catch (GeneralSecurityException e) {
      throw new RuntimeException("Exception creating SSL_CONTEXT", e);
    } catch (IOException e) {
      throw new RuntimeException("Exception creating SSL_CONTEXT", e);
    }

    HTTPS_CONFIGURATOR = createHttpsConfigurator(SSL_CONTEXT);
  }

  private final static TrustManager[] TRUST_MANAGERS = new TrustManager[]{
          new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[0];
            }

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
          }
  };

  private final static HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
    @Override
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
  };

  final static Authenticator createAuthenticator(String realm, final String validUsername, final String validPassword) {
    return new BasicAuthenticator(realm) {
      @Override
      public boolean checkCredentials(String username, String password) {
        return validUsername.equals(username) && validPassword.equals(password);
      }
    };
  }

  class Response {

    public long contentLength;
    public String body;

    public Response(long contentLength, String body) {
      this.contentLength = contentLength;
      this.body = body;
    }
  }

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

  Response requestWithCredentials(HTTPServer httpServer, String context, String suffix, String username, String password) throws IOException {
    String url = "http://localhost:" + httpServer.server.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    if (username != null && password != null) {
      connection.setRequestProperty("Authorization", encodeCredentials(username, password));
    }
    connection.connect();
    Scanner s = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
    return new Response(connection.getContentLength(), s.hasNext() ? s.next() : "");
  }

  Response requestWithSSL(String requestMethod, String username, String password, HTTPServer s, String context, String suffix) throws GeneralSecurityException, IOException {
    String url = "https://localhost:" + s.server.getAddress().getPort() + context + suffix;

    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, TRUST_MANAGERS, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    HttpsURLConnection.setDefaultHostnameVerifier(HOSTNAME_VERIFIER);

    URLConnection connection = new URL(url).openConnection();
    ((HttpURLConnection)connection).setRequestMethod(requestMethod);

    if (username != null && password != null) {
      connection.setRequestProperty("Authorization", encodeCredentials(username, password));
    }

    connection.setDoOutput(true);
    connection.connect();
    Scanner scanner = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
    return new Response(connection.getContentLength(), scanner.hasNext() ? scanner.next() : "");
  }

  Response request(HttpServer httpServer, String context, String suffix) throws IOException {
    String url = "http://localhost:" + httpServer.getAddress().getPort() + context + suffix;
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.connect();
    Scanner s = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A");
    return new Response(connection.getContentLength(), s.hasNext() ? s.next() : "");
  }

  String encodeCredentials(String username, String password) {
    // Per RFC4648 table 2. We support Java 6, and java.util.Base64 was only added in Java 8,
    try {
      byte[] credentialsBytes = (username + ":" + password).getBytes("UTF-8");
      return "Basic " +  DatatypeConverter.printBase64Binary(credentialsBytes);
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRefuseUsingUnbound() throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    HTTPServer s = new HTTPServer(HttpServer.create(), registry, true);
    s.close();
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

      Assert.assertNotNull(response);
      Assert.assertTrue(response.contentLength == 74);
      Assert.assertTrue("".equals(response.body));
    } finally {
      s.close();
    }
  }

  @Test
  public void testHEADRequestWithSSL() throws GeneralSecurityException, IOException {
    HTTPServer s = new HTTPServer.Builder()
            .withRegistry(registry)
            .withHttpsConfigurator(HTTPS_CONFIGURATOR)
            .build();

    try {
      Response response = requestWithSSL(
              "HEAD", null, null, s, "/metrics", "?name[]=a&name[]=b");

      Assert.assertNotNull(response);
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

  @Test
  public void testHEADRequestWithSSLAndBasicAuthSuccess() throws GeneralSecurityException, IOException {
    HTTPServer s = new HTTPServer.Builder()
            .withRegistry(registry)
            .withHttpsConfigurator(HTTPS_CONFIGURATOR)
            .withAuthenticator(createAuthenticator("/", "user", "secret"))
            .build();

    try {
      Response response = requestWithSSL(
              "HEAD", "user", "secret", s, "/metrics", "?name[]=a&name[]=b");

      Assert.assertNotNull(response);
      Assert.assertTrue(response.contentLength == 74);
      Assert.assertTrue("".equals(response.body));
    } finally {
      s.close();
    }
  }

  @Test
  public void testHEADRequestWithSSLAndBasicAuthCredentialsMissing() throws GeneralSecurityException, IOException {
    HTTPServer s = new HTTPServer.Builder()
            .withRegistry(registry)
            .withHttpsConfigurator(HTTPS_CONFIGURATOR)
            .withAuthenticator(createAuthenticator("/", "user", "secret"))
            .build();

    try {
      Response response = requestWithSSL("HEAD", null, null, s, "/metrics", "?name[]=a&name[]=b");
      Assert.fail("expected IOException with HTTP 401");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("401"));
    } finally {
      s.close();
    }
  }

  @Test
  public void testHEADRequestWithSSLAndBasicAuthWrongCredentials() throws GeneralSecurityException, IOException {
    HTTPServer s = new HTTPServer.Builder()
            .withRegistry(registry)
            .withHttpsConfigurator(HTTPS_CONFIGURATOR)
            .withAuthenticator(createAuthenticator("/", "user", "secret"))
            .build();

    try {
      Response response = requestWithSSL("HEAD", "user", "wrong", s, "/metrics", "?name[]=a&name[]=b");
      Assert.fail("expected IOException with HTTP 401");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("401"));
    } finally {
      s.close();
    }
  }
  /**
   * Create an SSLContext
   *
   * @param sslContextType
   * @param keyStoreType
   * @param keyStorePath
   * @param keyStorePassword
   * @return SSLContext
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public static SSLContext createSSLContext(String sslContextType, String keyStoreType, String keyStorePath, String keyStorePassword)
          throws GeneralSecurityException, IOException {
    SSLContext sslContext = null;
    FileInputStream fileInputStream = null;

    try {
      File file = new File(keyStorePath);

      if ((file.exists() == false) || (file.isFile() == false) || (file.canRead() == false)) {
        throw new IllegalArgumentException("cannot read 'keyStorePath', path = [" + file.getAbsolutePath() + "]");
      }

      fileInputStream = new FileInputStream(keyStorePath);

      KeyStore keyStore = KeyStore.getInstance(keyStoreType);
      keyStore.load(fileInputStream, keyStorePassword.toCharArray());

      KeyManagerFactory keyManagerFactor = KeyManagerFactory.getInstance("SunX509");
      keyManagerFactor.init(keyStore, keyStorePassword.toCharArray());

      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
      trustManagerFactory.init(keyStore);

      sslContext = SSLContext.getInstance(sslContextType);
      sslContext.init(keyManagerFactor.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
    } finally {
      if (fileInputStream != null) {
        try {
          fileInputStream.close();
        } catch (IOException e) {
          // IGNORE
        }
      }
    }

    return sslContext;
  }

  /**
   *
   * @param sslContext
   * @return HttpsConfigurator
   */
  private static HttpsConfigurator createHttpsConfigurator(SSLContext sslContext) {
    return new HttpsConfigurator(sslContext) {
      @Override
      public void configure(HttpsParameters params) {
        try {
          SSLContext c = getSSLContext();
          SSLEngine engine = c.createSSLEngine();
          params.setNeedClientAuth(false);
          params.setCipherSuites(engine.getEnabledCipherSuites());
          params.setProtocols(engine.getEnabledProtocols());
          SSLParameters sslParameters = c.getSupportedSSLParameters();
          params.setSSLParameters(sslParameters);
        } catch (Exception e) {
          throw new RuntimeException("Exception creating HttpsConfigurator", e);
        }
      }
    };
  }
}
