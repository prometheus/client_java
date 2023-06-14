package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterData;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot.GaugeData;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class GaugeSnapshotTest {

    @Test
    public void testCompleteGoodCase() {
        long exemplarTimestamp = System.currentTimeMillis();
        GaugeSnapshot snapshot = GaugeSnapshot.newBuilder()
                .withName("cache_size_bytes")
                .withHelp("cache size in Bytes")
                .withUnit(Unit.BYTES)
                .addGaugeData(GaugeData.newBuilder()
                        .withValue(1024.0)
                        .withExemplar(Exemplar.newBuilder()
                                .withValue(1024.0)
                                .withTraceId("abc123")
                                .withSpanId("123457")
                                .withTimestampMillis(exemplarTimestamp)
                                .build())
                        .withLabels(Labels.newBuilder()
                                .addLabel("env", "prod")
                                .build())
                        .build()
                ).addGaugeData(GaugeData.newBuilder()
                        .withValue(128.0)
                        .withExemplar(Exemplar.newBuilder()
                                .withValue(128.0)
                                .withTraceId("def456")
                                .withSpanId("234567")
                                .withTimestampMillis(exemplarTimestamp)
                                .build())
                        .withLabels(Labels.newBuilder()
                                .addLabel("env", "dev")
                                .build())
                        .build()
                )
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "cache_size_bytes", "cache size in Bytes", "bytes");
        Assert.assertEquals(2, snapshot.getData().size());
        GaugeData data = snapshot.getData().get(0); // data is sorted by labels, so the first one should be path="/hello"
        Assert.assertEquals(Labels.of("env", "dev"), data.getLabels());
        Assert.assertEquals(128.0, data.getValue(), 0.0);
        Assert.assertEquals(128.0, data.getExemplar().getValue(), 0.0);
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
        data = snapshot.getData().get(1);
        Assert.assertEquals(Labels.of("env", "prod"), data.getLabels());
        Assert.assertEquals(1024.0, data.getValue(), 0.0);
        Assert.assertEquals(1024.0, data.getExemplar().getValue(), 0.0);
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
    }

    @Test
    public void testMinimalGoodCase() {
        GaugeSnapshot snapshot = GaugeSnapshot.newBuilder()
                .withName("temperature")
                .addGaugeData(GaugeData.newBuilder().withValue(23.0).build())
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "temperature", null, null);
        Assert.assertEquals(1, snapshot.getData().size());
        GaugeData data = snapshot.getData().get(0);
        Assert.assertEquals(Labels.EMPTY, data.getLabels());
        Assert.assertEquals(23.0, data.getValue(), 0.0);
        Assert.assertNull(data.getExemplar());
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
    }

    @Test
    public void testEmptyGauge() {
        GaugeSnapshot snapshot = GaugeSnapshot.newBuilder()
                .withName("temperature")
                .build();
        Assert.assertEquals(0, snapshot.getData().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTotalSuffixPresent() {
        CounterSnapshot.newBuilder().withName("test_total").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueMissing() {
        CounterData.newBuilder().build();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
        GaugeSnapshot snapshot = GaugeSnapshot.newBuilder()
                .withName("gauge")
                .addGaugeData(GaugeData.newBuilder().withLabels(Labels.of("a", "a")).withValue(23.0).build())
                .addGaugeData(GaugeData.newBuilder().withLabels(Labels.of("a", "b")).withValue(23.0).build())
                .build();
        Iterator<GaugeSnapshot.GaugeData> iterator = snapshot.getData().iterator();
        iterator.next();
        iterator.remove();
    }
}
