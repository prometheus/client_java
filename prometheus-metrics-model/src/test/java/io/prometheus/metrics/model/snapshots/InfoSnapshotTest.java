package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class InfoSnapshotTest {

    @Test
    public void testCompleteGoodCase() {
        InfoSnapshot snapshot = InfoSnapshot.builder()
                .name("target")
                .help("Target info")
                .dataPoint(InfoSnapshot.InfoDataPointSnapshot.builder()
                        .labels(Labels.of("instance_id", "127.0.0.1:9100", "service_name", "gateway"))
                        .build())
                .build();
        Assert.assertEquals("target", snapshot.getMetadata().getName());
        Assert.assertEquals("Target info", snapshot.getMetadata().getHelp());
        Assert.assertFalse(snapshot.getMetadata().hasUnit());
        Assert.assertEquals(1, snapshot.getDataPoints().size());
    }

    @Test
    public void testEmptyInfo() {
        InfoSnapshot snapshot = InfoSnapshot.builder().name("target").build();
        Assert.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
        InfoSnapshot snapshot = InfoSnapshot.builder()
                .name("target")
                .dataPoint(InfoSnapshot.InfoDataPointSnapshot.builder()
                        .labels(Labels.of("instance_id", "127.0.0.1:9100", "service_name", "gateway.v1"))
                        .build())
                .dataPoint(InfoSnapshot.InfoDataPointSnapshot.builder()
                        .labels(Labels.of("instance_id", "127.0.0.1:9200", "service_name", "gateway.v2"))
                        .build())
                .build();
        Iterator<InfoSnapshot.InfoDataPointSnapshot> iterator = snapshot.getDataPoints().iterator();
        iterator.next();
        iterator.remove();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameMustNotIncludeSuffix() {
        InfoSnapshot.builder()
                .name("jvm_info")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameMustNotIncludeSuffixDot() {
        InfoSnapshot.builder()
                .name("jvm.info")
                .build();
    }
}
