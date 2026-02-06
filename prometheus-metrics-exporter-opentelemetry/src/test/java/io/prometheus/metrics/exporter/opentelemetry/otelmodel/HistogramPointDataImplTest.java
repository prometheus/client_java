package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import java.util.List;
import org.junit.jupiter.api.Test;

class HistogramPointDataImplTest {

  @Test
  void hasMinReturnsTrueWhenMinIsNotNaN() {
    HistogramPointDataImpl histogramPoint =
        new HistogramPointDataImpl(
            10.0,
            5,
            1.0,
            5.0,
            List.of(1.0, 2.0),
            List.of(2L, 3L),
            0L,
            1L,
            Attributes.empty(),
            List.of());
    assertThat(histogramPoint.hasMin()).isTrue();
    assertThat(histogramPoint.getMin()).isEqualTo(1.0);
  }

  @Test
  void hasMinReturnsFalseWhenMinIsNaN() {
    HistogramPointDataImpl histogramPoint =
        new HistogramPointDataImpl(
            10.0,
            5,
            Double.NaN,
            5.0,
            List.of(1.0, 2.0),
            List.of(2L, 3L),
            0L,
            1L,
            Attributes.empty(),
            List.of());
    assertThat(histogramPoint.hasMin()).isFalse();
  }

  @Test
  void hasMaxReturnsTrueWhenMaxIsNotNaN() {
    HistogramPointDataImpl histogramPoint =
        new HistogramPointDataImpl(
            10.0,
            5,
            1.0,
            5.0,
            List.of(1.0, 2.0),
            List.of(2L, 3L),
            0L,
            1L,
            Attributes.empty(),
            List.of());
    assertThat(histogramPoint.hasMax()).isTrue();
    assertThat(histogramPoint.getMax()).isEqualTo(5.0);
  }

  @Test
  void hasMaxReturnsFalseWhenMaxIsNaN() {
    HistogramPointDataImpl histogramPoint =
        new HistogramPointDataImpl(
            10.0,
            5,
            1.0,
            Double.NaN,
            List.of(1.0, 2.0),
            List.of(2L, 3L),
            0L,
            1L,
            Attributes.empty(),
            List.of());
    assertThat(histogramPoint.hasMax()).isFalse();
  }
}
