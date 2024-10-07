package io.prometheus.metrics.core.metrics;

import static org.junit.Assert.assertEquals;

import io.prometheus.metrics.model.snapshots.Exemplar;

public class TestUtil {

  public static void assertExemplarEquals(Exemplar expected, Exemplar actual) {
    // ignore timestamp
    assertEquals(expected.getValue(), actual.getValue(), 0.00001);
    assertEquals(expected.getLabels(), actual.getLabels());
  }
}
