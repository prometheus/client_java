package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class StateSetSnapshotTest {

  @Test
  public void testCompleteGoodCase() {
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
  public void testStateSetDataSorted() {
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

  @Test(expected = IllegalArgumentException.class)
  public void testMustHaveState() {
    // Must have at least one state.
    StateSetSnapshot.StateSetDataPointSnapshot.builder().build();
  }

  @Test
  public void testMinimal() {
    StateSetSnapshot snapshot =
        StateSetSnapshot.builder()
            .name("my_flag")
            .dataPoint(
                StateSetSnapshot.StateSetDataPointSnapshot.builder().state("flag", true).build())
            .build();
    assertThat(snapshot.dataPoints.size()).isOne();
  }

  @Test
  public void testEmpty() {
    StateSetSnapshot snapshot = StateSetSnapshot.builder().name("my_flag").build();
    assertThat(snapshot.dataPoints).isEmpty();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testDataImmutable() {
    StateSetSnapshot.StateSetDataPointSnapshot data =
        StateSetSnapshot.StateSetDataPointSnapshot.builder()
            .state("a", true)
            .state("b", true)
            .state("c", true)
            .build();
    Iterator<StateSetSnapshot.State> iterator = data.iterator();
    iterator.next();
    iterator.remove();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDuplicateState() {
    StateSetSnapshot.StateSetDataPointSnapshot.builder()
        .state("a", true)
        .state("b", true)
        .state("a", true)
        .build();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testStateSetImmutable() {
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
    iterator.remove();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLabelsUnique() {
    StateSetSnapshot.builder()
        .name("flags")
        .dataPoint(
            StateSetSnapshot.StateSetDataPointSnapshot.builder().state("feature", true).build())
        .dataPoint(
            StateSetSnapshot.StateSetDataPointSnapshot.builder().state("feature", true).build())
        .build();
  }
}
