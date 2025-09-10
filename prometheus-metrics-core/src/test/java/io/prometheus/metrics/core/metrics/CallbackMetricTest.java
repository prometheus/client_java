package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.prometheus.metrics.expositionformats.ExpositionFormats;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.io.IOException;
import org.junit.jupiter.api.Test;

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

  @Test
  void manual() throws IOException {
    PrometheusRegistry prometheusRegistry = new PrometheusRegistry();
    Counter counter =
        Counter.builder().name("te:st").labelNames("test").register(prometheusRegistry);
    counter.labelValues("te:st").inc();
    ExpositionFormats.init()
        .getPrometheusTextFormatWriter()
        .write(System.out, prometheusRegistry.scrape());
  }
}
