package io.prometheus.metrics.exporter.pushgateway;


import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.URL;

import static org.junit.rules.ExpectedException.none;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class PushGatewayTest {

    @Rule
    public final ExpectedException thrown = none();

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);
    private MockServerClient mockServerClient;

    PrometheusRegistry registry;
    Gauge gauge;

    @Before
    public void setUp() {
        registry = new PrometheusRegistry();
        gauge = Gauge.builder().name("g").help("help").build();
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidURLThrowsRuntimeException() {
        PushGateway.builder().address("::").build(); // ":" is interpreted as port number, so parsing fails
    }

    @Test
    public void testMultipleSlashesAreStrippedFromURL() throws NoSuchFieldException, IllegalAccessException {
        final PushGateway pushGateway = PushGateway.builder()
                .address("example.com:1234/context///path//")
                .job("test")
                .build();
        Assert.assertEquals(
                "http://example.com:1234/context/path/metrics/job/test",
                getUrl(pushGateway).toString()
        );
    }

    private URL getUrl(PushGateway pushGateway) throws IllegalAccessException, NoSuchFieldException {
        Field field = pushGateway.getClass().getDeclaredField("url");
        field.setAccessible(true);
        return (URL) field.get(pushGateway);
    }

    @Test
    public void testPush() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("PUT")
                        .withPath("/metrics/job/j")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .job("j")
                .build();
        pg.push();
    }

    @Test
    public void testPush200Response() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("PUT")
                        .withPath("/metrics/job/j")
        ).respond(response().withStatusCode(200));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .job("j")
                .build();
        pg.push();
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
                        + mockServerRule.getPort()
                        + "/metrics/job/j was 500");
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .job("j")
                .build();
        pg.push();
    }

    @Test
    public void testPushCollector() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("PUT")
                        .withPath("/metrics/job/j")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .job("j")
                .build();
        pg.push();
    }

    @Test
    public void testPushWithGroupingKey() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("PUT")
                        .withPath("/metrics/job/j/l/v")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .job("j")
                .groupingKey("l", "v")
                .build();
        pg.push();
    }

    @Test
    public void testPushWithMultiGroupingKey() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("PUT")
                        .withPath("/metrics/job/j/l/v/l2/v2")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .job("j")
                .groupingKey("l", "v")
                .groupingKey("l2", "v2")
                .build();
        pg.push();
    }

    @Test
    public void testPushWithEmptyLabelGroupingKey() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("PUT")
                        .withPath("/metrics/job/j/l/v/l2@base64/=")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .job("j")
                .groupingKey("l", "v")
                .groupingKey("l2", "")
                .build();
        pg.push();
    }

    @Test
    public void testPushWithGroupingKeyWithSlashes() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("PUT")
                        .withPath("/metrics/job@base64/YS9i/l/v/l2@base64/75-_Lw==")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .job("a/b")
                .groupingKey("l", "v")
                .groupingKey("l2", "\uF7FF/")
                .build();
        pg.push();
    }

    @Test
    public void testPushCollectorWithGroupingKey() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("PUT")
                        .withPath("/metrics/job/j/l/v")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .job("j")
                .groupingKey("l", "v")
                .build();
        pg.push(gauge);
    }

    @Test
    public void testPushAdd() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withPath("/metrics/job/j")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .job("j")
                .build();
        pg.pushAdd();
    }

    @Test
    public void testPushAddCollector() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withPath("/metrics/job/j")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .job("j")
                .build();
        pg.pushAdd(gauge);
    }

    @Test
    public void testPushAddWithGroupingKey() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withPath("/metrics/job/j/l/v")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .groupingKey("l", "v")
                .job("j")
                .build();
        pg.pushAdd();
    }

    @Test
    public void testPushAddCollectorWithGroupingKey() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("POST")
                        .withPath("/metrics/job/j/l/v")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .registry(registry)
                .groupingKey("l", "v")
                .job("j")
                .build();
        pg.pushAdd(gauge);
    }

    @Test
    public void testDelete() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("DELETE")
                        .withPath("/metrics/job/j")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .job("j")
                .build();
        pg.delete();
    }

    @Test
    public void testDeleteWithGroupingKey() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("DELETE")
                        .withPath("/metrics/job/j/l/v")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .job("j")
                .groupingKey("l", "v")
                .build();
        pg.delete();
    }

    @Test
    public void testInstanceIpGroupingKey() throws IOException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        Assert.assertFalse(ip.isEmpty());
        mockServerClient.when(
                request()
                        .withMethod("DELETE")
                        .withPath("/metrics/job/j/instance/" + ip + "/l/v")
        ).respond(response().withStatusCode(202));
        PushGateway pg = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .job("j")
                .groupingKey("l", "v")
                .instanceIpGroupingKey()
                .build();
        pg.delete();
    }
}
