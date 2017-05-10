package io.prometheus.client.spring.boot;

import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.prometheus.client.CollectorRegistry;

@Configuration
class PrometheusEndpointConfiguration {

  @Bean
  public PrometheusEndpoint prometheusEndpoint() {
    return new PrometheusEndpoint(CollectorRegistry.defaultRegistry);
  }
  
  @Bean
  @ConditionalOnBean(PrometheusEndpoint.class)
  @ConditionalOnEnabledEndpoint("prometheus")
  public PrometheusMVCEndpoint prometheusMVCEndpoint(PrometheusEndpoint delegate) {
	return new PrometheusMVCEndpoint(delegate);
  }
}
