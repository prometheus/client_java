package io.prometheus.client.spring.boot;

import java.util.Collection;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PrometheusMetricsConfiguration {

  @Bean
  public SpringBootMetricsCollector springBootMetricsCollector(Collection<PublicMetrics> publicMetrics) {
    SpringBootMetricsCollector springBootMetricsCollector = new SpringBootMetricsCollector(publicMetrics);
    springBootMetricsCollector.register();
    return springBootMetricsCollector;
  }
}
