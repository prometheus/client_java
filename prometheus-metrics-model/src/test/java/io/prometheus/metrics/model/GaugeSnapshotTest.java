package io.prometheus.metrics.model;

import io.prometheus.metrics.model.CounterSnapshot.CounterData;
import io.prometheus.metrics.model.GaugeSnapshot.GaugeData;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.crypto.Data;
import java.util.concurrent.TimeUnit;

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
        CounterSnapshot snapshot = CounterSnapshot.newBuilder()
                .withName("events")
                .addCounterData(CounterData.newBuilder().withValue(1.0).build())
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "events", null, null);
        Assert.assertEquals(1, snapshot.getData().size());
        CounterData data = snapshot.getData().get(0);
        Assert.assertEquals(Labels.EMPTY, data.getLabels());
        Assert.assertEquals(1.0, data.getValue(), 0.0);
        Assert.assertNull(data.getExemplar());
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTotalSuffixPresent() {
        CounterSnapshot.newBuilder().withName("test_total").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueMissing() {
        CounterData.newBuilder().build();
    }
}
