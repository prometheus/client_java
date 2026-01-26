package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

import io.prometheus.metrics.model.snapshots.Exemplar;
import io.prometheus.metrics.model.snapshots.Label;

public class TestUtil {

  public static void assertExemplarEquals(Exemplar expected, Exemplar actual) {
    // ignore timestamp
    assertThat(actual.getValue()).isCloseTo(expected.getValue(), offset(0.00001));
    assertThat((Iterable<? extends Label>) actual.getLabels()).isEqualTo(expected.getLabels());
  }
}
