package io.prometheus.metrics.model.registry;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import org.junit.jupiter.api.Test;

class CollectorTest {

  Collector collector = () -> CounterSnapshot.builder().name("counter_a").build();

  @Test
  void predicate() {
    PrometheusScrapeRequest request =
        new PrometheusScrapeRequest() {
          @Override
          public String getRequestPath() {
            return "/metrics";
          }

          @Override
          public String[] getParameterValues(String name) {
            return new String[0];
          }
        };
    assertThat(collector.collect(name -> false, request)).isNull();
    assertThat(collector.collect(name -> true, request)).isNotNull();
  }
}
