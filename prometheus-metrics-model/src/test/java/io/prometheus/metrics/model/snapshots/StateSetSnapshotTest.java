package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

class StateSetSnapshotTest {

    @Test
    void testCompleteGoodCase() {
        long scrapeTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
        StateSetSnapshot snapshot = StateSetSnapshot.builder()
                .name("my_feature_flags")
                .help("Feature Flags")
                .dataPoint(StateSetSnapshot.StateSetDataPointSnapshot.builder()
                        .labels(Labels.of("entity", "controller"))
                        .scrapeTimestampMillis(scrapeTimestamp)
                        .state("feature1", true)
                        .state("feature2", false)
                        .build()
                )
                .dataPoint(StateSetSnapshot.StateSetDataPointSnapshot.builder()
                        .labels(Labels.of("entity", "api"))
                        .state("feature1", false)
                        .state("feature2", false)
                        .build()
                )
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "my_feature_flags", "Feature Flags", null);
        Assertions.assertEquals(2, snapshot.getDataPoints().size());
        StateSetSnapshot.StateSetDataPointSnapshot data = snapshot.getDataPoints().get(1); // data is sorted by labels, so the second one should be entity="controller"
        Assertions.assertEquals(Labels.of("entity", "controller"), data.getLabels());
        Assertions.assertEquals(2, data.size());
        Assertions.assertEquals("feature1", data.getName(0));
        Assertions.assertTrue(data.isTrue(0));
        Assertions.assertEquals("feature2", data.getName(1));
        Assertions.assertFalse(data.isTrue(1));
        Assertions.assertTrue(data.hasScrapeTimestamp());
        Assertions.assertEquals(scrapeTimestamp, data.getScrapeTimestampMillis());
        Assertions.assertFalse(data.hasCreatedTimestamp());
    }

    @Test
    void testStateSetDataSorted() {
        StateSetSnapshot.StateSetDataPointSnapshot data = StateSetSnapshot.StateSetDataPointSnapshot.builder()
                .state("b", true)
                .state("d", false)
                .state("c", true)
                .state("a", false)
                .build();
        Assertions.assertEquals(4, data.size());
        Assertions.assertEquals("a", data.getName(0));
        Assertions.assertFalse(data.isTrue(0));
        Assertions.assertEquals("b", data.getName(1));
        Assertions.assertTrue(data.isTrue(1));
        Assertions.assertEquals("c", data.getName(2));
        Assertions.assertTrue(data.isTrue(2));
        Assertions.assertEquals("d", data.getName(3));
        Assertions.assertFalse(data.isTrue(3));
    }

    @Test
    void testMustHaveState() {
        // Must have at least one state.
        assertThrows(IllegalArgumentException.class, () -> StateSetSnapshot.StateSetDataPointSnapshot.builder().build());
    }

    @Test
    void testMinimal() {
        StateSetSnapshot snapshot = StateSetSnapshot.builder()
                .name("my_flag")
                .dataPoint(StateSetSnapshot.StateSetDataPointSnapshot.builder()
                        .state("flag", true)
                        .build()
                )
                .build();
        Assertions.assertEquals(1, snapshot.dataPoints.size());
    }

    @Test
    void testEmpty() {
        StateSetSnapshot snapshot = StateSetSnapshot.builder()
                .name("my_flag")
                .build();
        Assertions.assertEquals(0, snapshot.dataPoints.size());
    }

    @Test
    void testDataImmutable() {
        StateSetSnapshot.StateSetDataPointSnapshot data = StateSetSnapshot.StateSetDataPointSnapshot.builder()
                .state("a", true)
                .state("b", true)
                .state("c", true)
                .build();
        Iterator<StateSetSnapshot.State> iterator = data.iterator();
        iterator.next();
        Assertions.assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    void testDuplicateState() {
        assertThrows(IllegalArgumentException.class,
                () -> StateSetSnapshot.StateSetDataPointSnapshot.builder()
                        .state("a", true)
                        .state("b", true)
                        .state("a", true)
                        .build());
    }

    @Test
    void testStateSetImmutable() {
        StateSetSnapshot snapshot = StateSetSnapshot.builder()
                .name("flags")
                .dataPoint(StateSetSnapshot.StateSetDataPointSnapshot.builder()
                        .labels(Labels.of("entity", "controller"))
                        .state("feature", true)
                        .build()
                )
                .dataPoint(StateSetSnapshot.StateSetDataPointSnapshot.builder()
                        .labels(Labels.of("entity", "api"))
                        .state("feature", true)
                        .build()
                )
                .build();
        Iterator<StateSetSnapshot.StateSetDataPointSnapshot> iterator = snapshot.getDataPoints().iterator();
        iterator.next();
        Assertions.assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    void testLabelsUnique() {
        assertThrows(IllegalArgumentException.class,
                () -> StateSetSnapshot.builder()
                        .name("flags")
                        .dataPoint(StateSetSnapshot.StateSetDataPointSnapshot.builder()
                                .state("feature", true)
                                .build()
                        )
                        .dataPoint(StateSetSnapshot.StateSetDataPointSnapshot.builder()
                                .state("feature", true)
                                    .build()
                            )
                        .build());
    }
}
