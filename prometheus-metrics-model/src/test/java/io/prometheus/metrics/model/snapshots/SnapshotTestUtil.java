package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;

class SnapshotTestUtil {

  public static void assertMetadata(
      MetricSnapshot snapshot, String name, String help, String unit) {
    assertThat(snapshot.getMetadata().getName()).isEqualTo(name);
    assertThat(snapshot.getMetadata().getHelp()).isEqualTo(help);
    if (unit != null) {
      assertThat(snapshot.getMetadata().getUnit().toString()).isEqualTo(unit);
    } else {
      assertThat(snapshot.getMetadata().getUnit()).isNull();
    }
  }
}
