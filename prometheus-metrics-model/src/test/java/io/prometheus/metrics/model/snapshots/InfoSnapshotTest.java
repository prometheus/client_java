package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertThrows;

class InfoSnapshotTest {

    @Test
    void testCompleteGoodCase() {
        InfoSnapshot snapshot = InfoSnapshot.builder()
                .name("target")
                .help("Target info")
                .dataPoint(InfoSnapshot.InfoDataPointSnapshot.builder()
                        .labels(Labels.of("instance_id", "127.0.0.1:9100", "service_name", "gateway"))
                        .build())
                .build();
        Assertions.assertEquals("target", snapshot.getMetadata().getName());
        Assertions.assertEquals("Target info", snapshot.getMetadata().getHelp());
        Assertions.assertFalse(snapshot.getMetadata().hasUnit());
        Assertions.assertEquals(1, snapshot.getDataPoints().size());
    }

    @Test
    void testEmptyInfo() {
        InfoSnapshot snapshot = InfoSnapshot.builder().name("target").build();
        Assertions.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test
    void testDataImmutable() {
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
        Assertions.assertThrows(UnsupportedOperationException.class, iterator::remove);
    }

    @Test
    void testNameMustNotIncludeSuffix() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoSnapshot.builder()
                        .name("jvm_info")
                        .build());
    }

    @Test
    void testNameMustNotIncludeSuffixDot() {
        assertThrows(IllegalArgumentException.class,
                () -> InfoSnapshot.builder()
                        .name("jvm.info")
                        .build());
    }
}
