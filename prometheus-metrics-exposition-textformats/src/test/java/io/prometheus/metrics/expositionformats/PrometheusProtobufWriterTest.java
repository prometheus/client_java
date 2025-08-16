package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.prometheus.metrics.config.EscapingScheme;
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
    assertThatCode(() -> writer.write(null, null, EscapingScheme.ALLOW_UTF8))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void toDebugString() {
    assertThatCode(() -> writer.toDebugString(null, EscapingScheme.ALLOW_UTF8))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
