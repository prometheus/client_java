package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.model.snapshots.Exemplar;
import org.junit.jupiter.api.Assertions;

public class TestUtil {

    public static void assertExemplarEquals(Exemplar expected, Exemplar actual) {
        // ignore timestamp
        Assertions.assertEquals(expected.getValue(), actual.getValue(), 0.00001);
        Assertions.assertEquals(expected.getLabels(), actual.getLabels());
    }
}
