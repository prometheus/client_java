package io.prometheus.metrics.expositionformats;

import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.model.snapshots.EscapingScheme;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;
import org.junit.jupiter.api.Test;

class ExpositionFormatWriterTest {

  private final ExpositionFormatWriter writer = OpenMetricsTextFormatWriter.create();

  @Test
  void toDebugString() {
    assertThat(writer.toDebugString(new MetricSnapshots(), EscapingScheme.NO_ESCAPING))
        .isEqualTo("# EOF\n");
  }

  @Test
  void isAvailable() {
    assertThat(writer.isAvailable()).isTrue();
  }
}
