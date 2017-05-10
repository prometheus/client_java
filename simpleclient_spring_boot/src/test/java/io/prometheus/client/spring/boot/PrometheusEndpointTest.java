package io.prometheus.client.spring.boot;

import io.prometheus.client.Counter;
import io.prometheus.client.matchers.CustomMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    final Counter promCounter = Counter.build()
        .name("foo_bar")
        .help("a simple prometheus counter")
        .labelNames("label1", "label2")
        .register();

    // when:
    promCounter.labels("val1", "val2").inc(3);
    ResponseEntity<String> metricsResponse = template.getForEntity(getBaseUrl() + "/prometheus", String.class);

    // then:
    assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
    assertTrue(MediaType.TEXT_PLAIN.isCompatibleWith(metricsResponse.getHeaders().getContentType()));

    List<String> responseLines = Arrays.asList(metricsResponse.getBody().split("\n"));
    assertThat(responseLines, CustomMatchers.<String>exactlyNItems(1,
        matchesPattern("foo_bar\\{label1=\"val1\",label2=\"val2\",?\\} 3.0")));
  }
  
  @Test
  public void testMetricsExportedThroughPrometheusMVCEndpoint() {
    // given:
    final Counter promCounter = Counter.build()
        .name("foo_bar2")
        .help("a simple prometheus counter")
        .labelNames("label1", "label2")
        .register();

    // when:
    promCounter.labels("val1", "val2").inc(3);
   
    final String xx = "application/vnd.google.protobuf;proto=io.prometheus.client.MetricFamily;encoding=delimited;q=0.7,text/plain;version=0.0.4;q=0.3";
    
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Arrays.asList( MediaType.valueOf("text/plain;version=0.0.4")));
    HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        
    ResponseEntity<String> metricsResponse = template.exchange(getBaseUrl() + "/prometheus", HttpMethod.GET, entity, String.class);

    // then:
    assertEquals(HttpStatus.OK, metricsResponse.getStatusCode());
    assertTrue(MediaType.TEXT_PLAIN.isCompatibleWith(metricsResponse.getHeaders().getContentType()));

    List<String> responseLines = Arrays.asList(metricsResponse.getBody().split("\n"));
    assertThat(responseLines, CustomMatchers.<String>exactlyNItems(1,
        matchesPattern("foo_bar2\\{label1=\"val1\",label2=\"val2\",?\\} 3.0")));
  }

  private String getBaseUrl() {
    return "http://localhost:" + localServerPort;
  }
}
