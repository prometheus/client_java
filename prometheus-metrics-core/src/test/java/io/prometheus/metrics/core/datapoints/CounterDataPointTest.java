package io.prometheus.metrics.core.datapoints;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.model.snapshots.Labels;
import org.junit.jupiter.api.Test;

class CounterDataPointTest {

  private double value = 0;

  @Test
  void inc() {
    CounterDataPoint counterDataPoint =
        new CounterDataPoint() {
          @Override
          public void inc(double value) {
            CounterDataPointTest.this.value += value;
          }

          @Override
          public void incWithExemplar(double amount, Labels labels) {}

          @Override
          public long getLongValue() {
            return 0;
          }

          @Override
          public double get() {
            return 0;
          }
        };
    counterDataPoint.inc(1);
    assertThat(value).isOne();
    counterDataPoint.incWithExemplar(1, null);
    assertThat(value).isEqualTo(2);
  }
}
