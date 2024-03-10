package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LabelsTest {

    private <T extends Comparable<T>> void assertLessThan(T a, T b) {
        Assertions.assertTrue(a.compareTo(b) < 0);
    }

    private <T extends Comparable<T>> void assertGreaterThan(T a, T b) {
        Assertions.assertTrue(a.compareTo(b) > 0);
    }

    @Test
    void testCompareDifferentLabelNames() {
        Labels labels1 = Labels.of("env", "prod", "status2", "200");
        Labels labels2 = Labels.of("env", "prod", "status1", "200");
        assertGreaterThan(labels1, labels2);
        assertLessThan(labels2, labels1);
        assertNotEquals(labels1, labels2);
        assertNotEquals(labels2, labels1);
    }

    @Test
    void testCompareSameLabelNames() {
        // If all label names are the same, labels should be sorted by label value.
        Labels labels1 = Labels.of("env", "prod", "status", "200");
        Labels labels2 = Labels.of("env", "prod", "status", "500");
        assertLessThan(labels1, labels2);
        assertGreaterThan(labels2, labels1);
        assertNotEquals(labels1, labels2);
        assertNotEquals(labels2, labels1);
    }

    @Test
    void testCompareDifferentNumberOfLabels() {
        Labels labels1 = Labels.of("env", "prod", "status", "200");
        Labels labels2 = Labels.of("env", "prod", "status", "200", "x_code", "none");
        assertLessThan(labels1, labels2);
        assertGreaterThan(labels2, labels1);
        assertNotEquals(labels1, labels2);
        assertNotEquals(labels2, labels1);
    }

    @Test
    void testComparePrometheusNames() {
        Labels labels1 = Labels.of("my_a", "val");
        Labels labels2 = Labels.of("my.b", "val");
        assertLessThan(labels1, labels2); // this is true because it compares "my_a" to "my_b".
    }

    @Test
    void testEqualsHashcodeDots() {
        Labels labels1 = Labels.of("my_a", "val");
        Labels labels2 = Labels.of("my.a", "val");
        Assertions.assertEquals(labels1, labels2);
        Assertions.assertEquals(labels1.hashCode(), labels2.hashCode());
    }

    @Test
    void testCompareEquals() {
        Labels labels1 = Labels.of("env", "prod", "status", "200");
        Labels labels2 = Labels.of("env", "prod", "status", "200");
        Assertions.assertEquals(0, labels1.compareTo(labels2));
        Assertions.assertEquals(0, labels2.compareTo(labels1));
        Assertions.assertEquals(labels1, labels2);
        Assertions.assertEquals(labels2, labels1);
    }

    @Test
    void testIllegalLabelName() {
        assertThrows(IllegalArgumentException.class, () -> Labels.of("my_service/status", "200"));
    }

    @Test
    void testReservedLabelName() {
        assertThrows(IllegalArgumentException.class, () -> Labels.of("__name__", "requests_total"));
    }

    @Test
    void testDuplicateLabelName() {
        assertThrows(IllegalArgumentException.class, () -> Labels.of("name1", "value1", "name2", "value2", "name1", "value3"));
    }

    @Test
    void testMakePrometheusNames() {
        String[] names = new String[]{};
        String[] prometheusNames = Labels.makePrometheusNames(names);
        Assertions.assertSame(names, prometheusNames);

        names = new String[]{"no_dots", "at_all"};
        prometheusNames = Labels.makePrometheusNames(names);
        Assertions.assertSame(names, prometheusNames);

        names = new String[]{"dots", "here.it.is"};
        prometheusNames = Labels.makePrometheusNames(names);
        Assertions.assertNotSame(names, prometheusNames);
        Assertions.assertSame(names[0], prometheusNames[0]);
        Assertions.assertEquals("here.it.is", names[1]);
        Assertions.assertEquals("here_it_is", prometheusNames[1]);
    }

    @Test
    void testMerge() {
        Labels labels1 = Labels.of("key.1", "value 1", "key.3", "value 3");
        Labels labels2 = Labels.of("key_2", "value 2");
        Labels merged = labels2.merge(labels1);
        Assertions.assertEquals("key.1", merged.getName(0));
        Assertions.assertEquals("key_2", merged.getName(1));
        Assertions.assertEquals("key.3", merged.getName(2));
    }

    @Test
    void testMergeDuplicateName() {
        Labels labels1 = Labels.of("key_one", "v1");
        Labels labels2 = Labels.of("key.one", "v2");
        assertThrows(IllegalArgumentException.class, () -> labels2.merge(labels1));
    }

    @Test
    void testDuplicateName() {
        assertThrows(IllegalArgumentException.class, () -> Labels.of("key_one", "v1", "key.one", "v2"));
    }
}
