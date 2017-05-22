package io.prometheus.client.spring.boot;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.matchers.CustomMatchers;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.cthul.matchers.CthulMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(DummyBootApplication.class)
@WebIntegrationTest(randomPort = true)
@EnablePrometheusEndpoint
public class PrometheusEndpointTest {

  @Value("${local.server.port}")
  int localServerPort;

  RestTemplate template = new TestRestTemplate();

  @Test
  public void testMetricsExportedThroughPrometheusEndpoint() {
    // given:
    final Counter promCounter = Counter.build("foo_bar", "test counter")
            .labelNames("label1", "label2")
            .register();
    final Counter filteredCounter = Counter.build("filtered_foo_bar", "test counter")
            .labelNames("label1", "label2")
            .register();

    // when:
    promCounter.labels("val1", "val2").inc(3);
    filteredCounter.labels("val1", "val2").inc(6);
    ResponseEntity<String> metricsResponse = template.getForEntity(getBaseUrl() + "/prometheus?name[]=foo_bar", String.class);

    // then:
    assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
    assertTrue(StringUtils.deleteWhitespace(TextFormat.CONTENT_TYPE_004).equals(metricsResponse.getHeaders().getContentType().toString()));

    List<String> responseLines = Arrays.asList(metricsResponse.getBody().split("\n"));
    assertThat(responseLines, CustomMatchers.<String>exactlyNItems(1,
            matchesPattern("foo_bar\\{label1=\"val1\",label2=\"val2\",?\\} 3.0")));
  }

  private String getBaseUrl() {
    return "http://localhost:" + localServerPort;
  }
}
