package io.prometheus.client.spring.resttemplate;

import static org.junit.Assert.assertEquals;

import io.prometheus.client.CollectorRegistry;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MetricsRestTemplateRestTemplateConfig.class})
public class MetricsClientHttpRequestInterceptorTest {

  final static CollectorRegistry REGISTRY = new CollectorRegistry();

  private final String[] labelNames = {"method", "host", "path"};

  @Autowired
  private RestTemplate restTemplate;

  @Test
  public void testMetricsGathered() {
    final String[] labelValues = {"GET", "unknown", "/test"};
    MockRestServiceServer mockServer = MockRestServiceServer.createServer(restTemplate);
    mockServer.expect(MockRestRequestMatchers.requestTo("/test"))
        .andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
        .andRespond(MockRestResponseCreators.withSuccess("OK", MediaType.APPLICATION_JSON));
    restTemplate.getForObject("/test", String.class);

    assertEquals(new Double(1),
        REGISTRY.getSampleValue("spring_rest_template_request_duration_seconds_count",
            labelNames,
            labelValues));

    mockServer.verify();
  }
}

@Configuration
class MetricsRestTemplateRestTemplateConfig {

  @Bean
  public MetricsClientHttpRequestInterceptor metricsClientHttpRequestInterceptor() {
    return new MetricsClientHttpRequestInterceptor(
        MetricsClientHttpRequestInterceptorTest.REGISTRY);
  }

  @Bean
  RestTemplate restTemplate(MetricsClientHttpRequestInterceptor interceptor) {
    RestTemplate restTemplate = new RestTemplate();
    restTemplate
        .setInterceptors(Collections.<ClientHttpRequestInterceptor>singletonList(interceptor));
    return restTemplate;
  }
}
