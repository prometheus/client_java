package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.expositionformats.generated.com_google_protobuf_4_33_0.Metrics;
import io.prometheus.metrics.expositionformats.internal.PrometheusProtobufWriterImpl;
import io.prometheus.metrics.expositionformats.internal.ProtobufUtil;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;

class ProtobufExpositionFormatsTest extends ExpositionFormatsTest {

  @Override
  protected void assertPrometheusProtobuf(String expected, MetricSnapshot snapshot) {
    PrometheusProtobufWriterImpl writer = new PrometheusProtobufWriterImpl();
    Metrics.MetricFamily protobufData =
        writer.convert(snapshot, EscapingScheme.UNDERSCORE_ESCAPING);
    String actual = ProtobufUtil.shortDebugString(protobufData);
    assertThat(actual).isEqualTo(expected);
  }
}
