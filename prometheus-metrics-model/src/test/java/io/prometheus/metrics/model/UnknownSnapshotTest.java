package io.prometheus.metrics.model;

import org.junit.Assert;
import org.junit.Test;

public class UnknownSnapshotTest {

    @Test
    public void testCompleteGoodCase() {
        long exemplarTimestamp = System.currentTimeMillis();
        UnknownSnapshot snapshot = UnknownSnapshot.newBuilder()
                .withName("my_unknown_seconds")
                .withHelp("something in seconds")
                .withUnit(Unit.SECONDS)
                .addUnknownData(UnknownSnapshot.UnknownData.newBuilder()
                        .withValue(0.3)
                        .withExemplar(Exemplar.newBuilder()
                                .withValue(0.12)
                                .withTraceId("abc123")
                                .withSpanId("123457")
                                .withTimestampMillis(exemplarTimestamp)
                                .build())
                        .withLabels(Labels.newBuilder()
                                .addLabel("env", "prod")
                                .build())
                        .build()
                ).addUnknownData(UnknownSnapshot.UnknownData.newBuilder()
                        .withValue(0.29)
                        .withLabels(Labels.newBuilder()
                                .addLabel("env", "dev")
                                .build())
                        .build()
                )
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "my_unknown_seconds", "something in seconds", "seconds");
        Assert.assertEquals(2, snapshot.getData().size());
        UnknownSnapshot.UnknownData data = snapshot.getData().get(1); // env="prod"
        Assert.assertEquals(Labels.of("env", "prod"), data.getLabels());
        Assert.assertEquals(0.3, data.getValue(), 0.0);
        Assert.assertEquals(0.12, data.getExemplar().getValue(), 0.0);
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
    }

    @Test
    public void testMinimal() {
        UnknownSnapshot snapshot = UnknownSnapshot.newBuilder()
                .withName("test")
                .addUnknownData(UnknownSnapshot.UnknownData.newBuilder()
                        .withValue(1.0)
                        .build())
                .build();
        Assert.assertEquals(1, snapshot.getData().size());
    }

    @Test
    public void testEmpty() {
        UnknownSnapshot snapshot = UnknownSnapshot.newBuilder().withName("test").build();
        Assert.assertEquals(0, snapshot.getData().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNameMissing() {
        UnknownSnapshot.newBuilder().build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueMissing() {
        UnknownSnapshot.UnknownData.newBuilder().build();
    }
}
