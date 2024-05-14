package io.prometheus.metrics.exporter.pushgateway;

import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.MockServerRule;

import java.io.IOException;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class BasicAuthPushGatewayTest {

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);
    private MockServerClient mockServerClient;

    PrometheusRegistry registry;
    Gauge gauge;
    PushGateway pushGateway;

    @Before
    public void setUp() {
        registry = new PrometheusRegistry();
        gauge = Gauge.builder().name("g").help("help").build();
        pushGateway = PushGateway.builder()
                .address("localhost:" + mockServerRule.getPort())
                .basicAuth("testUser", "testPwd")
                .registry(registry)
                .job("j")
                .build();
    }

    @Test
    public void testAuthorizedPush() throws IOException {
        mockServerClient.when(
                request()
                        .withMethod("PUT")
                        .withHeader("Authorization", "Basic dGVzdFVzZXI6dGVzdFB3ZA==")
                        .withPath("/metrics/job/j")
        ).respond(response().withStatusCode(202));
        pushGateway.push();
    }
}
