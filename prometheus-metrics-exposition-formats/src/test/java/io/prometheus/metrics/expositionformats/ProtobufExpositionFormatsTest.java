package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.generated.Metrics;
import io.prometheus.metrics.expositionformats.internal.PrometheusProtobufWriterImpl;
import io.prometheus.metrics.expositionformats.internal.ProtobufUtil;
import io.prometheus.metrics.model.snapshots.HistogramSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import io.prometheus.metrics.model.snapshots.NativeHistogramBuckets;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonCanonicalType")
class ProtobufExpositionFormatsTest extends ExpositionFormatsTest {

  @Override
  protected void assertPrometheusProtobuf(String expected, MetricSnapshot snapshot) {
    PrometheusProtobufWriterImpl writer = new PrometheusProtobufWriterImpl();
    Metrics.MetricFamily protobufData =
        writer.convert(snapshot, EscapingScheme.UNDERSCORE_ESCAPING);
    String actual = ProtobufUtil.shortDebugString(protobufData);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  void testNativeHistogramDebugString() {
    HistogramSnapshot histogram =
        HistogramSnapshot.builder()
            .name("request_latency_seconds")
            .help("request latency")
            .dataPoint(
                HistogramSnapshot.HistogramDataPointSnapshot.builder()
                    .sum(0.123)
                    .nativeSchema(5)
                    .nativeZeroThreshold(2.938735877055719E-39)
                    .nativeZeroCount(0)
                    .nativeBucketsForPositiveValues(
                        NativeHistogramBuckets.builder().bucket(-96, 1).build())
                    .build())
            .build();

    PrometheusProtobufWriterImpl writer = new PrometheusProtobufWriterImpl();

    assertThat(
            writer.toDebugString(MetricSnapshots.of(histogram), EscapingScheme.UNDERSCORE_ESCAPING))
        .isEqualTo(
            "name: \"request_latency_seconds\"\n"
                + "help: \"request latency\"\n"
                + "type: HISTOGRAM\n"
                + "metric {\n"
                + "  histogram {\n"
                + "    sample_count: 1\n"
                + "    sample_sum: 0.123\n"
                + "    schema: 5\n"
                + "    zero_threshold: 2.938735877055719E-39\n"
                + "    zero_count: 0\n"
                + "    positive_span {\n"
                + "      offset: -96\n"
                + "      length: 1\n"
                + "    }\n"
                + "    positive_delta: 1\n"
                + "  }\n"
                + "}\n");
  }
}
