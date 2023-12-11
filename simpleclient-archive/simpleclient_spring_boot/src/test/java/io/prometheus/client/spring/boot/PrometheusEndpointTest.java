package io.prometheus.client.spring.boot;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.matchers.CustomMatchers;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.cthul.matchers.CthulMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@EnablePrometheusEndpoint
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = DummyBootApplication.class)
@TestPropertySource(properties = "management.security.enabled=false")
public class PrometheusEndpointTest {

  @Value("${local.server.port}")
  int localServerPort;

  @Autowired
  TestRestTemplate template;

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

    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "text/plain");

    ResponseEntity<String> metricsResponse = template.exchange(getBaseUrl() + "/prometheus?name[]=foo_bar_total", HttpMethod.GET, new HttpEntity(headers), String.class);

    // then:
    assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
    assertEquals(StringUtils.deleteWhitespace(TextFormat.CONTENT_TYPE_004), metricsResponse.getHeaders().getContentType().toString().toLowerCase());

    List<String> responseLines = Arrays.asList(metricsResponse.getBody().split("\n"));
    assertThat(responseLines, CustomMatchers.<String>exactlyNItems(1,
            matchesPattern("foo_bar_total\\{label1=\"val1\",label2=\"val2\",?\\} 3.0")));
  }

  private String getBaseUrl() {
    return "http://localhost:" + localServerPort;
  }
}
