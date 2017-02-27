/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prometheus.client.spring.boot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.PublicMetricsAutoConfiguration;
import org.springframework.boot.actuate.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.spring.EnablePrometheusCollectorRegistration;
import io.prometheus.client.spring.EnablePrometheusMetrics;
import io.prometheus.client.spring.HotspotMetricCollectorsConfiguration;
import io.prometheus.client.spring.SpringPublicMetricsConfiguration;

/**
 * Auto-configuration class for creating a {@link io.prometheus.client.spring.boot.PrometheusEndpoint}.
 *
 * <p>
 * Configuration:
 * <br>You can customize this endpoint at runtime using the following spring properties:
 * <ul>
 * <li>{@code endpoints.prometheus.path} (default: "/prometheus")</li>
 * <li>{@code endpoints.prometheus.enabled} (default: {@code true})</li>
 * <li>{@code endpoints.prometheus.sensitive} (default: {@code true})</li>
 * </ul>
 *
 * @author Stuart Williams (pidster)
 */
@ManagementContextConfiguration
@ConditionalOnWebApplication
@AutoConfigureBefore(EndpointAutoConfiguration.class)
@AutoConfigureAfter(PublicMetricsAutoConfiguration.class)
@EnableConfigurationProperties({
    PrometheusProperties.class
})
public class PrometheusAutoConfiguration {

  @Bean
  @ConditionalOnEnabledEndpoint("prometheus")
  @ConditionalOnMissingBean(PrometheusEndpoint.class)
  public PrometheusEndpoint prometheusEndpoint(
      CollectorRegistry collectorRegistry,
      @Value("${endpoints.prometheus.sensitive:false}") boolean sensitive,
      @Value("${endpoints.prometheus.enabled:true}") boolean enabled) {
    return new PrometheusEndpoint(collectorRegistry, sensitive, enabled);
  }

  @Bean
  @ConditionalOnEnabledEndpoint("prometheus")
  @ConditionalOnMissingBean(PrometheusMvcEndpoint.class)
  public PrometheusMvcEndpoint prometheusMvcEndpoint(PrometheusEndpoint prometheusEndpoint) {
    return new PrometheusMvcEndpoint(prometheusEndpoint);
  }

  @Configuration
  @ConditionalOnMissingBean(CollectorRegistry.class)
  @EnablePrometheusCollectorRegistration
  static class PrometheusCollectorRegistrationConfiguration {

  }

  @Configuration
  @ConditionalOnMissingBean({
      HotspotMetricCollectorsConfiguration.class,
      SpringPublicMetricsConfiguration.class,
  })
  @EnablePrometheusMetrics
  static class PrometheusMetricsConfiguration {

  }

}
