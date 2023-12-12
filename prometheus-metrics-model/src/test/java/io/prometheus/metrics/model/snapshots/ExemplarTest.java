package io.prometheus.metrics.model.snapshots;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ExemplarTest {

    @Test
    void testGoodCaseComplete() {
        long timestamp = System.currentTimeMillis();
        Exemplar exemplar = Exemplar.builder()
                .value(2.2)
                .traceId("abc123abc123")
                .spanId("def456def456")
                .timestampMillis(timestamp)
                .labels(Labels.of("path", "/", "error", "none"))
                .build();
        Assertions.assertEquals(2.2, exemplar.getValue(), 0.0);
        Assertions.assertEquals(Labels.of(Exemplar.TRACE_ID, "abc123abc123", Exemplar.SPAN_ID, "def456def456", "path", "/", "error", "none"), exemplar.getLabels());
        Assertions.assertTrue(exemplar.hasTimestamp());
        Assertions.assertEquals(timestamp, exemplar.getTimestampMillis());
    }

    @Test
    void testValueMissing() {
        Assertions.assertThrows(IllegalStateException.class, () -> Exemplar.builder().build());
    }

    @Test
    void testMinimal() {
        Exemplar exemplar = Exemplar.builder().value(0.0).build();
        Assertions.assertEquals(0.0, exemplar.getValue(), 0.0);
        Assertions.assertEquals(Labels.EMPTY, exemplar.getLabels());
        Assertions.assertFalse(exemplar.hasTimestamp());
    }

    @Test
    void testLabelsMergeTraceId() {
        Exemplar exemplar = Exemplar.builder()
                .value(0.0)
                .labels(Labels.of("a", "b"))
                .traceId("abc")
                .build();
        Assertions.assertEquals(Labels.of("a", "b", "trace_id", "abc"), exemplar.getLabels());
    }

    @Test
    void testLabelsMergeSpanId() {
        Exemplar exemplar = Exemplar.builder()
                .value(0.0)
                .labels(Labels.of("a", "b"))
                .spanId("abc")
                .build();
        Assertions.assertEquals(Labels.of("a", "b", "span_id", "abc"), exemplar.getLabels());
    }

    @Test
    void testLabelsMergeTraceIdAndSpanId() {
        Exemplar exemplar = Exemplar.builder()
                .value(0.0)
                .labels(Labels.of("a", "b"))
                .spanId("abc")
                .traceId("def")
                .build();
        Assertions.assertEquals(Labels.of("span_id", "abc", "a", "b", "trace_id", "def"), exemplar.getLabels());
    }

    @Test
    void testLabelsMergeNone() {
        Exemplar exemplar = Exemplar.builder()
                .value(0.0)
                .labels(Labels.of("a", "b"))
                .build();
        Assertions.assertEquals(Labels.of("a", "b"), exemplar.getLabels());
    }
}
