package io.prometheus.client.spring.boot;

import io.prometheus.client.exporter.common.TextFormat;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "text/plain; version=0.0.4; charset=utf-8");
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity metricsResponse = template.exchange(getBaseUrl() + "/prometheus?name[]=foo_bar", HttpMethod.GET, entity, String.class);
        // then:
        assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
        assertTrue(StringUtils.deleteWhitespace(TextFormat.CONTENT_TYPE_004).equals(metricsResponse.getHeaders().getContentType().toString()));

        // if name param is null, it should be initialized as an empty array.
        // mvc endpoint should return 200 not 406 (caused by NPE)
        metricsResponse = template.exchange(getBaseUrl() + "/prometheus", HttpMethod.GET, entity, String.class);
        // then:
        assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
        assertTrue(StringUtils.deleteWhitespace(TextFormat.CONTENT_TYPE_004).equals(metricsResponse.getHeaders().getContentType().toString()));
    }

    private String getBaseUrl() {
        return "http://localhost:" + localServerPort;
    }
}