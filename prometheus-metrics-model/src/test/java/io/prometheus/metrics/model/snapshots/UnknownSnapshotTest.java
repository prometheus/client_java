package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UnknownSnapshotTest {

    @Test
    void testCompleteGoodCase() {
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
        Assertions.assertEquals(2, snapshot.getDataPoints().size());
        UnknownSnapshot.UnknownDataPointSnapshot data = snapshot.getDataPoints().get(1); // env="prod"
        Assertions.assertEquals(Labels.of("env", "prod"), data.getLabels());
        Assertions.assertEquals(0.3, data.getValue(), 0.0);
        Assertions.assertEquals(0.12, data.getExemplar().getValue(), 0.0);
        Assertions.assertFalse(data.hasCreatedTimestamp());
        Assertions.assertFalse(data.hasScrapeTimestamp());
    }

    @Test
    void testMinimal() {
        UnknownSnapshot snapshot = UnknownSnapshot.builder()
                .name("test")
                .dataPoint(UnknownSnapshot.UnknownDataPointSnapshot.builder()
                        .value(1.0)
                        .build())
                .build();
        Assertions.assertEquals(1, snapshot.getDataPoints().size());
    }

    @Test
    void testEmpty() {
        UnknownSnapshot snapshot = UnknownSnapshot.builder().name("test").build();
        Assertions.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test
    void testNameMissing() {
        assertThrows(IllegalArgumentException.class, () -> UnknownSnapshot.builder().build());
    }

    @Test
    void testValueMissing() {
        assertThrows(IllegalArgumentException.class, () -> UnknownSnapshot.UnknownDataPointSnapshot.builder().build());
    }
}
