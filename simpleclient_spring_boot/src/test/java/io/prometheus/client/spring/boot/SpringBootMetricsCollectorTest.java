package io.prometheus.client.spring.boot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.GaugeService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class SpringBootMetricsCollectorTest {

  @Autowired
  private CounterService counterService;

  @Autowired
  private GaugeService gaugeService;

  @Test
  public void collect() throws Exception {
    counterService.increment("foo");
    gaugeService.submit("bar", 3.14);

    CollectorRegistry defaultRegistry = CollectorRegistry.defaultRegistry;
    assertThat(defaultRegistry.getSampleValue("counter_foo"), is(1.0));
    assertThat(defaultRegistry.getSampleValue("gauge_bar"), is(3.14));
  }

  @SpringBootApplication
  @EnableSpringBootMetricsCollector
  static class TestConfiguration {

  }

}
