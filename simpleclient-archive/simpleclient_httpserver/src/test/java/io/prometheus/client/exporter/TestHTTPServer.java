package io.prometheus.client.exporter;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.SampleNameFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

  /**
   * TrustManager[] that trusts all certificates
   */
  private final static TrustManager[] TRUST_ALL_CERTS_TRUST_MANAGERS = new TrustManager[]{
          new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
          }
  };

  /**
   * HostnameVerifier that accepts any hostname
   */
  private final static HostnameVerifier TRUST_ALL_HOSTS_HOSTNAME_VERIFIER = new HostnameVerifier() {
    @Override
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
  };

  HttpRequest.Builder createHttpRequestBuilder(HTTPServer httpServer, String urlPath) {
    return new HttpRequest.Builder().withURL("http://localhost:" + httpServer.getPort() + urlPath);
  }

  HttpRequest.Builder createHttpRequestBuilderWithSSL(HTTPServer httpServer, String urlPath) {
    return new HttpRequest.Builder().withURL("https://localhost:" + httpServer.getPort() + urlPath)
            .withTrustManagers(TRUST_ALL_CERTS_TRUST_MANAGERS)
            .withHostnameVerifier(TRUST_ALL_HOSTS_HOSTNAME_VERIFIER);
  }

  HttpRequest.Builder createHttpRequestBuilder(HttpServer httpServer, String urlPath) {
    return new HttpRequest.Builder().withURL("http://localhost:" + httpServer.getAddress().getPort() + urlPath);
  }

  @Before
  public void init() throws IOException {
    registry = new CollectorRegistry();
    Gauge.build("a", "a help").register(registry);
    Gauge.build("b", "a help").register(registry);
    Gauge.build("c", "a help").register(registry);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRefuseUsingUnbound() throws IOException {
    CollectorRegistry registry = new CollectorRegistry();
    HTTPServer httpServer = new HTTPServer(HttpServer.create(), registry, true);
    httpServer.close();
  }

  @Test
  public void testSimpleRequest() throws IOException {
    HTTPServer httpServer = new HTTPServer(new InetSocketAddress(0), registry);

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics").build().execute().getBody();
      assertThat(body).contains("a 0.0");
      assertThat(body).contains("b 0.0");
      assertThat(body).contains("c 0.0");
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testBadParams() throws IOException {
    HTTPServer httpServer = new HTTPServer(new InetSocketAddress(0), registry);

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics?x").build().execute().getBody();
      assertThat(body).contains("a 0.0");
      assertThat(body).contains("b 0.0");
      assertThat(body).contains("c 0.0");
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testSingleName() throws IOException {
    HTTPServer httpServer = new HTTPServer(new InetSocketAddress(0), registry);

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics?name[]=a").build().execute().getBody();
      assertThat(body).contains("a 0.0");
      assertThat(body).doesNotContain("b 0.0");
      assertThat(body).doesNotContain("c 0.0");
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testMultiName() throws IOException {
    HTTPServer httpServer = new HTTPServer(new InetSocketAddress(0), registry);

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics?name[]=a&name[]=b").build().execute().getBody();
      assertThat(body).contains("a 0.0");
      assertThat(body).contains("b 0.0");
      assertThat(body).doesNotContain("c 0.0");
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testSampleNameFilter() throws IOException {
    HTTPServer httpServer = new HTTPServer.Builder()
            .withRegistry(registry)
            .withSampleNameFilter(new SampleNameFilter.Builder()
                    .nameMustNotStartWith("a")
                    .build())
            .build();

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics?name[]=a&name[]=b").build().execute().getBody();
      assertThat(body).doesNotContain("a 0.0");
      assertThat(body).contains("b 0.0");
      assertThat(body).doesNotContain("c 0.0");
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testSampleNameFilterEmptyBody() throws IOException {
    HTTPServer httpServer = new HTTPServer.Builder()
            .withRegistry(registry)
            .withSampleNameFilter(new SampleNameFilter.Builder()
                    .nameMustNotStartWith("a")
                    .nameMustNotStartWith("b")
                    .build())
            .build();

    try {
      HttpResponse httpResponse = createHttpRequestBuilder(httpServer, "/metrics?name[]=a&name[]=b").build().execute();
      assertThat(httpResponse.getBody()).isEmpty();
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testDecoding() throws IOException {
    HTTPServer httpServer = new HTTPServer(new InetSocketAddress(0), registry);

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics?n%61me[]=%61").build().execute().getBody();
      assertThat(body).contains("a 0.0");
      assertThat(body).doesNotContain("b 0.0");
      assertThat(body).doesNotContain("c 0.0");
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testGzipCompression() throws IOException {
    HTTPServer httpServer = new HTTPServer(new InetSocketAddress(0), registry);

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics")
              .withHeader("Accept", "gzip")
              .withHeader("Accept", "deflate")
              .build().execute().getBody();
      assertThat(body).contains("a 0.0");
      assertThat(body).contains("b 0.0");
      assertThat(body).contains("c 0.0");
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testOpenMetrics() throws IOException {
    HTTPServer httpServer = new HTTPServer(new InetSocketAddress(0), registry);

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics")
              .withHeader("Accept", "application/openmetrics-text; version=0.0.1,text/plain;version=0.0.4;q=0.5,*/*;q=0.1")
              .build().execute().getBody();
      assertThat(body).contains("# EOF");
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testHealth() throws IOException {
    HTTPServer httpServer = new HTTPServer(new InetSocketAddress(0), registry);

    try {
      String body = createHttpRequestBuilder(httpServer, "/-/healthy").build().execute().getBody();
      assertThat(body).contains("Exporter is Healthy");
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testHealthGzipCompression() throws IOException {
    HTTPServer httpServer = new HTTPServer(new InetSocketAddress(0), registry);

    try {
      String body = createHttpRequestBuilder(httpServer, "/-/healthy")
              .withHeader("Accept", "gzip")
              .withHeader("Accept", "deflate")
              .build().execute().getBody();
      assertThat(body).contains("Exporter is Healthy");
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testBasicAuthSuccess() throws IOException {
    HTTPServer httpServer = new HTTPServer.Builder()
            .withRegistry(registry)
            .withAuthenticator(createAuthenticator("/", "user", "secret"))
            .build();

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics?name[]=a&name[]=b")
              .withAuthorization("user", "secret")
              .build().execute().getBody();
      assertThat(body).contains("a 0.0");
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testBasicAuthCredentialsMissing() throws IOException {
    HTTPServer httpServer = new HTTPServer.Builder()
            .withRegistry(registry)
            .withAuthenticator(createAuthenticator("/", "user", "secret"))
            .build();

    try {
      createHttpRequestBuilder(httpServer, "/metrics?name[]=a&name[]=b").build().execute().getBody();
      Assert.fail("expected IOException with HTTP 401");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("401"));
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testBasicAuthWrongCredentials() throws IOException {
    HTTPServer httpServer = new HTTPServer.Builder()
            .withRegistry(registry)
            .withAuthenticator(createAuthenticator("/", "user", "secret"))
            .build();

    try {
      createHttpRequestBuilder(httpServer, "/metrics?name[]=a&name[]=b")
              .withAuthorization("user", "wrong")
              .build().execute().getBody();
      Assert.fail("expected IOException with HTTP 401");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("401"));
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testHEADRequest() throws IOException {
    HTTPServer httpServer = new HTTPServer.Builder()
            .withRegistry(registry)
            .build();

    try {
      HttpResponse httpResponse = createHttpRequestBuilder(httpServer, "/metrics?name[]=a&name[]=b")
              .withMethod(HttpRequest.METHOD.HEAD)
              .build().execute();
      Assert.assertNotNull(httpResponse);
      Assert.assertNotNull(httpResponse.getHeaderAsLong("content-length"));
      Assert.assertTrue(httpResponse.getHeaderAsLong("content-length") == 74);
      assertThat(httpResponse.getBody()).isEmpty();
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testHEADRequestWithSSL() throws GeneralSecurityException, IOException {
    HTTPServer httpServer = new HTTPServer.Builder()
            .withRegistry(registry)
            .withHttpsConfigurator(HTTPS_CONFIGURATOR)
            .build();

    try {
      HttpResponse httpResponse = createHttpRequestBuilderWithSSL(httpServer, "/metrics?name[]=a&name[]=b")
              .withMethod(HttpRequest.METHOD.HEAD)
              .build().execute();
      Assert.assertNotNull(httpResponse);
      Assert.assertNotNull(httpResponse.getHeaderAsLong("content-length"));
      Assert.assertTrue(httpResponse.getHeaderAsLong("content-length") == 74);
      assertThat(httpResponse.getBody()).isEmpty();
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testSimpleRequestHttpServerWithHTTPMetricHandler() throws IOException {
    InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 0);
    HttpServer httpServer = HttpServer.create(inetSocketAddress, 0);
    httpServer.createContext("/metrics", new HTTPServer.HTTPMetricHandler(registry));
    httpServer.start();

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics").build().execute().getBody();
      assertThat(body).contains("a 0.0");
      assertThat(body).contains("b 0.0");
      assertThat(body).contains("c 0.0");
    } finally {
      httpServer.stop(0);
    }
  }

  @Test
  public void testHEADRequestWithSSLAndBasicAuthSuccess() throws GeneralSecurityException, IOException {
    HTTPServer httpServer = new HTTPServer.Builder()
            .withRegistry(registry)
            .withHttpsConfigurator(HTTPS_CONFIGURATOR)
            .withAuthenticator(createAuthenticator("/", "user", "secret"))
            .build();

    try {
      HttpResponse httpResponse = createHttpRequestBuilderWithSSL(httpServer, "/metrics?name[]=a&name[]=b")
              .withMethod(HttpRequest.METHOD.HEAD)
              .withAuthorization("user", "secret")
              .build().execute();
      Assert.assertNotNull(httpResponse);
      Assert.assertNotNull(httpResponse.getHeaderAsLong("content-length"));
      Assert.assertTrue(httpResponse.getHeaderAsLong("content-length") == 74);
      assertThat(httpResponse.getBody()).isEmpty();
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testHEADRequestWithSSLAndBasicAuthCredentialsMissing() throws GeneralSecurityException, IOException {
    HTTPServer httpServer = new HTTPServer.Builder()
            .withRegistry(registry)
            .withHttpsConfigurator(HTTPS_CONFIGURATOR)
            .withAuthenticator(createAuthenticator("/", "user", "secret"))
            .build();

    try {
      createHttpRequestBuilderWithSSL(httpServer, "/metrics?name[]=a&name[]=b")
              .withMethod(HttpRequest.METHOD.HEAD)
              .build().execute();
      Assert.fail("expected IOException with HTTP 401");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("401"));
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testHEADRequestWithSSLAndBasicAuthWrongCredentials() throws GeneralSecurityException, IOException {
    HTTPServer httpServer = new HTTPServer.Builder()
            .withRegistry(registry)
            .withHttpsConfigurator(HTTPS_CONFIGURATOR)
            .withAuthenticator(createAuthenticator("/", "user", "secret"))
            .build();

    try {
      createHttpRequestBuilderWithSSL(httpServer, "/metrics?name[]=a&name[]=b")
              .withMethod(HttpRequest.METHOD.HEAD)
              .withAuthorization("user", "wrong")
              .build().execute();
      Assert.fail("expected IOException with HTTP 401");
    } catch (IOException e) {
      Assert.assertTrue(e.getMessage().contains("401"));
    } finally {
      httpServer.close();
    }
  }

  @Test
  public void testExecutorService() throws IOException {
    ExecutorService executorService = Executors.newFixedThreadPool(20);

    HTTPServer httpServer = new HTTPServer.Builder()
            .withExecutorService(executorService)
            .withRegistry(registry)
            .build();

    Assert.assertEquals(httpServer.executorService, executorService);

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics").build().execute().getBody();
      assertThat(body).contains("a 0.0");
      assertThat(body).contains("b 0.0");
      assertThat(body).contains("c 0.0");
    } finally {
      httpServer.close();
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testExecutorServiceWithHttpServer() throws IOException {
    InetSocketAddress inetSocketAddress = new InetSocketAddress("localhost", 0);

    HttpServer externalHttpServer = HttpServer.create(inetSocketAddress, 0);
    externalHttpServer.createContext("/metrics", new HTTPServer.HTTPMetricHandler(registry));
    externalHttpServer.start();

    ExecutorService executorService = Executors.newFixedThreadPool(20);

    HTTPServer httpServer = new HTTPServer.Builder()
            .withExecutorService(executorService)
            .withHttpServer(externalHttpServer)
            .withRegistry(registry)
            .build();

    try {
      String body = createHttpRequestBuilder(httpServer, "/metrics").build().execute().getBody();
      assertThat(body).contains("a 0.0");
      assertThat(body).contains("b 0.0");
      assertThat(body).contains("c 0.0");
    } finally {
      httpServer.close();
    }
  }

  /**
   * Encodes authorization credentials
   *
   * @param username
   * @param password
   * @return String
   */
  private final static String encodeCredentials(String username, String password) {
    // Per RFC4648 table 2. We support Java 6, and java.util.Base64 was only added in Java 8,
    try {
      byte[] credentialsBytes = (username + ":" + password).getBytes("UTF-8");
      return "Basic " +  DatatypeConverter.printBase64Binary(credentialsBytes);
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
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
  private final static SSLContext createSSLContext(String sslContextType, String keyStoreType, String keyStorePath, String keyStorePassword)
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
   * Creates an Authenticator
   *
   * @param realm
   * @param validUsername
   * @param validPassword
   * @return Authenticator
   */
  private final static Authenticator createAuthenticator(String realm, final String validUsername, final String validPassword) {
    return new BasicAuthenticator(realm) {
      @Override
      public boolean checkCredentials(String username, String password) {
        return validUsername.equals(username) && validPassword.equals(password);
      }
    };
  }

  /**
   * Creates an HttpsConfiguration
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
