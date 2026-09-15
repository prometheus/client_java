package io.prometheus.metrics.exporter.opentelemetry.otelmodel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.exporter.internal.otlp.metrics.MetricsRequestMarshaler;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.resources.Resource;
import io.prometheus.metrics.model.snapshots.ClassicHistogramBuckets;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class PrometheusClassicHistogramTest {

  // Prometheus classic buckets: le=1 -> 2, le=5 -> 3, le=+Inf -> 4 (non-cumulative counts).
  private static final HistogramSnapshot SNAPSHOT =
      HistogramSnapshot.builder()
          .name("request_size_bytes")
          .dataPoint(
              HistogramSnapshot.HistogramDataPointSnapshot.builder()
                  .classicHistogramBuckets(
                      ClassicHistogramBuckets.of(
                          new double[] {1.0, 5.0, Double.POSITIVE_INFINITY}, new long[] {2, 3, 4}))
                  .sum(42.0)
                  .build())
          .build();

  private static HistogramPointData toOtelPoint() {
    PrometheusClassicHistogram histogram = new PrometheusClassicHistogram(SNAPSHOT, 1_000L);
    assertThat(histogram.getPoints()).hasSize(1);
    return histogram.getPoints().iterator().next();
  }

  @Test
  void infBucketIsImplicitInOtelBoundaries() {
    HistogramPointData point = toOtelPoint();
    // OTel explicit bounds are finite; the +Inf bucket is implicit, so counts has one more entry.
    assertThat(point.getBoundaries()).containsExactly(1.0, 5.0);
    assertThat(point.getBoundaries()).noneMatch(bound -> bound.isInfinite());
    assertThat(point.getCounts()).containsExactly(2L, 3L, 4L);
    assertThat(point.getCounts()).hasSize(point.getBoundaries().size() + 1);
    assertThat(point.getCount()).isEqualTo(9);
    assertThat(point.getSum()).isEqualTo(42.0);
  }

  @Test
  void pointSatisfiesOtelSdkHistogramContract() {
    HistogramPointData point = toOtelPoint();
    // The OTel SDK's own histogram point implementation validates the data model contract.
    assertThatCode(
            () ->
                ImmutableHistogramPointData.create(
                    point.getStartEpochNanos(),
                    point.getEpochNanos(),
                    point.getAttributes(),
                    point.getSum(),
                    point.hasMin(),
                    point.getMin(),
                    point.hasMax(),
                    point.getMax(),
                    point.getBoundaries(),
                    point.getCounts(),
                    point.getExemplars()))
        .doesNotThrowAnyException();
  }

  @Test
  void otlpExplicitBoundsAreFinite() throws IOException {
    MetricDataFactory factory =
        new MetricDataFactory(
            Resource.empty(), InstrumentationScopeInfo.create("test"), 1_000L, false);
    MetricData metricData = Objects.requireNonNull(factory.create(SNAPSHOT));

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    MetricsRequestMarshaler.create(Collections.singletonList(metricData)).writeBinaryTo(out);
    HistogramDataPoint dataPoint =
        ExportMetricsServiceRequest.parseFrom(out.toByteArray())
            .getResourceMetrics(0)
            .getInstrumentationLibraryMetrics(0)
            .getMetrics(0)
            .getHistogram()
            .getDataPoints(0);

    assertThat(dataPoint.getExplicitBoundsList()).containsExactly(1.0, 5.0);
    assertThat(dataPoint.getBucketCountsList()).containsExactly(2L, 3L, 4L);
    assertThat(dataPoint.getCount()).isEqualTo(9);
  }
}
