package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.escapeMetricSnapshot;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.escapeName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.isValidLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeMetricName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeUnitName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.unescapeName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.validateMetricName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.validateUnitName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class PrometheusNamingTest {

  @Test
  public void testSanitizeMetricName() {
    assertThat(sanitizeMetricName("my_counter_total")).isEqualTo("my_counter");
    assertThat(sanitizeMetricName("jvm.info")).isEqualTo("jvm");
    assertThat(sanitizeMetricName("jvm_info")).isEqualTo("jvm");
    assertThat(sanitizeMetricName("jvm.info")).isEqualTo("jvm");
    assertThat(sanitizeMetricName("a.b")).isEqualTo("a.b");
    assertThat(sanitizeMetricName("_total")).isEqualTo("total");
    assertThat(sanitizeMetricName("total")).isEqualTo("total");
  }

  @Test
  public void testSanitizeMetricNameWithUnit() {
    assertThat(prometheusName(sanitizeMetricName("def", Unit.RATIO)))
        .isEqualTo("def_" + Unit.RATIO);
    assertThat(prometheusName(sanitizeMetricName("my_counter_total", Unit.RATIO)))
        .isEqualTo("my_counter_" + Unit.RATIO);
    assertThat(sanitizeMetricName("jvm.info", Unit.RATIO)).isEqualTo("jvm_" + Unit.RATIO);
    assertThat(sanitizeMetricName("_total", Unit.RATIO)).isEqualTo("total_" + Unit.RATIO);
    assertThat(sanitizeMetricName("total", Unit.RATIO)).isEqualTo("total_" + Unit.RATIO);
  }

  @Test
  public void testSanitizeLabelName() {
    assertThat(prometheusName(sanitizeLabelName("0abc.def"))).isEqualTo("_abc_def");
    assertThat(prometheusName(sanitizeLabelName("_abc"))).isEqualTo("_abc");
    assertThat(prometheusName(sanitizeLabelName("__abc"))).isEqualTo("_abc");
    assertThat(prometheusName(sanitizeLabelName("___abc"))).isEqualTo("_abc");
    assertThat(prometheusName(sanitizeLabelName("_.abc"))).isEqualTo("_abc");
    assertThat(sanitizeLabelName("abc.def")).isEqualTo("abc.def");
    assertThat(sanitizeLabelName("abc.def2")).isEqualTo("abc.def2");
  }

  @Test
  public void testValidateUnitName() {
    assertThat(validateUnitName("secondstotal")).isNotNull();
    assertThat(validateUnitName("total")).isNotNull();
    assertThat(validateUnitName("seconds_total")).isNotNull();
    assertThat(validateUnitName("_total")).isNotNull();
    assertThat(validateUnitName("")).isNotNull();

    assertThat(validateUnitName("seconds")).isNull();
    assertThat(validateUnitName("2")).isNull();
  }

  @Test
  public void testSanitizeUnitName() {
    assertThat(sanitizeUnitName("seconds")).isEqualTo("seconds");
    assertThat(sanitizeUnitName("seconds_total")).isEqualTo("seconds");
    assertThat(sanitizeUnitName("seconds_total_total")).isEqualTo("seconds");
    assertThat(sanitizeUnitName("m/s")).isEqualTo("m_s");
    assertThat(sanitizeUnitName("secondstotal")).isEqualTo("seconds");
    assertThat(sanitizeUnitName("2")).isEqualTo("2");
  }

  @Test
  public void testInvalidUnitName1() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> sanitizeUnitName("total"));
  }

  @Test
  public void testInvalidUnitName2() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> sanitizeUnitName("_total"));
  }

  @Test
  public void testInvalidUnitName3() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> sanitizeUnitName("%"));
  }

  @Test
  public void testEmptyUnitName() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> sanitizeUnitName(""));
  }

  @ParameterizedTest
  @MethodSource("nameIsValid")
  public void testLabelNameIsValidUtf8(
      String labelName, boolean utf8Valid) {
    assertMetricName(labelName, utf8Valid);
    assertLabelName(labelName, utf8Valid);
  }

  private static void assertLabelName(String labelName, boolean legacyValid) {
    assertThat(isValidLabelName(labelName))
        .describedAs("isValidLabelName(%s)", labelName)
        .isEqualTo(legacyValid);
  }

  private static void assertMetricName(String labelName, boolean valid) {
    assertThat(validateMetricName(labelName))
        .describedAs("validateMetricName(%s)", labelName)
        .isEqualTo(valid ? null : "The metric name contains unsupported characters");
  }

  static Stream<Arguments> nameIsValid() {
    return Stream.of(
        Arguments.of("", false),
        Arguments.of("Avalid_23name", true),
        Arguments.of("_Avalid_23name", true),
        Arguments.of("1valid_23name", true),
        Arguments.of("avalid_23name",  true),
        Arguments.of("Ava:lid_23name",  true),
        Arguments.of("a lid_23name", true),
        Arguments.of(":leading_colon",  true),
        Arguments.of("colon:in:the:middle",true),
        Arguments.of("aΩz", true),
        Arguments.of("a\ud800z",  false));
  }

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
        .isEqualTo(
            Labels.builder()
                .label("__name__", expectedName)
                .label(expectedLabelName, expectedLabelValue)
                .build());

    assertThat(original.getMetadata().getName()).isEqualTo(name);
    assertThat(original.getMetadata().getHelp()).isEqualTo("some help text");
    assertThat(original.getDataPoints()).hasSize(1);

    DataPointSnapshot originalData = original.getDataPoints().get(0);
    assertThat((Iterable<? extends Label>) originalData.getLabels())
        .isEqualTo(Labels.builder().label("__name__", name).label(labelName, labelValue).build());
  }

  private MetricSnapshot createTestSnapshot(
      String name,
      String labelName,
      String labelValue,
      Class<? extends MetricSnapshot> snapshotType) {
    Labels labels = Labels.builder().label("__name__", name).label(labelName, labelValue).build();

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
}
