package io.prometheus.client.spring.boot;

import io.prometheus.client.Counter;
import io.prometheus.client.matchers.CustomMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.cthul.matchers.CthulMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = DummyBootApplication.class)
@EnablePrometheusEndpoint
public class PrometheusEndpointTest {

  @Autowired
  TestRestTemplate template;

  @Test
  public void testMixedMetricsExportedThroughPrometheusEndpoint() {
    // given:
    final Counter promCounter = Counter.build()
        .name("foo_bar")
        .help("a simple prometheus counter")
        .labelNames("attr1", "attr2")
        .register();

    // when:
    promCounter.labels("val1", "val2").inc(3);
    ResponseEntity<String> metricsResponse = template.getForEntity("/prometheus", String.class);

    // then:
    assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
    assertTrue(MediaType.TEXT_PLAIN.isCompatibleWith(metricsResponse.getHeaders().getContentType()));

    List<String> responseLines = Arrays.asList(metricsResponse.getBody().split("\n"));
    assertThat(responseLines, CustomMatchers.<String>exactlyNItems(1,
        matchesPattern("foo_bar\\{attr1=\"val1\",attr2=\"val2\",?\\} 3.0")));
  }
}
