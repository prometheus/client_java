package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class StateSetTest {

  enum MyFeatureFlag {
    EXPERIMENTAL_FEATURE_1 {
      @Override
      public String toString() {
        return "feature1";
      }
    },

    EXPERIMENTAL_FEATURE_2 {
      @Override
      public String toString() {
        return "feature2";
      }
    }
  }

  @Test
  public void testEnumStateSet() {
    StateSet stateSet =
        StateSet.builder()
            .name("feature_flags")
            .labelNames("environment")
            .states(MyFeatureFlag.class)
            .build();
    stateSet.labelValues("dev").setTrue(MyFeatureFlag.EXPERIMENTAL_FEATURE_2);
    stateSet.labelValues("prod").setFalse(MyFeatureFlag.EXPERIMENTAL_FEATURE_2);
    StateSetSnapshot snapshot = stateSet.collect();
    assertThat(snapshot.getDataPoints()).hasSize(2);
    assertThat(getData(stateSet, "environment", "dev").size()).isEqualTo(2);
    assertThat(getData(stateSet, "environment", "dev").getName(0)).isEqualTo("feature1");
    assertThat(getData(stateSet, "environment", "dev").isTrue(0)).isFalse();
    assertThat(getData(stateSet, "environment", "dev").getName(1)).isEqualTo("feature2");
    assertThat(getData(stateSet, "environment", "dev").isTrue(1)).isTrue();
    assertThat(getData(stateSet, "environment", "prod").size()).isEqualTo(2);
    assertThat(getData(stateSet, "environment", "prod").getName(0)).isEqualTo("feature1");
    assertThat(getData(stateSet, "environment", "prod").isTrue(0)).isFalse();
    assertThat(getData(stateSet, "environment", "prod").getName(1)).isEqualTo("feature2");
    assertThat(getData(stateSet, "environment", "prod").isTrue(1)).isFalse();
  }

  @Test
  public void testDefaultFalse() {
    StateSet stateSet =
        StateSet.builder().name("test").states("state1", "state2", "state3").build();
    assertThat(getData(stateSet).size()).isEqualTo(3);
    assertThat(getData(stateSet).getName(0)).isEqualTo("state1");
    assertThat(getData(stateSet).isTrue(0)).isFalse();
    assertThat(getData(stateSet).getName(1)).isEqualTo("state2");
    assertThat(getData(stateSet).isTrue(1)).isFalse();
    assertThat(getData(stateSet).getName(2)).isEqualTo("state3");
    assertThat(getData(stateSet).isTrue(2)).isFalse();
  }

  private StateSetSnapshot.StateSetDataPointSnapshot getData(StateSet stateSet, String... labels) {
    return stateSet.collect().getDataPoints().stream()
        .filter(d -> d.getLabels().equals(Labels.of(labels)))
        .findAny()
        .orElseThrow(
            () ->
                new RuntimeException(
                    "stateset with labels " + Arrays.toString(labels) + " not found"));
  }

  @Test
  public void testStatesCannotBeEmpty() {
    assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(
                () ->
    StateSet.builder().name("invalid").build());
  }
}
