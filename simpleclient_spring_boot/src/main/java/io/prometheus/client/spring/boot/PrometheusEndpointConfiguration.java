package io.prometheus.client.spring.boot;

import io.prometheus.client.CollectorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PrometheusEndpointConfiguration {

  private final CollectorRegistry collectorRegistry;

  @Autowired(required = false)
  public PrometheusEndpointConfiguration(CollectorRegistry collectorRegistry) {
    this.collectorRegistry = collectorRegistry == null ? CollectorRegistry.defaultRegistry : collectorRegistry;
  }

  @Bean
  public PrometheusEndpoint prometheusEndpoint() {
    return new PrometheusEndpoint(collectorRegistry);
  }

  @Bean
  @ConditionalOnBean(PrometheusEndpoint.class)
  @ConditionalOnEnabledEndpoint("prometheus")
  public PrometheusMvcEndpoint prometheusEndpointFix(PrometheusEndpoint prometheusEndpoint) {
    return new PrometheusMvcEndpoint(prometheusEndpoint);
  }
}
