package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertThrows;

class GaugeSnapshotTest {

    @Test
    void testCompleteGoodCase() {
        long exemplarTimestamp = System.currentTimeMillis();
        GaugeSnapshot snapshot = GaugeSnapshot.builder()
                .name("cache_size_bytes")
                .help("cache size in Bytes")
                .unit(Unit.BYTES)
                .dataPoint(GaugeDataPointSnapshot.builder()
                        .value(1024.0)
                        .exemplar(Exemplar.builder()
                                .value(1024.0)
                                .traceId("abc123")
                                .spanId("123457")
                                .timestampMillis(exemplarTimestamp)
                                .build())
                        .labels(Labels.builder()
                                .label("env", "prod")
                                .build())
                        .build()
                ).dataPoint(GaugeDataPointSnapshot.builder()
                        .value(128.0)
                        .exemplar(Exemplar.builder()
                                .value(128.0)
                                .traceId("def456")
                                .spanId("234567")
                                .timestampMillis(exemplarTimestamp)
                                .build())
                        .labels(Labels.builder()
                                .label("env", "dev")
                                .build())
                        .build()
                )
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "cache_size_bytes", "cache size in Bytes", "bytes");
        Assertions.assertEquals(2, snapshot.getDataPoints().size());
        GaugeDataPointSnapshot data = snapshot.getDataPoints().get(0); // data is sorted by labels, so the first one should be path="/hello"
        Assertions.assertEquals(Labels.of("env", "dev"), data.getLabels());
        Assertions.assertEquals(128.0, data.getValue(), 0.0);
        Assertions.assertEquals(128.0, data.getExemplar().getValue(), 0.0);
        Assertions.assertFalse(data.hasCreatedTimestamp());
        Assertions.assertFalse(data.hasScrapeTimestamp());
        data = snapshot.getDataPoints().get(1);
        Assertions.assertEquals(Labels.of("env", "prod"), data.getLabels());
        Assertions.assertEquals(1024.0, data.getValue(), 0.0);
        Assertions.assertEquals(1024.0, data.getExemplar().getValue(), 0.0);
        Assertions.assertFalse(data.hasCreatedTimestamp());
        Assertions.assertFalse(data.hasScrapeTimestamp());
    }

    @Test
    void testMinimalGoodCase() {
        GaugeSnapshot snapshot = GaugeSnapshot.builder()
                .name("temperature")
                .dataPoint(GaugeDataPointSnapshot.builder().value(23.0).build())
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "temperature", null, null);
        Assertions.assertEquals(1, snapshot.getDataPoints().size());
        GaugeDataPointSnapshot data = snapshot.getDataPoints().get(0);
        Assertions.assertEquals(Labels.EMPTY, data.getLabels());
        Assertions.assertEquals(23.0, data.getValue(), 0.0);
        Assertions.assertNull(data.getExemplar());
        Assertions.assertFalse(data.hasCreatedTimestamp());
        Assertions.assertFalse(data.hasScrapeTimestamp());
    }

    @Test
    void testEmptyGauge() {
        GaugeSnapshot snapshot = GaugeSnapshot.builder()
                .name("temperature")
                .build();
        Assertions.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test
    void testTotalSuffixPresent() {
        assertThrows(IllegalArgumentException.class, () -> CounterSnapshot.builder().name("test_total").build());
    }

    @Test
    void testTotalSuffixPresentDot() {
        assertThrows(IllegalArgumentException.class, () -> CounterSnapshot.builder().name("test.total").build());
    }

    @Test
    void testValueMissing() {
        assertThrows(IllegalArgumentException.class, () -> CounterDataPointSnapshot.builder().build());
    }

    @Test
    void testDataImmutable() {
        GaugeSnapshot snapshot = GaugeSnapshot.builder()
                .name("gauge")
                .dataPoint(GaugeDataPointSnapshot.builder().labels(Labels.of("a", "a")).value(23.0).build())
                .dataPoint(GaugeDataPointSnapshot.builder().labels(Labels.of("a", "b")).value(23.0).build())
                .build();
        Iterator<GaugeDataPointSnapshot> iterator = snapshot.getDataPoints().iterator();
        iterator.next();
        Assertions.assertThrows(UnsupportedOperationException.class, iterator::remove);
    }
}
