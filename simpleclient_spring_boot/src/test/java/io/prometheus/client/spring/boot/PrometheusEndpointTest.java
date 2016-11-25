package io.prometheus.client.spring.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import io.prometheus.client.Counter;
import io.prometheus.client.matchers.CustomMatchers;

import static org.cthul.matchers.CthulMatchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PrometheusEndpointTest {

  @Autowired
  Counter promCounter;

  @Autowired
  SecurityProperties securityProperties;

  @LocalServerPort
  int localServerPort;

  @Test
  public void testMetricsExportedThroughPrometheusEndpoint() {
    // given:

    RestTemplate template = new RestTemplateBuilder()
        .basicAuthorization("user", securityProperties.getUser().getPassword())
        .build();

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

  private String getBaseUrl() {
    return "http://localhost:" + localServerPort;
  }

  @SpringBootConfiguration
  @EnablePrometheusEndpoint
  @EnableAutoConfiguration(exclude = {
    PrometheusAutoConfiguration.class
  })
  @ComponentScan(excludeFilters = @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class))
  static class TestConfiguration {

    @Bean
    public Counter counter() {
      return Counter.build()
          .name("foo_bar")
          .help("a simple prometheus counter")
          .labelNames("label1", "label2")
          .register();
    }

  }


}
