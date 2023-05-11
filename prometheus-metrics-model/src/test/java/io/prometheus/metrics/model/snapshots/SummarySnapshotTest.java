package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class SummarySnapshotTest {

    @Test
    public void testCompleteGoodCase() {
        long createdTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
        long scrapeTimestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2);
        long exemplarTimestamp = System.currentTimeMillis();
        SummarySnapshot snapshot = SummarySnapshot.newBuilder()
                .withName("latency_seconds")
                .withHelp("latency in seconds")
                .withUnit(Unit.SECONDS)
                .addDataPoint(SummarySnapshot.SummaryDataPointSnapshot.newBuilder()
                        .withCreatedTimestampMillis(createdTimestamp)
                        .withScrapeTimestampMillis(scrapeTimestamp)
                        .withLabels(Labels.of("endpoint", "/"))
                        .withQuantiles(Quantiles.newBuilder()
                                .addQuantile(0.5, 0.2)
                                .addQuantile(0.95, 0.22)
                                .addQuantile(0.99, 0.23)
                                .build())
                        .withExemplars(Exemplars.newBuilder()
                                .addExemplar(Exemplar.newBuilder()
                                        .withValue(0.2)
                                        .withTraceId("abc123")
                                        .withSpanId("123457")
                                        .withTimestampMillis(exemplarTimestamp)
                                        .build())
                                .addExemplar(Exemplar.newBuilder()
                                        .withValue(0.21)
                                        .withTraceId("abc124")
                                        .withSpanId("123458")
                                        .withTimestampMillis(exemplarTimestamp)
                                        .build())
                                .build())
                        .withCount(1093)
                        .withSum(218.6)
                        .build())
                .addDataPoint(SummarySnapshot.SummaryDataPointSnapshot.newBuilder()
                        .withLabels(Labels.of("endpoint", "/test"))
                        .withCount(1093)
                        .withSum(218.6)
                        .build())
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "latency_seconds", "latency in seconds", "seconds");
        Assert.assertEquals(2, snapshot.getData().size());
        SummarySnapshot.SummaryDataPointSnapshot data = snapshot.getData().get(0);
        Assert.assertEquals(Labels.of("endpoint", "/"), data.getLabels());
        Assert.assertTrue(data.hasCount());
        Assert.assertEquals(1093, data.getCount());
        Assert.assertTrue(data.hasSum());
        Assert.assertEquals(218.6, data.getSum(), 0);
        Assert.assertTrue(data.hasCreatedTimestamp());
        Assert.assertEquals(createdTimestamp, data.getCreatedTimestampMillis());
        Assert.assertTrue(data.hasScrapeTimestamp());
        Assert.assertEquals(scrapeTimestamp, data.getScrapeTimestampMillis());
        Quantiles quantiles = data.getQuantiles();
        Assert.assertEquals(3, quantiles.size());
        // quantiles are tested in QuantilesTest already, skipping here.
        Assert.assertEquals(2, data.getExemplars().size());
        // exemplars are tested in ExemplarsTest already, skipping here.

        data = snapshot.getData().get(1);
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
        Assert.assertTrue(data.hasCount());
        Assert.assertTrue(data.hasSum());
    }

    @Test
    public void testMinimal() {
        SummarySnapshot snapshot = SummarySnapshot.newBuilder()
                .withName("size_bytes")
                .addDataPoint(SummarySnapshot.SummaryDataPointSnapshot.newBuilder()
                        .withCount(10)
                        .withSum(12.0)
                        .build())
                .build();
        Assert.assertEquals(1, snapshot.getData().size());
        Assert.assertEquals(Labels.EMPTY, snapshot.getData().get(0).getLabels());
    }

    @Test
    public void testEmptySnapshot() {
        SummarySnapshot snapshot = SummarySnapshot.newBuilder().withName("empty_summary").build();
        Assert.assertEquals(0, snapshot.getData().size());
    }

    @Test
    public void testEmptyData() {
        SummarySnapshot.SummaryDataPointSnapshot data = SummarySnapshot.SummaryDataPointSnapshot.newBuilder().build();
        Assert.assertEquals(0, data.getQuantiles().size());
        Assert.assertFalse(data.hasCount());
        Assert.assertFalse(data.hasSum());
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
        Assert.assertEquals(0, data.getExemplars().size());
    }
}
