package io.prometheus.metrics.core.datapoints;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TimerApiTest {

  private double time = 0;

  private io.prometheus.metrics.core.datapoints.Timer timer =
      new io.prometheus.metrics.core.datapoints.Timer(t -> time = t) {};
  private TimerApi api = () -> timer;

  @Test
  void runnable() {
    api.time(() -> {});
    assertThat(time).isPositive();
  }

  @Test
  void supplier() {
    assertThat(api.time(() -> "foo")).isEqualTo("foo");
    assertThat(time).isPositive();
  }

  @Test
  void callable() throws Exception {
    assertThat(api.timeChecked(() -> "foo")).isEqualTo("foo");
    assertThat(time).isPositive();
  }
}
