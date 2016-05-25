package io.prometheus.client.spring.boot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(SpringBootMetricsCollectorTest.class)
@EnableAutoConfiguration
public class SpringBootMetricsCollectorTest {
  @Autowired
  private SpringBootMetricsCollector springBootMetricsCollector;

  @Autowired
  private CounterService counterService;

  @Autowired
  private GaugeService gaugeService;

  @Bean
  public SpringBootMetricsCollector springBootMetricsCollector(Collection<PublicMetrics> publicMetrics) {
    SpringBootMetricsCollector springBootMetricsCollector = new SpringBootMetricsCollector(publicMetrics);
    springBootMetricsCollector.register();
    return springBootMetricsCollector;
  }

  @Test
  public void collect() throws Exception {
    counterService.increment("foo");
    gaugeService.submit("bar", 3.14);

    CollectorRegistry defaultRegistry = CollectorRegistry.defaultRegistry;
    assertThat(defaultRegistry.getSampleValue("counter_foo"), is(1.0));
    assertThat(defaultRegistry.getSampleValue("gauge_bar"), is(3.14));
  }
}
