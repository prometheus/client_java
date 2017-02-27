package io.prometheus.client.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A {@link Configuration} class that imports Spring's {@link org.springframework.boot.actuate.endpoint.PublicMetrics}
 * and makes them available to Prometheus.
 *
 * @author Stuart Williams (pidster)
 */
@Configuration
public class SpringPublicMetricsConfiguration {

  @Bean
  public static SpringPublicMetricsCollector publicMetricsCollector(
    @Value("${endpoints.prometheus.publicPrefix:jvm_spring}") String prefix) {
    return new SpringPublicMetricsCollector(prefix);
  }

}
