package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class InfoSnapshotTest {

    @Test
    public void testCompleteGoodCase() {
        InfoSnapshot snapshot = InfoSnapshot.newBuilder()
                .withName("target")
                .withHelp("Target info")
                .addDataPoint(InfoSnapshot.InfoDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("instance_id", "127.0.0.1:9100", "service_name", "gateway"))
                        .build())
                .build();
        Assert.assertEquals("target", snapshot.getMetadata().getName());
        Assert.assertEquals("Target info", snapshot.getMetadata().getHelp());
        Assert.assertFalse(snapshot.getMetadata().hasUnit());
        Assert.assertEquals(1, snapshot.getData().size());
    }

    @Test
    public void testEmptyInfo() {
        InfoSnapshot snapshot = InfoSnapshot.newBuilder().withName("target").build();
        Assert.assertEquals(0, snapshot.getData().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
        InfoSnapshot snapshot = InfoSnapshot.newBuilder()
                .withName("target")
                .addDataPoint(InfoSnapshot.InfoDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("instance_id", "127.0.0.1:9100", "service_name", "gateway.v1"))
                        .build())
                .addDataPoint(InfoSnapshot.InfoDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("instance_id", "127.0.0.1:9200", "service_name", "gateway.v2"))
                        .build())
                .build();
        Iterator<InfoSnapshot.InfoDataPointSnapshot> iterator = snapshot.getData().iterator();
        iterator.next();
        iterator.remove();
    }
}
