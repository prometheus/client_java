package io.prometheus.metrics.model;

import org.junit.Assert;
import org.junit.Test;

public class ExemplarTest {

    @Test
    public void testGoodCaseComplete() {
        long timestamp = System.currentTimeMillis();
        Exemplar exemplar = Exemplar.newBuilder()
                .withValue(2.2)
                .withTraceId("abc123abc123")
                .withSpanId("def456def456")
                .withTimestampMillis(timestamp)
                .withLabels(Labels.of("path", "/", "error", "none"))
                .build();
        Assert.assertEquals(2.2, exemplar.getValue(), 0.0);
        Assert.assertEquals(Labels.of(Exemplar.TRACE_ID, "abc123abc123", Exemplar.SPAN_ID, "def456def456", "path", "/", "error", "none"), exemplar.getLabels());
        Assert.assertTrue(exemplar.hasTimestamp());
        Assert.assertEquals(timestamp, exemplar.getTimestampMillis());
    }

    @Test(expected = IllegalStateException.class)
    public void testValueMissing() {
        Exemplar.newBuilder().build();
    }

    @Test
    public void testMinimal() {
        Exemplar exemplar = Exemplar.newBuilder().withValue(0.0).build();
        Assert.assertEquals(0.0, exemplar.getValue(), 0.0);
        Assert.assertEquals(Labels.EMPTY, exemplar.getLabels());
        Assert.assertFalse(exemplar.hasTimestamp());
    }

    @Test
    public void testLabelsMergeTraceId() {
        Exemplar exemplar = Exemplar.newBuilder()
                .withValue(0.0)
                .withLabels(Labels.of("a", "b"))
                .withTraceId("abc")
                .build();
        Assert.assertEquals(Labels.of("a", "b", "trace_id", "abc"), exemplar.getLabels());
    }

    @Test
    public void testLabelsMergeSpanId() {
        Exemplar exemplar = Exemplar.newBuilder()
                .withValue(0.0)
                .withLabels(Labels.of("a", "b"))
                .withSpanId("abc")
                .build();
        Assert.assertEquals(Labels.of("a", "b", "span_id", "abc"), exemplar.getLabels());
    }

    @Test
    public void testLabelsMergeTraceIdAndSpanId() {
        Exemplar exemplar = Exemplar.newBuilder()
                .withValue(0.0)
                .withLabels(Labels.of("a", "b"))
                .withSpanId("abc")
                .withTraceId("def")
                .build();
        Assert.assertEquals(Labels.of("span_id", "abc", "a", "b", "trace_id", "def"), exemplar.getLabels());
    }

    @Test
    public void testLabelsMergeNone() {
        Exemplar exemplar = Exemplar.newBuilder()
                .withValue(0.0)
                .withLabels(Labels.of("a", "b"))
                .build();
        Assert.assertEquals(Labels.of("a", "b"), exemplar.getLabels());
    }
}
