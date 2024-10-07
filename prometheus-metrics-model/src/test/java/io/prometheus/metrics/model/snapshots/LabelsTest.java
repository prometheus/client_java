package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.assertj.core.api.IterableAssert;
import org.junit.jupiter.api.Test;

public class LabelsTest {

  private <T extends Comparable<T>> void assertLessThan(T a, T b) {
    assertThat(a).isLessThan(b);
  }

  private <T extends Comparable<T>> void assertGreaterThan(T a, T b) {
    assertThat(a).isGreaterThan(b);
  }

  @Test
  public void testCompareDifferentLabelNames() {
    Labels labels1 = Labels.of("env", "prod", "status2", "200");
    Labels labels2 = Labels.of("env", "prod", "status1", "200");
    assertGreaterThan(labels1, labels2);
    assertLessThan(labels2, labels1);
    assertLabels(labels2).isNotEqualTo(labels1);
    assertLabels(labels1).isNotEqualTo(labels2);
  }

  private static IterableAssert<? extends Label> assertLabels(Labels labels) {
    return assertThat((Iterable<? extends Label>) labels);
  }

  @Test
  public void testCompareSameLabelNames() {
    // If all label names are the same, labels should be sorted by label value.
    Labels labels1 = Labels.of("env", "prod", "status", "200");
    Labels labels2 = Labels.of("env", "prod", "status", "500");
    assertLessThan(labels1, labels2);
    assertGreaterThan(labels2, labels1);
    assertLabels(labels2).isNotEqualTo(labels1);
    assertLabels(labels1).isNotEqualTo(labels2);
  }

  @Test
  public void testCompareDifferentNumberOfLabels() {
    Labels labels1 = Labels.of("env", "prod", "status", "200");
    Labels labels2 = Labels.of("env", "prod", "status", "200", "x_code", "none");
    assertLessThan(labels1, labels2);
    assertGreaterThan(labels2, labels1);
    assertLabels(labels2).isNotEqualTo(labels1);
    assertLabels(labels1).isNotEqualTo(labels2);
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
    assertLabels(labels2).isEqualTo(labels1).hasSameHashCodeAs(labels1);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void testCompareEquals() {
    Labels labels1 = Labels.of("env", "prod", "status", "200");
    Labels labels2 = Labels.of("env", "prod", "status", "200");
    assertThat((Comparable) labels1).isEqualByComparingTo(labels2);
    assertThat((Comparable) labels2).isEqualByComparingTo(labels1);
    assertLabels(labels2).isEqualTo(labels1);
    assertLabels(labels1).isEqualTo(labels2);
  }

  @Test
  public void testIllegalLabelName() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Labels.of("my_service/status", "200"));
  }

  @Test
  public void testReservedLabelName() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Labels.of("__name__", "requests_total"));
  }

  @Test
  public void testDuplicateLabelName() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Labels.of("name1", "value1", "name2", "value2", "name1", "value3"));
  }

  @Test
  public void testMakePrometheusNames() {
    String[] names = new String[] {};
    String[] prometheusNames = Labels.makePrometheusNames(names);
    assertThat(prometheusNames).isSameAs(names);

    names = new String[] {"no_dots", "at_all"};
    prometheusNames = Labels.makePrometheusNames(names);
    assertThat(prometheusNames).isSameAs(names);

    names = new String[] {"dots", "here.it.is"};
    prometheusNames = Labels.makePrometheusNames(names);
    assertThat(prometheusNames).isNotSameAs(names);
    assertThat(prometheusNames[0]).isSameAs(names[0]);
    assertThat(names[1]).isEqualTo("here.it.is");
    assertThat(prometheusNames[1]).isEqualTo("here_it_is");
  }

  @Test
  public void testMerge() {
    Labels labels1 = Labels.of("key.1", "value 1", "key.3", "value 3");
    Labels labels2 = Labels.of("key_2", "value 2");
    Labels merged = labels2.merge(labels1);
    assertThat(merged.getName(0)).isEqualTo("key.1");
    assertThat(merged.getName(1)).isEqualTo("key_2");
    assertThat(merged.getName(2)).isEqualTo("key.3");
  }

  @Test
  public void testMergeDuplicateName() {
    Labels labels1 = Labels.of("key_one", "v1");
    Labels labels2 = Labels.of("key.one", "v2");
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> labels2.merge(labels1));
  }

  @Test
  public void testDuplicateName() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Labels.of("key_one", "v1", "key.one", "v2"));
  }
}
