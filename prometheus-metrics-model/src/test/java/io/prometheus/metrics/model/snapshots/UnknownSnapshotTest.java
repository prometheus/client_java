package io.prometheus.metrics.model.snapshots;

import org.junit.Assert;
import org.junit.Test;

public class UnknownSnapshotTest {

    @Test
    public void testCompleteGoodCase() {
        long exemplarTimestamp = System.currentTimeMillis();
        UnknownSnapshot snapshot = UnknownSnapshot.builder()
                .name("my_unknown_seconds")
                .help("something in seconds")
                .unit(Unit.SECONDS)
                .dataPoint(UnknownSnapshot.UnknownDataPointSnapshot.builder()
                        .value(0.3)
                        .exemplar(Exemplar.builder()
                                .value(0.12)
                                .traceId("abc123")
                                .spanId("123457")
                                .timestampMillis(exemplarTimestamp)
                                .build())
                        .labels(Labels.builder()
                                .label("env", "prod")
                                .build())
                        .build()
                ).dataPoint(UnknownSnapshot.UnknownDataPointSnapshot.builder()
                        .value(0.29)
                        .labels(Labels.builder()
                                .label("env", "dev")
                                .build())
                        .build()
                )
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "my_unknown_seconds", "something in seconds", "seconds");
        Assert.assertEquals(2, snapshot.getDataPoints().size());
        UnknownSnapshot.UnknownDataPointSnapshot data = snapshot.getDataPoints().get(1); // env="prod"
        Assert.assertEquals(Labels.of("env", "prod"), data.getLabels());
        Assert.assertEquals(0.3, data.getValue(), 0.0);
        Assert.assertEquals(0.12, data.getExemplar().getValue(), 0.0);
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
    }

    @Test
    public void testMinimal() {
        UnknownSnapshot snapshot = UnknownSnapshot.builder()
                .name("test")
                .dataPoint(UnknownSnapshot.UnknownDataPointSnapshot.builder()
                        .value(1.0)
                        .build())
                .build();
        Assert.assertEquals(1, snapshot.getDataPoints().size());
    }

    @Test
    public void testEmpty() {
        UnknownSnapshot snapshot = UnknownSnapshot.builder().name("test").build();
        Assert.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameMissing() {
        UnknownSnapshot.builder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueMissing() {
        UnknownSnapshot.UnknownDataPointSnapshot.builder().build();
    }

    @Test
    public void testUnknownDataPointSnapshot() {
        Labels labels = Labels.of("k1", "v1");
        Exemplar exemplar = Exemplar.builder().value(2.0).build();

        UnknownSnapshot.UnknownDataPointSnapshot data = new UnknownSnapshot.UnknownDataPointSnapshot(1.0, labels, exemplar);
        Assert.assertEquals(1.0, data.getValue(), 0.1);
        Assert.assertEquals(labels, data.getLabels());
        Assert.assertEquals(exemplar, data.getExemplar());

        data = new UnknownSnapshot.UnknownDataPointSnapshot(1.0, labels, exemplar, 0L);
        Assert.assertEquals(1.0, data.getValue(), 0.1);
        Assert.assertEquals(labels, data.getLabels());
        Assert.assertEquals(exemplar, data.getExemplar());
    }
}
