package io.prometheus.metrics.expositionformats;

import static io.prometheus.metrics.expositionformats.SnapshotEscaper.escapeMetricSnapshot;
import static io.prometheus.metrics.expositionformats.SnapshotEscaper.getSnapshotLabelName;
import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.DataPointSnapshot;
import io.prometheus.metrics.model.snapshots.EscapingScheme;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Label;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricMetadata;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class SnapshotEscaperTest {

  @Test
  public void testEscapeMetricSnapshotEmpty() {
    MetricSnapshot original = CounterSnapshot.builder().name("empty").build();
    MetricSnapshot got = escapeMetricSnapshot(original, EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(Objects.requireNonNull(got).getMetadata().getName()).isEqualTo("empty");
    assertThat(original.getMetadata().getName()).isEqualTo("empty");
  }

  @Test
  public void testEscapeMetricSnapshotSimpleNoEscapingNeeded() {
    testEscapeMetricSnapshot(
        "my_metric",
        "some_label",
        "labelvalue",
        "my_metric",
        "some_label",
        "labelvalue",
        EscapingScheme.VALUE_ENCODING_ESCAPING,
        CounterSnapshot.class);
  }

  @Test
  public void testEscapeMetricSnapshotLabelNameEscapingNeeded() {
    testEscapeMetricSnapshot(
        "my_metric",
        "some.label",
        "labelvalue",
        "my_metric",
        "U__some_2e_label",
        "labelvalue",
        EscapingScheme.VALUE_ENCODING_ESCAPING,
        CounterSnapshot.class);
  }

  @Test
  public void testEscapeMetricSnapshotCounterEscapingNeeded() {
    testEscapeMetricSnapshot(
        "my.metric",
        "some?label",
        "label??value",
        "U__my_2e_metric",
        "U__some_3f_label",
        "label??value",
        EscapingScheme.VALUE_ENCODING_ESCAPING,
        CounterSnapshot.class);
  }

  @Test
  public void testEscapeMetricSnapshotGaugeEscapingNeeded() {
    testEscapeMetricSnapshot(
        "unicode.and.dots.花火",
        "some_label",
        "label??value",
        "unicode_dot_and_dot_dots_dot_____",
        "some__label",
        "label??value",
        EscapingScheme.DOTS_ESCAPING,
        GaugeSnapshot.class);
  }

  private void testEscapeMetricSnapshot(
      String name,
      String labelName,
      String labelValue,
      String expectedName,
      String expectedLabelName,
      String expectedLabelValue,
      EscapingScheme escapingScheme,
      Class<? extends MetricSnapshot> snapshotType) {

    MetricSnapshot original = createTestSnapshot(name, labelName, labelValue, snapshotType);
    MetricSnapshot got = escapeMetricSnapshot(original, escapingScheme);

    assertThat(got.getMetadata().getName()).isEqualTo(expectedName);
    assertThat(got.getMetadata().getHelp()).isEqualTo("some help text");
    assertThat(got.getDataPoints()).hasSize(1);

    DataPointSnapshot escapedData = got.getDataPoints().get(0);
    assertThat((Iterable<? extends Label>) escapedData.getLabels())
        .isEqualTo(Labels.builder().label(expectedLabelName, expectedLabelValue).build());

    assertThat(original.getMetadata().getName()).isEqualTo(name);
    assertThat(original.getMetadata().getHelp()).isEqualTo("some help text");
    assertThat(original.getDataPoints()).hasSize(1);

    DataPointSnapshot originalData = original.getDataPoints().get(0);
    assertThat((Iterable<? extends Label>) originalData.getLabels())
        .isEqualTo(Labels.builder().label(labelName, labelValue).build());
  }

  private MetricSnapshot createTestSnapshot(
      String name,
      String labelName,
      String labelValue,
      Class<? extends MetricSnapshot> snapshotType) {
    Labels labels = Labels.builder().label(labelName, labelValue).build();

    if (snapshotType.equals(CounterSnapshot.class)) {
      return CounterSnapshot.builder()
          .name(name)
          .help("some help text")
          .dataPoint(
              CounterSnapshot.CounterDataPointSnapshot.builder().value(34.2).labels(labels).build())
          .build();
    } else if (snapshotType.equals(GaugeSnapshot.class)) {
      return GaugeSnapshot.builder()
          .name(name)
          .help("some help text")
          .dataPoint(
              GaugeSnapshot.GaugeDataPointSnapshot.builder().value(34.2).labels(labels).build())
          .build();
    }

    throw new IllegalArgumentException("Unsupported snapshot type: " + snapshotType);
  }

  @Test
  void escapeIsNoop() {
    MetricSnapshot original = CounterSnapshot.builder().name("empty").build();
    assertThat(original)
        .isSameAs(escapeMetricSnapshot(original, EscapingScheme.NO_ESCAPING))
        .isSameAs(escapeMetricSnapshot(original, EscapingScheme.UNDERSCORE_ESCAPING));
    assertThat(escapeMetricSnapshot(null, EscapingScheme.NO_ESCAPING)).isNull();
  }

  @Test
  void metadataName() {
    MetricMetadata metadata = new MetricMetadata("test.");
    assertThat(SnapshotEscaper.getMetadataName(metadata, EscapingScheme.NO_ESCAPING))
        .isEqualTo("test.");
    assertThat(SnapshotEscaper.getMetadataName(metadata, EscapingScheme.UNDERSCORE_ESCAPING))
        .isEqualTo("test_");
  }

  @Test
  void snapshotLabelName() {
    Labels labels = Labels.builder().label("test.", "value").build();
    assertThat(getSnapshotLabelName(labels, 0, EscapingScheme.NO_ESCAPING)).isEqualTo("test.");
    assertThat(getSnapshotLabelName(labels, 0, EscapingScheme.UNDERSCORE_ESCAPING))
        .isEqualTo("test_");
  }
}
