package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import org.junit.jupiter.api.Assertions;


public class SnapshotTestUtil {

    public static void assertMetadata(MetricSnapshot snapshot, String name, String help, String unit) {
        Assertions.assertEquals(name, snapshot.getMetadata().getName());
        Assertions.assertEquals(help, snapshot.getMetadata().getHelp());
        if (unit != null) {
            Assertions.assertEquals(unit, snapshot.getMetadata().getUnit().toString());
        } else {
            Assertions.assertNull(snapshot.getMetadata().getUnit());
        }
    }
}
