package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.model.snapshots.CounterSnapshot.CounterDataPointSnapshot;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class CounterSnapshotTest {

    @Test
    public void testCompleteGoodCase() {
        long createdTimestamp1 = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
        long createdTimestamp2 = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(2);
        long exemplarTimestamp = System.currentTimeMillis();
        CounterSnapshot snapshot = CounterSnapshot.builder()
                .name("http_server_requests_seconds")
                .help("total time spent serving requests")
                .unit(Unit.SECONDS)
                .dataPoint(CounterDataPointSnapshot.builder()
                        .value(1.0)
                        .exemplar(Exemplar.builder()
                                .value(3.0)
                                .traceId("abc123")
                                .spanId("123457")
                                .timestampMillis(exemplarTimestamp)
                                .build())
                        .labels(Labels.builder()
                                .label("path", "/world")
                                .build())
                        .createdTimestampMillis(createdTimestamp1)
                        .build()
                ).dataPoint(CounterDataPointSnapshot.builder()
                        .value(2.0)
                        .exemplar(Exemplar.builder()
                                .value(4.0)
                                .traceId("def456")
                                .spanId("234567")
                                .timestampMillis(exemplarTimestamp)
                                .build())
                        .labels(Labels.builder()
                                .label("path", "/hello")
                                .build())
                        .createdTimestampMillis(createdTimestamp2)
                        .build()
                )
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "http_server_requests_seconds", "total time spent serving requests", "seconds");
        Assert.assertEquals(2, snapshot.getDataPoints().size());
        CounterDataPointSnapshot data = snapshot.getDataPoints().get(0); // data is sorted by labels, so the first one should be path="/hello"
        Assert.assertEquals(Labels.of("path", "/hello"), data.getLabels());
        Assert.assertEquals(2.0, data.getValue(), 0.0);
        Assert.assertEquals(4.0, data.getExemplar().getValue(), 0.0);
        Assert.assertEquals(createdTimestamp2, data.getCreatedTimestampMillis());
        Assert.assertFalse(data.hasScrapeTimestamp());
        data = snapshot.getDataPoints().get(1);
        Assert.assertEquals(Labels.of("path", "/world"), data.getLabels());
        Assert.assertEquals(1.0, data.getValue(), 0.0);
        Assert.assertEquals(3.0, data.getExemplar().getValue(), 0.0);
        Assert.assertEquals(createdTimestamp1, data.getCreatedTimestampMillis());
        Assert.assertFalse(data.hasScrapeTimestamp());
    }

    @Test
    public void testMinimalGoodCase() {
        CounterSnapshot snapshot = CounterSnapshot.builder()
                .name("events")
                .dataPoint(CounterDataPointSnapshot.builder().value(1.0).build())
                .build();
        SnapshotTestUtil.assertMetadata(snapshot, "events", null, null);
        Assert.assertEquals(1, snapshot.getDataPoints().size());
        CounterDataPointSnapshot data = snapshot.getDataPoints().get(0);
        Assert.assertEquals(Labels.EMPTY, data.getLabels());
        Assert.assertEquals(1.0, data.getValue(), 0.0);
        Assert.assertNull(data.getExemplar());
        Assert.assertFalse(data.hasCreatedTimestamp());
        Assert.assertFalse(data.hasScrapeTimestamp());
    }

    @Test
    public void testEmptyCounter() {
        CounterSnapshot snapshot = CounterSnapshot.builder().name("events").build();
        Assert.assertEquals(0, snapshot.getDataPoints().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTotalSuffixPresent() {
        CounterSnapshot.builder().name("test_total").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueMissing() {
        CounterDataPointSnapshot.builder().build();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDataImmutable() {
        CounterSnapshot snapshot = CounterSnapshot.builder()
                .name("events")
                .dataPoint(CounterDataPointSnapshot.builder().labels(Labels.of("a", "a")).value(1.0).build())
                .dataPoint(CounterDataPointSnapshot.builder().labels(Labels.of("a", "b")).value(2.0).build())
                .build();
        Iterator<CounterDataPointSnapshot> iterator = snapshot.getDataPoints().iterator();
        iterator.next();
        iterator.remove();
    }
}
