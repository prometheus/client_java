package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.prometheus.metrics.model.snapshots.EscapingScheme;
import org.junit.jupiter.api.Test;

class PrometheusProtobufWriterTest {

  private final PrometheusProtobufWriter writer = new PrometheusProtobufWriter();

  @Test
  void accepts() {
    assertThat(writer.accepts(null)).isFalse();
  }

  @Test
  void getContentType() {
    assertThat(writer.getContentType())
        .isEqualTo(
            "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; "
                + "encoding=delimited");
  }

  @Test
  void write() {
    assertThatCode(() -> writer.write(null, null, EscapingScheme.NO_ESCAPING))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void toDebugString() {
    assertThatCode(() -> writer.toDebugString(null, EscapingScheme.NO_ESCAPING))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
