package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExponentialHistogramPointDataImplTest {

  private static final ExponentialHistogramBuckets EMPTY_BUCKETS =
      ExponentialHistogramBuckets.create(0, 0, List.of());

  @Test
  void hasMinReturnsTrueWhenMinIsNotNaN() {
    ExponentialHistogramPointDataImpl histogramPoint =
        new ExponentialHistogramPointDataImpl(
            0,
            10.0,
            5,
            0,
            1.0,
            5.0,
            EMPTY_BUCKETS,
            EMPTY_BUCKETS,
            0L,
            1L,
            Attributes.empty(),
            List.of());
    assertThat(histogramPoint.hasMin()).isTrue();
    assertThat(histogramPoint.getMin()).isEqualTo(1.0);
  }

  @Test
  void hasMinReturnsFalseWhenMinIsNaN() {
    ExponentialHistogramPointDataImpl histogramPoint =
        new ExponentialHistogramPointDataImpl(
            0,
            10.0,
            5,
            0,
            Double.NaN,
            5.0,
            EMPTY_BUCKETS,
            EMPTY_BUCKETS,
            0L,
            1L,
            Attributes.empty(),
            List.of());
    assertThat(histogramPoint.hasMin()).isFalse();
  }

  @Test
  void hasMaxReturnsTrueWhenMaxIsNotNaN() {
    ExponentialHistogramPointDataImpl histogramPoint =
        new ExponentialHistogramPointDataImpl(
            0,
            10.0,
            5,
            0,
            1.0,
            5.0,
            EMPTY_BUCKETS,
            EMPTY_BUCKETS,
            0L,
            1L,
            Attributes.empty(),
            List.of());
    assertThat(histogramPoint.hasMax()).isTrue();
    assertThat(histogramPoint.getMax()).isEqualTo(5.0);
  }

  @Test
  void hasMaxReturnsFalseWhenMaxIsNaN() {
    ExponentialHistogramPointDataImpl histogramPoint =
        new ExponentialHistogramPointDataImpl(
            0,
            10.0,
            5,
            0,
            1.0,
            Double.NaN,
            EMPTY_BUCKETS,
            EMPTY_BUCKETS,
            0L,
            1L,
            Attributes.empty(),
            List.of());
    assertThat(histogramPoint.hasMax()).isFalse();
  }
}
