package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

import org.assertj.core.api.IterableAssert;
import org.junit.jupiter.api.Test;

class ExemplarTest {

  @Test
  public void testGoodCaseComplete() {
    long timestamp = System.currentTimeMillis();
    Exemplar exemplar =
        Exemplar.builder()
            .value(2.2)
            .traceId("abc123abc123")
            .spanId("def456def456")
            .timestampMillis(timestamp)
            .labels(Labels.of("path", "/", "error", "none"))
            .build();
    assertThat(exemplar.getValue()).isCloseTo(2.2, offset(0.0));
    assertLabels(exemplar.getLabels())
        .isEqualTo(
            Labels.of(
                Exemplar.TRACE_ID,
                "abc123abc123",
                Exemplar.SPAN_ID,
                "def456def456",
                "path",
                "/",
                "error",
                "none"));
    assertThat(exemplar.hasTimestamp()).isTrue();
    assertThat(exemplar.getTimestampMillis()).isEqualTo(timestamp);
  }

  @Test
  public void testValueMissing() {
    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(() -> Exemplar.builder().build());
  }

  @Test
  public void testMinimal() {
    Exemplar exemplar = Exemplar.builder().value(0.0).build();
    assertThat(exemplar.getValue()).isCloseTo(0.0, offset(0.0));
    assertLabels(exemplar.getLabels()).isEqualTo(Labels.EMPTY);
    assertThat(exemplar.hasTimestamp()).isFalse();
  }

  @Test
  public void testLabelsMergeTraceId() {
    Exemplar exemplar =
        Exemplar.builder().value(0.0).labels(Labels.of("a", "b")).traceId("abc").build();
    assertLabels(exemplar.getLabels()).isEqualTo(Labels.of("a", "b", "trace_id", "abc"));
  }

  private static IterableAssert<? extends Label> assertLabels(Labels labels) {
    return assertThat((Iterable<? extends Label>) labels);
  }

  @Test
  public void testLabelsMergeSpanId() {
    Exemplar exemplar =
        Exemplar.builder().value(0.0).labels(Labels.of("a", "b")).spanId("abc").build();
    assertLabels(exemplar.getLabels()).isEqualTo(Labels.of("a", "b", "span_id", "abc"));
  }

  @Test
  public void testLabelsMergeTraceIdAndSpanId() {
    Exemplar exemplar =
        Exemplar.builder()
            .value(0.0)
            .labels(Labels.of("a", "b"))
            .spanId("abc")
            .traceId("def")
            .build();
    assertLabels(exemplar.getLabels())
        .isEqualTo(Labels.of("span_id", "abc", "a", "b", "trace_id", "def"));
  }

  @Test
  public void testLabelsMergeNone() {
    Exemplar exemplar = Exemplar.builder().value(0.0).labels(Labels.of("a", "b")).build();
    assertLabels(exemplar.getLabels()).isEqualTo(Labels.of("a", "b"));
  }
}
