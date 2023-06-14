package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import org.junit.Assert;

public class SnapshotTestUtil {

    public static void assertMetadata(MetricSnapshot snapshot, String name, String help, String unit) {
        Assert.assertEquals(name, snapshot.getMetadata().getName());
        Assert.assertEquals(help, snapshot.getMetadata().getHelp());
        if (unit != null) {
            Assert.assertEquals(unit, snapshot.getMetadata().getUnit().toString());
        } else {
            Assert.assertNull(snapshot.getMetadata().getUnit());
        }
    }
}
