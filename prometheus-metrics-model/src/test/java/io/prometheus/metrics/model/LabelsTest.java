package io.prometheus.metrics.model;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

public class LabelsTest {

    private <T extends Comparable<T>> void assertLessThan(T a, T b) {
        Assert.assertTrue(a.compareTo(b) < 0);
    }

    private <T extends Comparable<T>> void assertGreaterThan(T a, T b) {
        Assert.assertTrue(a.compareTo(b) > 0);
    }

    @Test
    public void testCompareDifferentLabelNames() {
        Labels labels1 = Labels.of("env", "prod", "status2", "200");
        Labels labels2 = Labels.of("env", "prod", "status1", "200");
        assertGreaterThan(labels1, labels2);
        assertLessThan(labels2, labels1);
        assertNotEquals(labels1, labels2);
        assertNotEquals(labels2, labels1);
    }

    @Test
    public void testCompareSameLabelNames() {
        // If all label names are the same, labels should be sorted by label value.
        Labels labels1 = Labels.of("env", "prod", "status", "200");
        Labels labels2 = Labels.of("env", "prod", "status", "500");
        assertLessThan(labels1, labels2);
        assertGreaterThan(labels2, labels1);
        assertNotEquals(labels1, labels2);
        assertNotEquals(labels2, labels1);
    }

    @Test
    public void testCompareDifferentNumberOfLabels() {
        Labels labels1 = Labels.of("env", "prod", "status", "200");
        Labels labels2 = Labels.of("env", "prod", "status", "200", "x_code", "none");
        assertLessThan(labels1, labels2);
        assertGreaterThan(labels2, labels1);
        assertNotEquals(labels1, labels2);
        assertNotEquals(labels2, labels1);
    }


    @Test
    public void testCompareEquals() {
        Labels labels1 = Labels.of("env", "prod", "status", "200");
        Labels labels2 = Labels.of("env", "prod", "status", "200");
        Assert.assertEquals(0, labels1.compareTo(labels2));
        Assert.assertEquals(0, labels2.compareTo(labels1));
        Assert.assertEquals(labels1, labels2);
        Assert.assertEquals(labels2, labels1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalLabelName() {
        Labels.of("http.status_code", "200");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReservedLabelName() {
        Labels.of("__name__", "requests_total");
    }

    @Test
    public void testSanitizeLabelName() {
        Assert.assertEquals("_my_label", Labels.sanitizeMetricName("...my/label"));
    }
}
