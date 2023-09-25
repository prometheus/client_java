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
        SummarySnapshot snapshot = SummarySnapshot.builder()
                .name("latency_seconds")
                .help("latency in seconds")
                .unit(Unit.SECONDS)
                .dataPoint(SummarySnapshot.SummaryDataPointSnapshot.builder()
                        .createdTimestampMillis(createdTimestamp)
                        .scrapeTimestampMillis(scrapeTimestamp)
                        .labels(Labels.of("endpoint", "/"))
                        .quantiles(Quantiles.builder()
                                .quantile(0.5, 0.2)
                                .quantile(0.95, 0.22)
                                .quantile(0.99, 0.23)
                                .build())
                        .exemplars(Exemplars.builder()
                                .exemplar(Exemplar.builder()
                                        .value(0.2)
                                        .traceId("abc123")
                                        .spanId("123457")
                                        .timestampMillis(exemplarTimestamp)
                                        .build())
                                .exemplar(Exemplar.builder()
                                        .value(0.21)
                                        .traceId("abc124")
                                        .spanId("123458")
                                        .timestampMillis(exemplarTimestamp)
                                        .build())
                                .build())
                        .count(1093)
                        .sum(218.6)
                        .build())
                .dataPoint(SummarySnapshot.SummaryDataPointSnapshot.builder()
                        .labels(Labels.of("endpoint", "/test"))
                        .count(1093)
                        .sum(218.6)
                        .build())
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "latency_seconds", "latency in seconds", "seconds");
        Assert.assertEquals(2, snapshot.getDataPoints().size());
        SummarySnapshot.SummaryDataPointSnapshot data = snapshot.getDataPoints().get(0);
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

        data = snapshot.getDataPoints().get(1);
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
        Assert.assertTrue(data.hasCount());
        Assert.assertTrue(data.hasSum());
    }

    @Test
    public void testMinimal() {
        SummarySnapshot snapshot = SummarySnapshot.builder()
                .name("size_bytes")
                .dataPoint(SummarySnapshot.SummaryDataPointSnapshot.builder()
                        .count(10)
                        .sum(12.0)
                        .build())
                .build();
        Assert.assertEquals(1, snapshot.getDataPoints().size());
        Assert.assertEquals(Labels.EMPTY, snapshot.getDataPoints().get(0).getLabels());
    }

    @Test
    public void testEmptySnapshot() {
        SummarySnapshot snapshot = SummarySnapshot.builder().name("empty_summary").build();
        Assert.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test
    public void testEmptyData() {
        SummarySnapshot.SummaryDataPointSnapshot data = SummarySnapshot.SummaryDataPointSnapshot.builder().build();
        Assert.assertEquals(0, data.getQuantiles().size());
        Assert.assertFalse(data.hasCount());
        Assert.assertFalse(data.hasSum());
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
        Assert.assertEquals(0, data.getExemplars().size());
    }
}
