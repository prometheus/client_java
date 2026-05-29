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
    assertThat(writer.accepts("text/plain")).isFalse();
    assertThat(writer.accepts("application/vnd.google.protobuf")).isFalse();
    assertThat(writer.accepts("proto=io.prometheus.client.MetricFamily")).isFalse();
    assertThat(
            writer.accepts(
                "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily"))
        .isTrue();
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

  @Test
  void isAvailable() {
    assertThat(writer.isAvailable()).isFalse();
  }
}
