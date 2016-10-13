package io.prometheus.client.spring.boot;

import io.prometheus.client.CollectorRegistry;
import org.springframework.context.annotation.Bean;

class PrometheusConfiguration {

  @Bean
  public PrometheusEndpoint prometheusEndpoint() {
    return new PrometheusEndpoint(CollectorRegistry.defaultRegistry);
  }
}
