package io.prometheus.metrics.model;

import org.junit.Assert;
import org.junit.Test;

public class LabelsTest {

    private <T extends Comparable<T>> void assertLessThan(T a, T b) {
        Assert.assertTrue(a.compareTo(b) < 0);
    }

    private <T extends Comparable<T>> void assertEquals(T a, T b) {
        Assert.assertEquals(0, a.compareTo(b));
    }

    private <T extends Comparable<T>> void assertGreaterThan(T a, T b) {
        Assert.assertTrue(a.compareTo(b) > 0);
    }

    @Test
    public void testCompareTo() {
        assertLessThan(Labels.of("env", "prod", "status", "200"), Labels.of("env", "prod", "status", "500"));
        assertLessThan(Labels.of("env", "prod", "status", "200"), Labels.of("env", "prod", "status", "200", "x_code", "none"));
        assertGreaterThan(Labels.of("env", "prod", "status2", "200"), Labels.of("env", "prod", "status1", "200"));
        assertEquals(Labels.of("env", "prod", "status", "200"), Labels.of("env", "prod", "status", "200"));
    }
}
