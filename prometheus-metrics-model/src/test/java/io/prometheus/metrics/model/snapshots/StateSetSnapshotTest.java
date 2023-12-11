package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class StateSetSnapshotTest {

    @Test
    public void testCompleteGoodCase() {
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
        Assert.assertEquals(2, snapshot.getDataPoints().size());
        StateSetSnapshot.StateSetDataPointSnapshot data = snapshot.getDataPoints().get(1); // data is sorted by labels, so the second one should be entity="controller"
        Assert.assertEquals(Labels.of("entity", "controller"), data.getLabels());
        Assert.assertEquals(2, data.size());
        Assert.assertEquals("feature1", data.getName(0));
        Assert.assertTrue(data.isTrue(0));
        Assert.assertEquals("feature2", data.getName(1));
        Assert.assertFalse(data.isTrue(1));
        Assert.assertTrue(data.hasScrapeTimestamp());
        Assert.assertEquals(scrapeTimestamp, data.getScrapeTimestampMillis());
        Assert.assertFalse(data.hasCreatedTimestamp());
    }

    @Test
    public void testStateSetDataSorted() {
        StateSetSnapshot.StateSetDataPointSnapshot data = StateSetSnapshot.StateSetDataPointSnapshot.builder()
                .state("b", true)
                .state("d", false)
                .state("c", true)
                .state("a", false)
                .build();
        Assert.assertEquals(4, data.size());
        Assert.assertEquals("a", data.getName(0));
        Assert.assertFalse(data.isTrue(0));
        Assert.assertEquals("b", data.getName(1));
        Assert.assertTrue(data.isTrue(1));
        Assert.assertEquals("c", data.getName(2));
        Assert.assertTrue(data.isTrue(2));
        Assert.assertEquals("d", data.getName(3));
        Assert.assertFalse(data.isTrue(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMustHaveState() {
        // Must have at least one state.
        StateSetSnapshot.StateSetDataPointSnapshot.builder().build();
    }

    @Test
    public void testMinimal() {
        StateSetSnapshot snapshot = StateSetSnapshot.builder()
                .name("my_flag")
                .dataPoint(StateSetSnapshot.StateSetDataPointSnapshot.builder()
                        .state("flag", true)
                        .build()
                )
                .build();
        Assert.assertEquals(1, snapshot.dataPoints.size());
    }

    @Test
    public void testEmpty() {
        StateSetSnapshot snapshot = StateSetSnapshot.builder()
                .name("my_flag")
                .build();
        Assert.assertEquals(0, snapshot.dataPoints.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
        StateSetSnapshot.StateSetDataPointSnapshot data = StateSetSnapshot.StateSetDataPointSnapshot.builder()
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
        StateSetSnapshot.StateSetDataPointSnapshot data = StateSetSnapshot.StateSetDataPointSnapshot.builder()
                .state("a", true)
                .state("b", true)
                .state("a", true)
                .build();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStateSetImmutable() {
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
        iterator.remove();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLabelsUnique() {
        StateSetSnapshot.builder()
                .name("flags")
                .dataPoint(StateSetSnapshot.StateSetDataPointSnapshot.builder()
                        .state("feature", true)
                        .build()
                )
                .dataPoint(StateSetSnapshot.StateSetDataPointSnapshot.builder()
                        .state("feature", true)
                        .build()
                )
                .build();
    }
}
