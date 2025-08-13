package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.escapeMetricSnapshot;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.escapeName;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.getSnapshotLabelName;
import static io.prometheus.metrics.model.snapshots.SnapshotEscaper.unescapeName;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class SnapshotEscaperTest {

  @ParameterizedTest
  @MethodSource("escapeNameLegacyTestCases")
  public void testEscapeNameLegacy(
      String input, EscapingScheme escapingScheme, String expected, String unescapeExpected) {
    assertEscape(input, escapingScheme, expected, unescapeExpected);
  }

  @ParameterizedTest
  @MethodSource("escapeNameUtf8TestCases")
  public void testEscapeNameUtf8(
      String input, EscapingScheme escapingScheme, String expected, String unescapeExpected) {
    assertEscape(input, escapingScheme, expected, unescapeExpected);
  }

  private static void assertEscape(
      String input, EscapingScheme escapingScheme, String expected, String unescapeExpected) {
    String escaped = escapeName(input, escapingScheme);
    assertThat(escaped).isEqualTo(expected);
    assertThat(unescapeName(escaped, escapingScheme)).isEqualTo(unescapeExpected);
  }

  static Stream<Arguments> escapeNameLegacyTestCases() {
    return Stream.of(
        Arguments.of("", EscapingScheme.UNDERSCORE_ESCAPING, "", ""),
        Arguments.of("", EscapingScheme.DOTS_ESCAPING, "", ""),
        Arguments.of("", EscapingScheme.VALUE_ENCODING_ESCAPING, "", ""),
        Arguments.of(
            "no:escaping_required",
            EscapingScheme.UNDERSCORE_ESCAPING,
            "no:escaping_required",
            "no:escaping_required"),
        // Dots escaping will escape underscores even though it's not strictly
        // necessary for compatibility.
        Arguments.of(
            "no:escaping_required",
            EscapingScheme.DOTS_ESCAPING,
            "no:escaping__required",
            "no:escaping_required"),
        Arguments.of(
            "no:escaping_required",
            EscapingScheme.VALUE_ENCODING_ESCAPING,
            "no:escaping_required",
            "no:escaping_required"),
        Arguments.of(
            "no:escaping_required",
            EscapingScheme.UNDERSCORE_ESCAPING,
            "no:escaping_required",
            "no:escaping_required"),
        Arguments.of(
            "mysystem.prod.west.cpu.load",
            EscapingScheme.DOTS_ESCAPING,
            "mysystem_dot_prod_dot_west_dot_cpu_dot_load",
            "mysystem.prod.west.cpu.load"),
        Arguments.of(
            "mysystem.prod.west.cpu.load_total",
            EscapingScheme.DOTS_ESCAPING,
            "mysystem_dot_prod_dot_west_dot_cpu_dot_load__total",
            "mysystem.prod.west.cpu.load_total"),
        Arguments.of(
            "http.status:sum",
            EscapingScheme.DOTS_ESCAPING,
            "http_dot_status:sum",
            "http.status:sum"),
        Arguments.of(
            "label with 😱", EscapingScheme.UNDERSCORE_ESCAPING, "label_with__", "label_with__"),
        Arguments.of(
            "label with 😱", EscapingScheme.DOTS_ESCAPING, "label__with____", "label_with__"),
        Arguments.of(
            "label with 😱",
            EscapingScheme.VALUE_ENCODING_ESCAPING,
            "U__label_20_with_20__1f631_",
            "label with 😱"),
        // name with unicode characters > 0x100
        Arguments.of("花火", EscapingScheme.UNDERSCORE_ESCAPING, "__", "__"),
        // Dots-replacement does not know the difference between two replaced
        Arguments.of("花火", EscapingScheme.DOTS_ESCAPING, "____", "__"),
        Arguments.of("花火", EscapingScheme.VALUE_ENCODING_ESCAPING, "U___82b1__706b_", "花火"),
        // name with spaces and edge-case value
        Arguments.of(
            "label with Ā", EscapingScheme.UNDERSCORE_ESCAPING, "label_with__", "label_with__"),
        Arguments.of(
            "label with Ā", EscapingScheme.DOTS_ESCAPING, "label__with____", "label_with__"),
        Arguments.of(
            "label with Ā",
            EscapingScheme.VALUE_ENCODING_ESCAPING,
            "U__label_20_with_20__100_",
            "label with Ā"));
  }

  static Stream<Arguments> escapeNameUtf8TestCases() {
    return Stream.of(
        // name with dots - needs UTF-8 validation for escaping to occur
        Arguments.of(
            "mysystem.prod.west.cpu.load",
            EscapingScheme.UNDERSCORE_ESCAPING,
            "mysystem_prod_west_cpu_load",
            "mysystem_prod_west_cpu_load"),
        Arguments.of(
            "mysystem.prod.west.cpu.load",
            EscapingScheme.VALUE_ENCODING_ESCAPING,
            "U__mysystem_2e_prod_2e_west_2e_cpu_2e_load",
            "mysystem.prod.west.cpu.load"),
        Arguments.of(
            "mysystem.prod.west.cpu.load_total",
            EscapingScheme.UNDERSCORE_ESCAPING,
            "mysystem_prod_west_cpu_load_total",
            "mysystem_prod_west_cpu_load_total"),
        Arguments.of(
            "mysystem.prod.west.cpu.load_total",
            EscapingScheme.VALUE_ENCODING_ESCAPING,
            "U__mysystem_2e_prod_2e_west_2e_cpu_2e_load__total",
            "mysystem.prod.west.cpu.load_total"),
        Arguments.of(
            "http.status:sum",
            EscapingScheme.UNDERSCORE_ESCAPING,
            "http_status:sum",
            "http_status:sum"),
        Arguments.of(
            "http.status:sum",
            EscapingScheme.VALUE_ENCODING_ESCAPING,
            "U__http_2e_status:sum",
            "http.status:sum"));
  }

  @ParameterizedTest
  @CsvSource(
      value = {
        // empty string
        "'',''",
        // basic case, no error
        "U__no:unescapingrequired,no:unescapingrequired",
        // capitals ok, no error
        "U__capitals_2E_ok,capitals.ok",
        // underscores, no error
        "U__underscores__doubled__,underscores_doubled_",
        // invalid single underscore
        "U__underscores_doubled_,U__underscores_doubled_",
        // invalid single underscore, 2
        "U__underscores__doubled_,U__underscores__doubled_",
        // giant fake UTF-8 code
        "U__my__hack_2e_attempt_872348732fabdabbab_,U__my__hack_2e_attempt_872348732fabdabbab_",
        // trailing UTF-8
        "U__my__hack_2e,U__my__hack_2e",
        // invalid UTF-8 value
        "U__bad__utf_2eg_,U__bad__utf_2eg_",
        // surrogate UTF-8 value
        "U__bad__utf_D900_,U__bad__utf_D900_",
      })
  public void testValueUnescapeErrors(String escapedName, String expectedUnescapedName) {
    assertThat(unescapeName(escapedName, EscapingScheme.VALUE_ENCODING_ESCAPING))
        .isEqualTo(expectedUnescapedName);
  }

  @Test
  public void testEscapeMetricSnapshotEmpty() {
    MetricSnapshot original = CounterSnapshot.builder().name("empty").build();
    MetricSnapshot got = escapeMetricSnapshot(original, EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(got.getMetadata().getName()).isEqualTo("empty");
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
