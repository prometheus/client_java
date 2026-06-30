package io.prometheus.metrics.expositionformats.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.TextFormat;
import com.google.protobuf.Timestamp;
import io.prometheus.metrics.expositionformats.generated.Metrics;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonCanonicalType")
class PrometheusProtobufDebugFormatTest {

  @Test
  void testHistogramFieldsAreEmittedInProtobufFieldNumberOrder() throws Exception {
    Metrics.MetricFamily metricFamily =
        Metrics.MetricFamily.newBuilder()
            .setName("request_latency_seconds")
            .setHelp("request latency")
            .setType(Metrics.MetricType.HISTOGRAM)
            .addMetric(
                Metrics.Metric.newBuilder()
                    .setTimestampMs(123)
                    .setHistogram(
                        Metrics.Histogram.newBuilder()
                            .setSampleCountFloat(3.0)
                            .setSampleSum(4.0)
                            .addBucket(
                                Metrics.Bucket.newBuilder()
                                    .setCumulativeCount(1)
                                    .setUpperBound(2.0)
                                    .setCumulativeCountFloat(1.0))
                            .setSchema(5)
                            .setZeroThreshold(2.938735877055719E-39)
                            .setZeroCount(0)
                            .addPositiveSpan(
                                Metrics.BucketSpan.newBuilder().setOffset(-96).setLength(1))
                            .addPositiveDelta(1)
                            .setCreatedTimestamp(
                                Timestamp.newBuilder().setSeconds(1000).setNanos(123000000))))
            .build();

    assertThat(PrometheusProtobufDebugFormat.toDebugString(metricFamily))
        .isEqualTo(TextFormat.printer().printToString(metricFamily));
  }

  @Test
  void testAllMetricTypesMatchProtobufTextFormat() throws Exception {
    Metrics.MetricFamily metricFamily =
        Metrics.MetricFamily.newBuilder()
            .setName("test_metric")
            .setHelp("test metric")
            .setType(Metrics.MetricType.UNTYPED)
            .setUnit("seconds")
            .addMetric(
                Metrics.Metric.newBuilder()
                    .addLabel(label("path", "/Björn"))
                    .setGauge(Metrics.Gauge.newBuilder().setValue(2.0)))
            .addMetric(
                Metrics.Metric.newBuilder()
                    .setCounter(
                        Metrics.Counter.newBuilder()
                            .setValue(3.0)
                            .setExemplar(exemplar())
                            .setCreatedTimestamp(timestamp())))
            .addMetric(
                Metrics.Metric.newBuilder()
                    .setSummary(
                        Metrics.Summary.newBuilder()
                            .setSampleCount(7)
                            .setSampleSum(8.0)
                            .addQuantile(
                                Metrics.Quantile.newBuilder().setQuantile(0.99).setValue(42.0))
                            .setCreatedTimestamp(timestamp())))
            .addMetric(
                Metrics.Metric.newBuilder().setUntyped(Metrics.Untyped.newBuilder().setValue(1.5)))
            .addMetric(
                Metrics.Metric.newBuilder()
                    .setHistogram(
                        Metrics.Histogram.newBuilder()
                            .setSampleCount(3)
                            .setSampleSum(4.0)
                            .addBucket(
                                Metrics.Bucket.newBuilder()
                                    .setCumulativeCount(1)
                                    .setUpperBound(2.0)
                                    .setExemplar(exemplar())
                                    .setCumulativeCountFloat(1.0))
                            .setSampleCountFloat(3.0)
                            .setSchema(5)
                            .setZeroThreshold(2.938735877055719E-39)
                            .setZeroCount(0)
                            .setZeroCountFloat(0.0)
                            .addNegativeSpan(
                                Metrics.BucketSpan.newBuilder().setOffset(-96).setLength(1))
                            .addNegativeDelta(1)
                            .addNegativeCount(1.0)
                            .addPositiveSpan(
                                Metrics.BucketSpan.newBuilder().setOffset(96).setLength(1))
                            .addPositiveDelta(1)
                            .addPositiveCount(1.0)
                            .setCreatedTimestamp(timestamp())
                            .addExemplars(exemplar())))
            .build();

    assertThat(PrometheusProtobufDebugFormat.toDebugString(metricFamily))
        .isEqualTo(TextFormat.printer().printToString(metricFamily));
  }

  private static Metrics.LabelPair label(String name, String value) {
    return Metrics.LabelPair.newBuilder().setName(name).setValue(value).build();
  }

  private static Metrics.Exemplar exemplar() {
    return Metrics.Exemplar.newBuilder()
        .addLabel(label("trace_id", "abc"))
        .setValue(1.0)
        .setTimestamp(timestamp())
        .build();
  }

  private static Timestamp timestamp() {
    return Timestamp.newBuilder().setSeconds(1000).setNanos(123000000).build();
  }
}
