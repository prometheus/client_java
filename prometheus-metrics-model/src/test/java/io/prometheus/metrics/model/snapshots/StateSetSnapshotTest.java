package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class StateSetSnapshotTest {

    @Test
    public void testCompleteGoodCase() {
        long scrapeTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
        StateSetSnapshot snapshot = StateSetSnapshot.newBuilder()
                .withName("my_feature_flags")
                .withHelp("Feature Flags")
                .addDataPoint(StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("entity", "controller"))
                        .withScrapeTimestampMillis(scrapeTimestamp)
                        .addState("feature1", true)
                        .addState("feature2", false)
                        .build()
                )
                .addDataPoint(StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("entity", "api"))
                        .addState("feature1", false)
                        .addState("feature2", false)
                        .build()
                )
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "my_feature_flags", "Feature Flags", null);
        Assert.assertEquals(2, snapshot.getData().size());
        StateSetSnapshot.StateSetDataPointSnapshot data = snapshot.getData().get(1); // data is sorted by labels, so the second one should be entity="controller"
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
        StateSetSnapshot.StateSetDataPointSnapshot data = StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                .addState("b", true)
                .addState("d", false)
                .addState("c", true)
                .addState("a", false)
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
        StateSetSnapshot.StateSetDataPointSnapshot.newBuilder().build();
    }

    @Test
    public void testMinimal() {
        StateSetSnapshot snapshot = StateSetSnapshot.newBuilder()
                .withName("my_flag")
                .addDataPoint(StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                        .addState("flag", true)
                        .build()
                )
                .build();
        Assert.assertEquals(1, snapshot.data.size());
    }

    @Test
    public void testEmpty() {
        StateSetSnapshot snapshot = StateSetSnapshot.newBuilder()
                .withName("my_flag")
                .build();
        Assert.assertEquals(0, snapshot.data.size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
        StateSetSnapshot.StateSetDataPointSnapshot data = StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                .addState("a", true)
                .addState("b", true)
                .addState("c", true)
                .build();
        Iterator<StateSetSnapshot.State> iterator = data.iterator();
        iterator.next();
        iterator.remove();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateState() {
        StateSetSnapshot.StateSetDataPointSnapshot data = StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                .addState("a", true)
                .addState("b", true)
                .addState("a", true)
                .build();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStateSetImmutable() {
        StateSetSnapshot snapshot = StateSetSnapshot.newBuilder()
                .withName("flags")
                .addDataPoint(StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("entity", "controller"))
                        .addState("feature", true)
                        .build()
                )
                .addDataPoint(StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("entity", "api"))
                        .addState("feature", true)
                        .build()
                )
                .build();
        Iterator<StateSetSnapshot.StateSetDataPointSnapshot> iterator = snapshot.getData().iterator();
        iterator.next();
        iterator.remove();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLabelsUnique() {
        StateSetSnapshot.newBuilder()
                .withName("flags")
                .addDataPoint(StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                        .addState("feature", true)
                        .build()
                )
                .addDataPoint(StateSetSnapshot.StateSetDataPointSnapshot.newBuilder()
                        .addState("feature", true)
                        .build()
                )
                .build();
    }
}
