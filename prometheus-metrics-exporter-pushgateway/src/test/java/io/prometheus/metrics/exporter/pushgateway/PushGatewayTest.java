package io.prometheus.metrics.exporter.pushgateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import io.prometheus.metrics.model.snapshots.EscapingScheme;
import io.prometheus.metrics.model.snapshots.PrometheusNaming;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

class PushGatewayTest {

  private MockServerClient mockServerClient;

  PrometheusRegistry registry;
  Gauge gauge;

  @BeforeEach
  public void setUp() {
    mockServerClient = ClientAndServer.startClientAndServer(0);
    registry = new PrometheusRegistry();
    gauge = Gauge.builder().name("g").help("help").build();
  }

  @AfterEach
  void tearDown() {
    mockServerClient.stop();
    PrometheusNaming.resetForTest();
  }

  @Test
  public void testInvalidURLThrowsRuntimeException() {
    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(
            () -> {
              PushGateway.builder()
                  .address("::")
                  .build(); // ":" is interpreted as port number, so parsing fails
            });
  }

  @Test
  public void testMultipleSlashesAreStrippedFromURL()
      throws NoSuchFieldException, IllegalAccessException {
    final PushGateway pushGateway =
        PushGateway.builder().address("example.com:1234/context///path//").job("test").build();
    assertThat(getUrl(pushGateway))
        .hasToString("http://example.com:1234/context/path/metrics/job/test");
  }

  private URL getUrl(PushGateway pushGateway) throws IllegalAccessException, NoSuchFieldException {
    Field field = pushGateway.getClass().getDeclaredField("url");
    field.setAccessible(true);
    return (URL) field.get(pushGateway);
  }

  @Test
  public void testPush() throws IOException {
    mockServerClient
        .when(request().withMethod("PUT").withPath("/metrics/job/j"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("j")
            .build();
    pg.push();
  }

  @Test
  public void testPush200Response() throws IOException {
    mockServerClient
        .when(request().withMethod("PUT").withPath("/metrics/job/j"))
        .respond(response().withStatusCode(200));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("j")
            .build();
    pg.push();
  }

  @Test
  public void testNon202ResponseThrows() {
    mockServerClient
        .when(request().withMethod("PUT").withPath("/metrics/job/j"))
        .respond(response().withStatusCode(500));
    assertThatExceptionOfType(IOException.class)
        .isThrownBy(
            () -> {
              PushGateway pg =
                  PushGateway.builder()
                      .address("localhost:" + mockServerClient.getPort())
                      .registry(registry)
                      .job("j")
                      .build();
              pg.push();
            })
        .withMessageContaining(
            "Response code from http://localhost:"
                + mockServerClient.getPort()
                + "/metrics/job/j was 500");
  }

  @Test
  public void testPushCollector() throws IOException {
    mockServerClient
        .when(request().withMethod("PUT").withPath("/metrics/job/j"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("j")
            .build();
    pg.push();
  }

  @Test
  public void testPushWithGroupingKey() throws IOException {
    mockServerClient
        .when(request().withMethod("PUT").withPath("/metrics/job/j/l/v"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("j")
            .groupingKey("l", "v")
            .build();
    pg.push();
  }

  @SetSystemProperty(key = "io.prometheus.naming.validationScheme", value = "utf-8")
  @Test
  public void testPushWithEscapedGroupingKey() throws IOException {
    PrometheusNaming.resetForTest();
    mockServerClient
        .when(request().withMethod("PUT").withPath("/metrics/job/j/U__l_2e_1/v1"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("j")
            .groupingKey("l.1", "v1")
            .build();
    pg.push();
  }

  @Test
  public void testPushWithMultiGroupingKey() throws IOException {
    mockServerClient
        .when(request().withMethod("PUT").withPath("/metrics/job/j/l/v/l2/v2"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("j")
            .groupingKey("l", "v")
            .groupingKey("l2", "v2")
            .build();
    pg.push();
  }

  @SetSystemProperty(key = "io.prometheus.naming.validationScheme", value = "utf-8")
  @Test
  public void testPushWithMultiEscapedGroupingKey() throws IOException {
    PrometheusNaming.resetForTest();

    mockServerClient
        .when(request().withMethod("PUT").withPath("/metrics/job/j/U__l_2e_1/v1/U__l_2e_2/v2"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("j")
            .groupingKey("l.1", "v1")
            .groupingKey("l.2", "v2")
            .build();
    pg.push();
  }

  @Test
  public void testPushWithEmptyLabelGroupingKey() throws IOException {
    mockServerClient
        .when(request().withMethod("PUT").withPath("/metrics/job/j/l/v/l2@base64/="))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("j")
            .groupingKey("l", "v")
            .groupingKey("l2", "")
            .build();
    pg.push();
  }

  @Test
  public void testPushWithGroupingKeyWithSlashes() throws IOException {
    mockServerClient
        .when(
            request().withMethod("PUT").withPath("/metrics/job@base64/YS9i/l/v/l2@base64/75-_Lw=="))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("a/b")
            .groupingKey("l", "v")
            .groupingKey("l2", "\uF7FF/")
            .build();
    pg.push();
  }

  @Test
  public void testPushCollectorWithGroupingKey() throws IOException {
    mockServerClient
        .when(request().withMethod("PUT").withPath("/metrics/job/j/l/v"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("j")
            .groupingKey("l", "v")
            .build();
    pg.push(gauge);
  }

  @SetSystemProperty(key = "io.prometheus.naming.validationScheme", value = "utf-8")
  @Test
  public void testPushCollectorWithEscapedGroupingKey() throws IOException {
    PrometheusNaming.resetForTest();
    mockServerClient
        .when(request().withMethod("PUT").withPath("/metrics/job/j/U__l_2e_1/v1"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("j")
            .groupingKey("l.1", "v1")
            .build();
    pg.push(gauge);
  }

  @Test
  public void testPushAdd() throws IOException {
    mockServerClient
        .when(request().withMethod("POST").withPath("/metrics/job/j"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .job("j")
            .build();
    pg.pushAdd();
  }

  @Test
  public void testPushAddCollector() throws IOException {
    mockServerClient
        .when(request().withMethod("POST").withPath("/metrics/job/j"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder().address("localhost:" + mockServerClient.getPort()).job("j").build();
    pg.pushAdd(gauge);
  }

  @Test
  public void testPushAddWithGroupingKey() throws IOException {
    mockServerClient
        .when(request().withMethod("POST").withPath("/metrics/job/j/l/v"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .groupingKey("l", "v")
            .job("j")
            .build();
    pg.pushAdd();
  }

  @SetSystemProperty(key = "io.prometheus.naming.validationScheme", value = "utf-8")
  @Test
  public void testPushAddWithEscapedGroupingKey() throws IOException {
    PrometheusNaming.resetForTest();
    mockServerClient
        .when(request().withMethod("POST").withPath("/metrics/job/j/U__l_2e_1/v1"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .groupingKey("l.1", "v1")
            .job("j")
            .build();
    pg.pushAdd();
  }

  @Test
  public void testPushAddCollectorWithGroupingKey() throws IOException {
    mockServerClient
        .when(request().withMethod("POST").withPath("/metrics/job/j/l/v"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .groupingKey("l", "v")
            .job("j")
            .build();
    pg.pushAdd(gauge);
  }

  @SetSystemProperty(key = "io.prometheus.naming.validationScheme", value = "utf-8")
  @Test
  public void testPushAddCollectorWithEscapedGroupingKey() throws IOException {
    PrometheusNaming.resetForTest();

    mockServerClient
        .when(request().withMethod("POST").withPath("/metrics/job/j/U__l_2e_1/v1"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .registry(registry)
            .groupingKey("l.1", "v1")
            .job("j")
            .build();
    pg.pushAdd(gauge);
  }

  @Test
  public void testDelete() throws IOException {
    mockServerClient
        .when(request().withMethod("DELETE").withPath("/metrics/job/j"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder().address("localhost:" + mockServerClient.getPort()).job("j").build();
    pg.delete();
  }

  @Test
  public void testDeleteWithGroupingKey() throws IOException {
    mockServerClient
        .when(request().withMethod("DELETE").withPath("/metrics/job/j/l/v"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .job("j")
            .groupingKey("l", "v")
            .build();
    pg.delete();
  }

  @SetSystemProperty(key = "io.prometheus.naming.validationScheme", value = "utf-8")
  @Test
  public void testDeleteWithEscapedGroupingKey() throws IOException {
    PrometheusNaming.resetForTest();

    mockServerClient
        .when(request().withMethod("DELETE").withPath("/metrics/job/j/U__l_2e_1/v1"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .job("j")
            .groupingKey("l.1", "v1")
            .build();
    pg.delete();
  }

  @Test
  public void testInstanceIpGroupingKey() throws IOException {
    String ip = InetAddress.getLocalHost().getHostAddress();
    assertThat(ip).isNotEmpty();
    mockServerClient
        .when(request().withMethod("DELETE").withPath("/metrics/job/j/instance/" + ip + "/l/v"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .job("j")
            .groupingKey("l", "v")
            .instanceIpGroupingKey()
            .build();
    pg.delete();
  }

  @SetSystemProperty(key = "io.prometheus.naming.validationScheme", value = "utf-8")
  @Test
  public void testInstanceIpEscapedGroupingKey() throws IOException {
    PrometheusNaming.resetForTest();

    String ip = InetAddress.getLocalHost().getHostAddress();
    assertThat(ip).isNotEmpty();
    mockServerClient
        .when(
            request()
                .withMethod("DELETE")
                .withPath("/metrics/job/j/instance/" + ip + "/U__l_2e_1/v1"))
        .respond(response().withStatusCode(202));
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .job("j")
            .groupingKey("l.1", "v1")
            .instanceIpGroupingKey()
            .build();
    pg.delete();
  }

  @Test
  public void testEscapingSchemeDefaultValue() throws IllegalAccessException, NoSuchFieldException {
    PushGateway pg =
        PushGateway.builder()
            .address("localhost:" + mockServerClient.getPort())
            .job("test")
            .build();

    Field escapingSchemeField = pg.getClass().getDeclaredField("escapingScheme");
    escapingSchemeField.setAccessible(true);
    EscapingScheme scheme = (EscapingScheme) escapingSchemeField.get(pg);

    assertThat(scheme).isEqualTo(EscapingScheme.NO_ESCAPING);
  }
}
