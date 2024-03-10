package io.prometheus.metrics.core.metrics;

import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.StateSetSnapshot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StateSetTest {

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
    void testEnumStateSet() {
        StateSet stateSet = StateSet.builder()
                .name("feature_flags")
                .labelNames("environment")
                .states(MyFeatureFlag.class)
                .build();
        stateSet.labelValues("dev").setTrue(MyFeatureFlag.EXPERIMENTAL_FEATURE_2);
        stateSet.labelValues("prod").setFalse(MyFeatureFlag.EXPERIMENTAL_FEATURE_2);
        StateSetSnapshot snapshot = stateSet.collect();
        Assertions.assertEquals(2, snapshot.getDataPoints().size());
        Assertions.assertEquals(2, getData(stateSet, "environment", "dev").size());
        Assertions.assertEquals("feature1", getData(stateSet, "environment", "dev").getName(0));
        Assertions.assertFalse(getData(stateSet, "environment", "dev").isTrue(0));
        Assertions.assertEquals("feature2", getData(stateSet, "environment", "dev").getName(1));
        Assertions.assertTrue(getData(stateSet, "environment", "dev").isTrue(1));
        Assertions.assertEquals(2, getData(stateSet, "environment", "prod").size());
        Assertions.assertEquals("feature1", getData(stateSet, "environment", "prod").getName(0));
        Assertions.assertFalse(getData(stateSet, "environment", "prod").isTrue(0));
        Assertions.assertEquals("feature2", getData(stateSet, "environment", "prod").getName(1));
        Assertions.assertFalse(getData(stateSet, "environment", "prod").isTrue(1));
    }

    @Test
    void testDefaultFalse() {
        StateSet stateSet = StateSet.builder()
                .name("test")
                .states("state1", "state2", "state3")
                .build();
        Assertions.assertEquals(3, getData(stateSet).size());
        Assertions.assertEquals("state1", getData(stateSet).getName(0));
        Assertions.assertFalse(getData(stateSet).isTrue(0));
        Assertions.assertEquals("state2", getData(stateSet).getName(1));
        Assertions.assertFalse(getData(stateSet).isTrue(1));
        Assertions.assertEquals("state3", getData(stateSet).getName(2));
        Assertions.assertFalse(getData(stateSet).isTrue(2));
    }

    private StateSetSnapshot.StateSetDataPointSnapshot getData(StateSet stateSet, String... labels) {
        return stateSet.collect().getDataPoints().stream()
                .filter(d -> d.getLabels().equals(Labels.of(labels)))
                .findAny()
                .orElseThrow(() -> new RuntimeException("stateset with labels " + labels + " not found"));
    }

    @Test
    void testStatesCannotBeEmpty() {
        Assertions.assertThrows(IllegalStateException.class, () -> StateSet.builder().name("invalid").build());
    }
}
