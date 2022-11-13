package io.prometheus.metrics.core;

import io.prometheus.metrics.model.Exemplar;
import org.junit.Assert;

public class TestUtil {

    public static void assertExemplarEquals(Exemplar expected, Exemplar actual) {
        // ignore timestamp
        Assert.assertEquals(expected.getValue(), actual.getValue(), 0.00001);
        Assert.assertEquals(expected.getLabels(), actual.getLabels());
    }
}
