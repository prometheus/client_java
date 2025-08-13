package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.escapeMetricSnapshot;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.getSnapshotLabelName;
import static org.assertj.core.api.Assertions.assertThat;

import io.prometheus.metrics.config.EscapingScheme;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

  @ParameterizedTest
  @MethodSource("emptySnapshots")
  void escape(MetricSnapshot original) {
    assertThat(original)
        .isSameAs(escapeMetricSnapshot(original, EscapingScheme.NO_ESCAPING))
        .isSameAs(escapeMetricSnapshot(original, EscapingScheme.UNDERSCORE_ESCAPING));
    assertThat(escapeMetricSnapshot(original, EscapingScheme.VALUE_ENCODING_ESCAPING))
        .usingRecursiveComparison()
        .isEqualTo(original);
  }

  @Test
  void escapeNull() {
    assertThat(escapeMetricSnapshot(null, EscapingScheme.NO_ESCAPING)).isNull();
  }

  public static Stream<Arguments> emptySnapshots() {
    return Stream.of(
        Arguments.of(
            CounterSnapshot.builder()
                .name("empty")
                .dataPoint(CounterSnapshot.CounterDataPointSnapshot.builder().value(0).build())
                .build()),
        Arguments.of(
            GaugeSnapshot.builder()
                .name("empty")
                .dataPoint(GaugeSnapshot.GaugeDataPointSnapshot.builder().value(0).build())
                .build()),
        Arguments.of(
            SummarySnapshot.builder()
                .name("empty")
                .dataPoint(
                    SummarySnapshot.SummaryDataPointSnapshot.builder().count(0).sum(0.0).build())
                .build()),
        Arguments.of(
            HistogramSnapshot.builder()
                .name("empty")
                .dataPoint(
                    HistogramSnapshot.HistogramDataPointSnapshot.builder()
                        .count(0)
                        .sum(0.0)
                        .classicHistogramBuckets(
                            ClassicHistogramBuckets.builder()
                                .bucket(0.0, 0)
                                .bucket(1.0, 0)
                                .bucket(2.0, 0)
                                .bucket(Double.POSITIVE_INFINITY, 0)
                                .build())
                        .exemplars(
                            Exemplars.builder()
                                .exemplar(
                                    Exemplar.builder()
                                        .labels(Labels.of("exemplar_label", "exemplar_value"))
                                        .value(0.0)
                                        .build())
                                .build())
                        .build())
                .build()),
        Arguments.of(
            StateSetSnapshot.builder()
                .name("empty")
                .dataPoint(
                    StateSetSnapshot.StateSetDataPointSnapshot.builder().state("foo", true).build())
                .build()),
        Arguments.of(
            UnknownSnapshot.builder()
                .name("empty")
                .dataPoint(UnknownSnapshot.UnknownDataPointSnapshot.builder().value(1.0).build())
                .build()));
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
