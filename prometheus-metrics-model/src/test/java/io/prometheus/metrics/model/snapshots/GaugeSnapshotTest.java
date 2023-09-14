package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeDataPointSnapshot;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class GaugeSnapshotTest {

    @Test
    public void testCompleteGoodCase() {
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
        Assert.assertEquals(2, snapshot.getDataPoints().size());
        GaugeDataPointSnapshot data = snapshot.getDataPoints().get(0); // data is sorted by labels, so the first one should be path="/hello"
        Assert.assertEquals(Labels.of("env", "dev"), data.getLabels());
        Assert.assertEquals(128.0, data.getValue(), 0.0);
        Assert.assertEquals(128.0, data.getExemplar().getValue(), 0.0);
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
        data = snapshot.getDataPoints().get(1);
        Assert.assertEquals(Labels.of("env", "prod"), data.getLabels());
        Assert.assertEquals(1024.0, data.getValue(), 0.0);
        Assert.assertEquals(1024.0, data.getExemplar().getValue(), 0.0);
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
    }

    @Test
    public void testMinimalGoodCase() {
        GaugeSnapshot snapshot = GaugeSnapshot.builder()
                .name("temperature")
                .dataPoint(GaugeDataPointSnapshot.builder().value(23.0).build())
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "temperature", null, null);
        Assert.assertEquals(1, snapshot.getDataPoints().size());
        GaugeDataPointSnapshot data = snapshot.getDataPoints().get(0);
        Assert.assertEquals(Labels.EMPTY, data.getLabels());
        Assert.assertEquals(23.0, data.getValue(), 0.0);
        Assert.assertNull(data.getExemplar());
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
    }

    @Test
    public void testEmptyGauge() {
        GaugeSnapshot snapshot = GaugeSnapshot.builder()
                .name("temperature")
                .build();
        Assert.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTotalSuffixPresent() {
        CounterSnapshot.builder().name("test_total").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTotalSuffixPresentDot() {
        CounterSnapshot.builder().name("test.total").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueMissing() {
        CounterDataPointSnapshot.builder().build();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
        GaugeSnapshot snapshot = GaugeSnapshot.builder()
                .name("gauge")
                .dataPoint(GaugeDataPointSnapshot.builder().labels(Labels.of("a", "a")).value(23.0).build())
                .dataPoint(GaugeDataPointSnapshot.builder().labels(Labels.of("a", "b")).value(23.0).build())
                .build();
        Iterator<GaugeDataPointSnapshot> iterator = snapshot.getDataPoints().iterator();
        iterator.next();
        iterator.remove();
    }
}
