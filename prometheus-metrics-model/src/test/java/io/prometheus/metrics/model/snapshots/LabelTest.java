package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LabelTest {

  private static final Label LABEL = new Label("name", "value");
  private static final Label SAME = new Label("name", "value");
  private static final Label LABEL2 = new Label("name", "value2");

  @Test
  void compareTo() {
    assertThat(LABEL).isEqualByComparingTo(SAME).isLessThan(LABEL2);
  }

  @Test
  void testToString() {
    assertThat(LABEL).hasToString("Label{name='name', value='value'}");
  }

  @Test
  void testEquals() {
    assertThat(LABEL).isEqualTo(SAME).isNotEqualTo(LABEL2);
  }

  @Test
  void testHashCode() {
    assertThat(LABEL).hasSameHashCodeAs(SAME).doesNotHaveSameHashCodeAs(LABEL2);
  }
}
