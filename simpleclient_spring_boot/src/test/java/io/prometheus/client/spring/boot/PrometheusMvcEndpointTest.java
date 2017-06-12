package io.prometheus.client.spring.boot;

import io.prometheus.client.exporter.common.TextFormat;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by sunghyuk on 2017. 6. 11..
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DummyBootApplication.class)
@WebIntegrationTest(randomPort = true)
public class PrometheusMvcEndpointTest {

    @Value("${local.server.port}")
    int localServerPort;

    RestTemplate template = new TestRestTemplate();

    @Test
    public void testNameParamIsNull() throws Exception {
        // if name param is null, it should be initialized as an empty array.
        // mvc endpoint should return 200 not 406 (caused by NPE)
        ResponseEntity metricsResponse = template.exchange(getBaseUrl() + "/prometheus", HttpMethod.GET, getEntity(), String.class);
        // then:
        assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
        assertTrue(StringUtils.deleteWhitespace(TextFormat.CONTENT_TYPE_004).equals(metricsResponse.getHeaders().getContentType().toString()));
    }

    @Test
    public void testNameParamIsNotNull() {
        ResponseEntity metricsResponse = template.exchange(getBaseUrl() + "/prometheus?name[]=foo_bar", HttpMethod.GET, getEntity(), String.class);
        // then:
        assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
        assertTrue(StringUtils.deleteWhitespace(TextFormat.CONTENT_TYPE_004).equals(metricsResponse.getHeaders().getContentType().toString()));
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