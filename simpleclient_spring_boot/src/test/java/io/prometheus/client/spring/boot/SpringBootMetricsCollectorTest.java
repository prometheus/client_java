package io.prometheus.client.spring.boot;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
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
import java.util.Enumeration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
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

  private Double httpResponseCount(String path, String status) {
    return CollectorRegistry.defaultRegistry.getSampleValue(
            "http_response_count",
            new String[]{"path", "status"},
            new String[]{path, status}
    );
  }

  @Test
  public void httpResponseCount() throws Exception {
    CollectorRegistry defaultRegistry = CollectorRegistry.defaultRegistry;
    assertThat(httpResponseCount("root", "200"), nullValue());

    counterService.increment("status.200.root");
    counterService.increment("status.200.root");
    counterService.increment("status.301.star-star");
    counterService.increment("status.200.foo.bar.html");

    // Test that the single http_response_count metric with labels is correctly exported
    assertThat(httpResponseCount("root", "200"), is(2.0));
    assertThat(httpResponseCount("root", "400"), nullValue());
    assertThat(httpResponseCount("star-star", "301"), is(1.0));
    assertThat(httpResponseCount("foo.bar.html", "200"), is(1.0));

    // Test that the metrics are also exported transparently
    assertThat(defaultRegistry.getSampleValue("counter_status_200_root"), is(2.0));
    assertThat(defaultRegistry.getSampleValue("counter_status_400_root"), nullValue());
    assertThat(defaultRegistry.getSampleValue("counter_status_301_star_star"), is(1.0));
    assertThat(defaultRegistry.getSampleValue("counter_status_200_foo_bar_html"), is(1.0));
  }
}
