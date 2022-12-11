package io.prometheus.metrics.core;

import io.prometheus.metrics.model.CounterSnapshot;
import io.prometheus.metrics.model.Labels;
import io.prometheus.metrics.model.StateSetSnapshot;
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
        StateSet stateSet = StateSet.newBuilder()
                .withName("feature_flags")
                .withLabelNames("environment")
                .withStates(MyFeatureFlag.class)
                .build();
        stateSet.withLabels("dev").setTrue(MyFeatureFlag.EXPERIMENTAL_FEATURE_2);
        stateSet.withLabels("prod").setFalse(MyFeatureFlag.EXPERIMENTAL_FEATURE_2);
        StateSetSnapshot snapshot = stateSet.collect();
        Assert.assertEquals(2, snapshot.getData().size());
        Assert.assertEquals(2, getData(stateSet, "environment", "dev").getStates().size());
        Assert.assertFalse(getData(stateSet, "environment", "dev").getState("feature1"));
        Assert.assertTrue(getData(stateSet, "environment", "dev").getState("feature2"));
        Assert.assertEquals(2, getData(stateSet, "environment", "prod").getStates().size());
        Assert.assertFalse(getData(stateSet, "environment", "prod").getState("feature1"));
        Assert.assertFalse(getData(stateSet, "environment", "prod").getState("feature2"));
    }

    @Test
    public void testDefaultFalse() {
        StateSet stateSet = StateSet.newBuilder()
                .withName("test")
                .withStates("state1", "state2", "state3")
                .build();
        Assert.assertEquals(3, getData(stateSet).getStates().size());
        Assert.assertFalse(getData(stateSet).getState("state1"));
        Assert.assertFalse(getData(stateSet).getState("state2"));
        Assert.assertFalse(getData(stateSet).getState("state3"));
    }

    private StateSetSnapshot.StateSetData getData(StateSet stateSet, String... labels) {
        return stateSet.collect().getData().stream()
                .filter(d -> d.getLabels().equals(Labels.of(labels)))
                .findAny()
                .orElseThrow(() -> new RuntimeException("stateset with labels " + labels + " not found"));
    }

    @Test(expected = IllegalStateException.class)
    public void testStatesCannotBeEmpty() {
        StateSet.newBuilder().withName("invalid").build();
    }
}
