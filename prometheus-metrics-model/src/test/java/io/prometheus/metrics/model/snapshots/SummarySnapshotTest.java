package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;

class SummarySnapshotTest {

    @Test
    void testCompleteGoodCase() {
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
        Assertions.assertEquals(2, snapshot.getDataPoints().size());
        SummarySnapshot.SummaryDataPointSnapshot data = snapshot.getDataPoints().get(0);
        Assertions.assertEquals(Labels.of("endpoint", "/"), data.getLabels());
        Assertions.assertTrue(data.hasCount());
        Assertions.assertEquals(1093, data.getCount());
        Assertions.assertTrue(data.hasSum());
        Assertions.assertEquals(218.6, data.getSum(), 0);
        Assertions.assertTrue(data.hasCreatedTimestamp());
        Assertions.assertEquals(createdTimestamp, data.getCreatedTimestampMillis());
        Assertions.assertTrue(data.hasScrapeTimestamp());
        Assertions.assertEquals(scrapeTimestamp, data.getScrapeTimestampMillis());
        Quantiles quantiles = data.getQuantiles();
        Assertions.assertEquals(3, quantiles.size());
        // quantiles are tested in QuantilesTest already, skipping here.
        Assertions.assertEquals(2, data.getExemplars().size());
        // exemplars are tested in ExemplarsTest already, skipping here.

        data = snapshot.getDataPoints().get(1);
        Assertions.assertFalse(data.hasCreatedTimestamp());
        Assertions.assertFalse(data.hasScrapeTimestamp());
        Assertions.assertTrue(data.hasCount());
        Assertions.assertTrue(data.hasSum());
    }

    @Test
    void testMinimal() {
        SummarySnapshot snapshot = SummarySnapshot.builder()
                .name("size_bytes")
                .dataPoint(SummarySnapshot.SummaryDataPointSnapshot.builder()
                        .count(10)
                        .sum(12.0)
                        .build())
                .build();
        Assertions.assertEquals(1, snapshot.getDataPoints().size());
        Assertions.assertEquals(Labels.EMPTY, snapshot.getDataPoints().get(0).getLabels());
    }

    @Test
    void testEmptySnapshot() {
        SummarySnapshot snapshot = SummarySnapshot.builder().name("empty_summary").build();
        Assertions.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test
    void testEmptyData() {
        SummarySnapshot.SummaryDataPointSnapshot data = SummarySnapshot.SummaryDataPointSnapshot.builder().build();
        Assertions.assertEquals(0, data.getQuantiles().size());
        Assertions.assertFalse(data.hasCount());
        Assertions.assertFalse(data.hasSum());
        Assertions.assertFalse(data.hasCreatedTimestamp());
        Assertions.assertFalse(data.hasScrapeTimestamp());
        Assertions.assertEquals(0, data.getExemplars().size());
    }
}
