package io.prometheus.client.spring.boot;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.hotspot.DefaultExports;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

class PrometheusConfiguration {

  @Configuration
  static class PublicMetricsCollector {
    @Autowired(required = false)
    List<PublicMetrics> publicMetrics = Collections.emptyList();

    @Bean
    public SpringBootMetricsCollector springBootMetricsCollector() {
      SpringBootMetricsCollector springBootMetricsCollector = new SpringBootMetricsCollector(publicMetrics);
      springBootMetricsCollector.register();
      return springBootMetricsCollector;
    }

  }

  @Configuration
  @ConditionalOnClass(DefaultExports.class)
  @ConditionalOnProperty(prefix = "prometheus.hotspot", name = "enabled", matchIfMissing = true)
  static class HotspotMetricsExporter {
    public HotspotMetricsExporter() {
      DefaultExports.initialize();
    }
  }

  @Bean
  public PrometheusEndpoint prometheusEndpoint() {
    return new PrometheusEndpoint(CollectorRegistry.defaultRegistry);
  }
}
