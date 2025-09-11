package io.prometheus.metrics.core.metrics;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CallbackMetricTest {

  @Test
  void makeLabels() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                CounterWithCallback.builder()
                    .name("c")
                    .callback(callback -> {})
                    .labelNames("label1", "label2")
                    .build()
                    .makeLabels("foo"))
        .withMessage(
            "CounterWithCallback was created with 2 label names, but the callback was called with 1"
                + " label values.");

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                CounterWithCallback.builder()
                    .name("c")
                    .callback(callback -> {})
                    .labelNames("label1", "label2")
                    .build()
                    .makeLabels((String[]) null))
        .withMessage(
            "CounterWithCallback was created with label names, but the callback was called without"
                + " label values.");

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                CounterWithCallback.builder()
                    .name("c")
                    .callback(callback -> {})
                    .build()
                    .makeLabels("foo"))
        .withMessage(
            "Cannot pass label values to a CounterWithCallback that was created without label"
                + " names.");
  }
}
