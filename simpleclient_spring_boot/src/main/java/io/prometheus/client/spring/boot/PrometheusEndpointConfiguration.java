package io.prometheus.client.spring.boot;

import io.prometheus.client.CollectorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PrometheusEndpointConfiguration {

  @Bean
  public PrometheusEndpoint prometheusEndpoint() {
    return new PrometheusEndpoint(CollectorRegistry.defaultRegistry);
  }

  @Bean
  public PrometheusEndpointProducer prometheusEndpointFix(PrometheusEndpoint prometheusEndpoint) {
  	return new PrometheusEndpointProducer(prometheusEndpoint);
  }
}
