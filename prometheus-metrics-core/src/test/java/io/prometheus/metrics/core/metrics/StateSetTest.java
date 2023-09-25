package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import org.junit.Assert;
import org.junit.Test;

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
        StateSet stateSet = StateSet.builder()
                .name("feature_flags")
                .labelNames("environment")
                .states(MyFeatureFlag.class)
                .build();
        stateSet.labelValues("dev").setTrue(MyFeatureFlag.EXPERIMENTAL_FEATURE_2);
        stateSet.labelValues("prod").setFalse(MyFeatureFlag.EXPERIMENTAL_FEATURE_2);
        StateSetSnapshot snapshot = stateSet.collect();
        Assert.assertEquals(2, snapshot.getDataPoints().size());
        Assert.assertEquals(2, getData(stateSet, "environment", "dev").size());
        Assert.assertEquals("feature1", getData(stateSet, "environment", "dev").getName(0));
        Assert.assertFalse(getData(stateSet, "environment", "dev").isTrue(0));
        Assert.assertEquals("feature2", getData(stateSet, "environment", "dev").getName(1));
        Assert.assertTrue(getData(stateSet, "environment", "dev").isTrue(1));
        Assert.assertEquals(2, getData(stateSet, "environment", "prod").size());
        Assert.assertEquals("feature1", getData(stateSet, "environment", "prod").getName(0));
        Assert.assertFalse(getData(stateSet, "environment", "prod").isTrue(0));
        Assert.assertEquals("feature2", getData(stateSet, "environment", "prod").getName(1));
        Assert.assertFalse(getData(stateSet, "environment", "prod").isTrue(1));
    }

    @Test
    public void testDefaultFalse() {
        StateSet stateSet = StateSet.builder()
                .name("test")
                .states("state1", "state2", "state3")
                .build();
        Assert.assertEquals(3, getData(stateSet).size());
        Assert.assertEquals("state1", getData(stateSet).getName(0));
        Assert.assertFalse(getData(stateSet).isTrue(0));
        Assert.assertEquals("state2", getData(stateSet).getName(1));
        Assert.assertFalse(getData(stateSet).isTrue(1));
        Assert.assertEquals("state3", getData(stateSet).getName(2));
        Assert.assertFalse(getData(stateSet).isTrue(2));
    }

    private StateSetSnapshot.StateSetDataPointSnapshot getData(StateSet stateSet, String... labels) {
        return stateSet.collect().getDataPoints().stream()
                .filter(d -> d.getLabels().equals(Labels.of(labels)))
                .findAny()
                .orElseThrow(() -> new RuntimeException("stateset with labels " + labels + " not found"));
    }

    @Test(expected = IllegalStateException.class)
    public void testStatesCannotBeEmpty() {
        StateSet.builder().name("invalid").build();
    }
}
