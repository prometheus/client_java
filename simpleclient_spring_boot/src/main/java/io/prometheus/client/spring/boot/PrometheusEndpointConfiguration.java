package io.prometheus.client.spring.boot;

import io.prometheus.client.CollectorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @deprecated in favour of using Spring's AutoConfiguration support
 */
@Deprecated
@Configuration
class PrometheusEndpointConfiguration {

  @Bean
  public PrometheusEndpoint prometheusEndpoint() {
    return new PrometheusEndpoint(CollectorRegistry.defaultRegistry, false, true);
  }
}
