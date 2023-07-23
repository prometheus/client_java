package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.Labels;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeLabelName;
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
    public void testComparePrometheusNames() {
        Labels labels1 = Labels.of("my_a", "val");
        Labels labels2 = Labels.of("my.b", "val");
        assertLessThan(labels1, labels2); // this is true because it compares "my_a" to "my_b".
    }

    @Test
    public void testEqualsHashcodeDots() {
        Labels labels1 = Labels.of("my_a", "val");
        Labels labels2 = Labels.of("my.a", "val");
        Assert.assertEquals(labels1, labels2);
        Assert.assertEquals(labels1.hashCode(), labels2.hashCode());
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
        Labels.of("my_service/status", "200");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReservedLabelName() {
        Labels.of("__name__", "requests_total");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateLabelName() {
        Labels.of("name1", "value1", "name2", "value2", "name1", "value3");
    }

    @Test
    public void testMakePrometheusNames() {
        String[] names = new String[]{};
        String[] prometheusNames = Labels.makePrometheusNames(names);
        Assert.assertSame(names, prometheusNames);

        names = new String[]{"no_dots", "at_all"};
        prometheusNames = Labels.makePrometheusNames(names);
        Assert.assertSame(names, prometheusNames);

        names = new String[]{"dots", "here.it.is"};
        prometheusNames = Labels.makePrometheusNames(names);
        Assert.assertNotSame(names, prometheusNames);
        Assert.assertSame(names[0], prometheusNames[0]);
        Assert.assertEquals("here.it.is", names[1]);
        Assert.assertEquals("here_it_is", prometheusNames[1]);
    }

    @Test
    public void testMerge() {
        Labels labels1 = Labels.of("key.1", "value 1", "key.3", "value 3");
        Labels labels2 = Labels.of("key_2", "value 2");
        Labels merged = labels2.merge(labels1);
        Assert.assertEquals("key.1", merged.getName(0));
        Assert.assertEquals("key_2", merged.getName(1));
        Assert.assertEquals("key.3", merged.getName(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMergeDuplicateName() {
        Labels labels1 = Labels.of("key_one", "v1");
        Labels labels2 = Labels.of("key.one", "v2");
        labels2.merge(labels1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateName() {
        Labels.of("key_one", "v1", "key.one", "v2");
    }
}
