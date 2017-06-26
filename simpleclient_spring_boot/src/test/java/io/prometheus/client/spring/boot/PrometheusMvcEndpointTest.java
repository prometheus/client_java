package io.prometheus.client.spring.boot;

import io.prometheus.client.exporter.common.TextFormat;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = DummyBootApplication.class)
@TestPropertySource( properties = "management.security.enabled=false")
public class PrometheusMvcEndpointTest {

    @Value("${local.server.port}")
    int localServerPort;

    @Autowired
    TestRestTemplate template;

    @Test
    public void testNameParamIsNull() throws Exception {
        ResponseEntity metricsResponse = template.exchange(getBaseUrl() + "/prometheus", HttpMethod.GET, getEntity(), String.class);

        assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
        assertEquals(StringUtils.deleteWhitespace(TextFormat.CONTENT_TYPE_004),metricsResponse.getHeaders().getContentType().toString().toLowerCase());

    }

    @Test
    public void testAcceptPlainText() throws Exception {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "text/plain");

        ResponseEntity<String> metricsResponse = template.exchange(getBaseUrl() + "/prometheus", HttpMethod.GET, new HttpEntity(headers), String.class);

        assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
    }

    @Test
    public void testNameParamIsNotNull() {
        ResponseEntity metricsResponse = template.exchange(getBaseUrl() + "/prometheus?name[]=foo_bar", HttpMethod.GET, getEntity(), String.class);

        assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
        assertEquals(StringUtils.deleteWhitespace(TextFormat.CONTENT_TYPE_004),metricsResponse.getHeaders().getContentType().toString().toLowerCase());

    }

    public HttpEntity getEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "text/plain; version=0.0.4; charset=utf-8");
        return new HttpEntity(headers);
    }

    private String getBaseUrl() {
        return "http://localhost:" + localServerPort;
    }
}