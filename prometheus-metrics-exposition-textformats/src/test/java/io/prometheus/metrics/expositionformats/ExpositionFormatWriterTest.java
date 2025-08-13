package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.EscapingScheme;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ExpositionFormatWriterTest {

  private final ExpositionFormatWriter writer = OpenMetricsTextFormatWriter.create();

  @Test
  void write() throws IOException {
    MetricSnapshots snapshots = new MetricSnapshots();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    writer.write(out, snapshots, EscapingScheme.NO_ESCAPING);
    assertThat(out).hasToString("# EOF\n");

    out.reset();
    writer.write(out, snapshots);
    assertThat(out).hasToString("# EOF\n");
  }

  @Test
  void toDebugString() {
    assertThat(writer.toDebugString(new MetricSnapshots(), EscapingScheme.NO_ESCAPING))
        .isEqualTo("# EOF\n");
    assertThat(writer.toDebugString(new MetricSnapshots())).isEqualTo("# EOF\n");
  }

  @Test
  void isAvailable() {
    assertThat(writer.isAvailable()).isTrue();
  }
}
