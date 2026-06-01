package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class StateSetSnapshotTest {

  @Test
  void testCompleteGoodCase() {
    long scrapeTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
    StateSetSnapshot snapshot =
        StateSetSnapshot.builder()
            .name("my_feature_flags")
            .help("Feature Flags")
            .dataPoint(
                StateSetSnapshot.StateSetDataPointSnapshot.builder()
                    .labels(Labels.of("entity", "controller"))
                    .scrapeTimestampMillis(scrapeTimestamp)
                    .state("feature1", true)
                    .state("feature2", false)
                    .build())
            .dataPoint(
                StateSetSnapshot.StateSetDataPointSnapshot.builder()
                    .labels(Labels.of("entity", "api"))
                    .state("feature1", false)
                    .state("feature2", false)
                    .build())
            .build();
    SnapshotTestUtil.assertMetadata(snapshot, "my_feature_flags", "Feature Flags", null);
    assertThat(snapshot.getDataPoints()).hasSize(2);
    StateSetSnapshot.StateSetDataPointSnapshot data =
        snapshot
            .getDataPoints()
            .get(1); // data is sorted by labels, so the second one should be entity="controller"
    assertThat((Iterable<? extends Label>) data.getLabels())
        .isEqualTo(Labels.of("entity", "controller"));
    assertThat(data.size()).isEqualTo(2);
    assertThat(data.getName(0)).isEqualTo("feature1");
    assertThat(data.isTrue(0)).isTrue();
    assertThat(data.getName(1)).isEqualTo("feature2");
    assertThat(data.isTrue(1)).isFalse();
    assertThat(data.hasScrapeTimestamp()).isTrue();
    assertThat(data.getScrapeTimestampMillis()).isEqualTo(scrapeTimestamp);
    assertThat(data.hasCreatedTimestamp()).isFalse();
  }

  @Test
  void testStateSetDataSorted() {
    StateSetSnapshot.StateSetDataPointSnapshot data =
        StateSetSnapshot.StateSetDataPointSnapshot.builder()
            .state("b", true)
            .state("d", false)
            .state("c", true)
            .state("a", false)
            .build();
    assertThat(data.size()).isEqualTo(4);
    assertThat(data.getName(0)).isEqualTo("a");
    assertThat(data.isTrue(0)).isFalse();
    assertThat(data.getName(1)).isEqualTo("b");
    assertThat(data.isTrue(1)).isTrue();
    assertThat(data.getName(2)).isEqualTo("c");
    assertThat(data.isTrue(2)).isTrue();
    assertThat(data.getName(3)).isEqualTo("d");
    assertThat(data.isTrue(3)).isFalse();
  }

  @Test
  void testMustHaveState() {
    // Must have at least one state.
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> StateSetSnapshot.StateSetDataPointSnapshot.builder().build());
  }

  @Test
  void testMinimal() {
    StateSetSnapshot snapshot =
        StateSetSnapshot.builder()
            .name("my_flag")
            .dataPoint(
                StateSetSnapshot.StateSetDataPointSnapshot.builder().state("flag", true).build())
            .build();
    assertThat(snapshot.dataPoints.size()).isOne();
  }

  @Test
  void testEmpty() {
    StateSetSnapshot snapshot = StateSetSnapshot.builder().name("my_flag").build();
    assertThat(snapshot.dataPoints).isEmpty();
  }

  @Test
  void testDataImmutable() {
    StateSetSnapshot.StateSetDataPointSnapshot data =
        StateSetSnapshot.StateSetDataPointSnapshot.builder()
            .state("a", true)
            .state("b", true)
            .state("c", true)
            .build();
    assertThat(data.iterator().next())
        .usingRecursiveComparison()
        .isEqualTo(data.stream().iterator().next());
    Iterator<StateSetSnapshot.State> iterator = data.iterator();
    StateSetSnapshot.State state = iterator.next();
    assertThat(state.getName()).isEqualTo("a");
    assertThat(state.isTrue()).isTrue();
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(iterator::remove);
  }

  @Test
  void testDuplicateState() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                StateSetSnapshot.StateSetDataPointSnapshot.builder()
                    .state("a", true)
                    .state("b", true)
                    .state("a", true)
                    .build());
  }

  @Test
  void noUnit() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> StateSetSnapshot.builder().name("flags").unit(Unit.BYTES).build());
  }

  @Test
  void testStateSetImmutable() {
    StateSetSnapshot snapshot =
        StateSetSnapshot.builder()
            .name("flags")
            .dataPoint(
                StateSetSnapshot.StateSetDataPointSnapshot.builder()
                    .labels(Labels.of("entity", "controller"))
                    .state("feature", true)
                    .build())
            .dataPoint(
                StateSetSnapshot.StateSetDataPointSnapshot.builder()
                    .labels(Labels.of("entity", "api"))
                    .state("feature", true)
                    .build())
            .build();
    Iterator<StateSetSnapshot.StateSetDataPointSnapshot> iterator =
        snapshot.getDataPoints().iterator();
    iterator.next();
    assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(iterator::remove);
  }

  @Test
  void testLabelsUnique() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(
            () ->
                StateSetSnapshot.builder()
                    .name("flags")
                    .dataPoint(
                        StateSetSnapshot.StateSetDataPointSnapshot.builder()
                            .state("feature", true)
                            .build())
                    .dataPoint(
                        StateSetSnapshot.StateSetDataPointSnapshot.builder()
                            .state("feature", true)
                            .build())
                    .build());
  }

  @Test
  void testSortSmallInputMaintainsPairs() {
    int size = 5;
    String[] names = new String[size];
    boolean[] values = new boolean[size];
    Map<String, Boolean> expectedValues = new TreeMap<>();
    for (int i = 0; i < size; i++) {
      names[i] = "state_" + i;
      values[i] = i % 3 == 0;
      expectedValues.put(names[i], values[i]);
    }

    Random random = new Random(16L);
    for (int i = size - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      String name = names[i];
      names[i] = names[j];
      names[j] = name;
      boolean value = values[i];
      values[i] = values[j];
      values[j] = value;
    }

    StateSetSnapshot.StateSetDataPointSnapshot snapshot =
        new StateSetSnapshot.StateSetDataPointSnapshot(names, values, Labels.EMPTY);
    for (int i = 1; i < snapshot.size(); i++) {
      assertThat(snapshot.getName(i - 1)).isLessThan(snapshot.getName(i));
    }
    for (int i = 0; i < snapshot.size(); i++) {
      assertThat(snapshot.isTrue(i)).isEqualTo(expectedValues.get(snapshot.getName(i)));
    }
  }

  @Test
  void testSortMediumInputMaintainsPairs() {
    int size = 25;
    String[] names = new String[size];
    boolean[] values = new boolean[size];
    Map<String, Boolean> expectedValues = new TreeMap<>();
    for (int i = 0; i < size; i++) {
      names[i] = "state_" + i;
      values[i] = i % 3 == 0;
      expectedValues.put(names[i], values[i]);
    }

    Random random = new Random(17L);
    for (int i = size - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      String name = names[i];
      names[i] = names[j];
      names[j] = name;
      boolean value = values[i];
      values[i] = values[j];
      values[j] = value;
    }

    StateSetSnapshot.StateSetDataPointSnapshot snapshot =
        new StateSetSnapshot.StateSetDataPointSnapshot(names, values, Labels.EMPTY);
    for (int i = 1; i < snapshot.size(); i++) {
      assertThat(snapshot.getName(i - 1)).isLessThan(snapshot.getName(i));
    }
    for (int i = 0; i < snapshot.size(); i++) {
      assertThat(snapshot.isTrue(i)).isEqualTo(expectedValues.get(snapshot.getName(i)));
    }
  }

  @Test
  void testSortLargeInputMaintainsPairs() {
    int size = 64;
    String[] names = new String[size];
    boolean[] values = new boolean[size];
    Map<String, Boolean> expectedValues = new TreeMap<>();
    for (int i = 0; i < size; i++) {
      names[i] = "state_" + i;
      values[i] = i % 3 == 0;
      expectedValues.put(names[i], values[i]);
    }

    Random random = new Random(4L);
    for (int i = size - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      String name = names[i];
      names[i] = names[j];
      names[j] = name;
      boolean value = values[i];
      values[i] = values[j];
      values[j] = value;
    }

    StateSetSnapshot.StateSetDataPointSnapshot snapshot =
        new StateSetSnapshot.StateSetDataPointSnapshot(names, values, Labels.EMPTY);
    for (int i = 1; i < snapshot.size(); i++) {
      assertThat(snapshot.getName(i - 1)).isLessThan(snapshot.getName(i));
    }
    for (int i = 0; i < snapshot.size(); i++) {
      assertThat(snapshot.isTrue(i)).isEqualTo(expectedValues.get(snapshot.getName(i)));
    }
  }
}
