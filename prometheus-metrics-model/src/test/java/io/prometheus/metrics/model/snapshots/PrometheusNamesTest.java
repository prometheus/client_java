package io.prometheus.metrics.model.snapshots;

import io.prometheus.metrics.config.EscapingScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.prometheus.metrics.model.snapshots.PrometheusNames.escapeName;
import static io.prometheus.metrics.model.snapshots.PrometheusNames.isValidLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNames.prometheusName;
import static io.prometheus.metrics.model.snapshots.PrometheusNames.sanitizeLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNames.sanitizeMetricName;
import static io.prometheus.metrics.model.snapshots.PrometheusNames.sanitizeUnitName;
import static io.prometheus.metrics.model.snapshots.PrometheusNames.validateMetricName;
import static io.prometheus.metrics.model.snapshots.PrometheusNames.validateUnitName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class PrometheusNamesTest {

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
  public void testLabelNameIsValidUtf8(String labelName, boolean utf8Valid) {
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
        Arguments.of("avalid_23name", true),
        Arguments.of("Ava:lid_23name", true),
        Arguments.of("a lid_23name", true),
        Arguments.of(":leading_colon", true),
        Arguments.of("colon:in:the:middle", true),
        Arguments.of("aΩz", true),
        Arguments.of("a\ud800z", false));
  }

  @ParameterizedTest
  @MethodSource("escapeNameLegacyTestCases")
  public void testEscapeName(String input, EscapingScheme escapingScheme, String expected) {
    assertThat(escapeName(input, escapingScheme)).isEqualTo(expected);
  }

  static Stream<Arguments> escapeNameLegacyTestCases() {
    return Stream.of(
        Arguments.of("", EscapingScheme.UNDERSCORE_ESCAPING, ""),
        Arguments.of("", EscapingScheme.DOTS_ESCAPING, ""),
        Arguments.of("", EscapingScheme.VALUE_ENCODING_ESCAPING, ""),
        Arguments.of(
            "no:escaping_required", EscapingScheme.UNDERSCORE_ESCAPING, "no:escaping_required"),
        // Dots escaping will escape underscores even though it's not strictly
        // necessary for compatibility.
        Arguments.of("no:escaping_required", EscapingScheme.DOTS_ESCAPING, "no:escaping__required"),
        Arguments.of(
            "no:escaping_required", EscapingScheme.VALUE_ENCODING_ESCAPING, "no:escaping_required"),
        Arguments.of(
            "no:escaping_required", EscapingScheme.UNDERSCORE_ESCAPING, "no:escaping_required"),
        Arguments.of(
            "mysystem.prod.west.cpu.load",
            EscapingScheme.DOTS_ESCAPING,
            "mysystem_dot_prod_dot_west_dot_cpu_dot_load"),
        Arguments.of(
            "mysystem.prod.west.cpu.load_total",
            EscapingScheme.DOTS_ESCAPING,
            "mysystem_dot_prod_dot_west_dot_cpu_dot_load__total"),
        Arguments.of("http.status:sum", EscapingScheme.DOTS_ESCAPING, "http_dot_status:sum"),
        Arguments.of("label with 😱", EscapingScheme.UNDERSCORE_ESCAPING, "label_with__"),
        Arguments.of("label with 😱", EscapingScheme.DOTS_ESCAPING, "label__with____"),
        Arguments.of(
            "label with 😱", EscapingScheme.VALUE_ENCODING_ESCAPING, "U__label_20_with_20__1f631_"),
        // name with unicode characters > 0x100
        Arguments.of("花火", EscapingScheme.UNDERSCORE_ESCAPING, "__"),
        // Dots-replacement does not know the difference between two replaced
        Arguments.of("花火", EscapingScheme.DOTS_ESCAPING, "____"),
        Arguments.of("花火", EscapingScheme.VALUE_ENCODING_ESCAPING, "U___82b1__706b_"),
        // name with spaces and edge-case value
        Arguments.of("label with Ā", EscapingScheme.UNDERSCORE_ESCAPING, "label_with__"),
        Arguments.of("label with Ā", EscapingScheme.DOTS_ESCAPING, "label__with____"),
        Arguments.of(
            "label with Ā", EscapingScheme.VALUE_ENCODING_ESCAPING, "U__label_20_with_20__100_"),
        // name with dots - needs UTF-8 validation for escaping to occur
        Arguments.of(
            "mysystem.prod.west.cpu.load",
            EscapingScheme.UNDERSCORE_ESCAPING,
            "mysystem_prod_west_cpu_load"),
        Arguments.of(
            "mysystem.prod.west.cpu.load",
            EscapingScheme.VALUE_ENCODING_ESCAPING,
            "U__mysystem_2e_prod_2e_west_2e_cpu_2e_load"),
        Arguments.of(
            "mysystem.prod.west.cpu.load_total",
            EscapingScheme.UNDERSCORE_ESCAPING,
            "mysystem_prod_west_cpu_load_total"),
        Arguments.of(
            "mysystem.prod.west.cpu.load_total",
            EscapingScheme.VALUE_ENCODING_ESCAPING,
            "U__mysystem_2e_prod_2e_west_2e_cpu_2e_load__total"),
        Arguments.of("http.status:sum", EscapingScheme.UNDERSCORE_ESCAPING, "http_status:sum"),
        Arguments.of(
            "http.status:sum", EscapingScheme.VALUE_ENCODING_ESCAPING, "U__http_2e_status:sum"));
  }

  @Test
  void testValidMetricName() {
    assertThat(PrometheusNames.isValidMetricName("valid_metric_name")).isTrue();
    assertThat(PrometheusNames.isValidMetricName("invalid_metric_name_total")).isFalse();
    assertThat(PrometheusNames.isValidMetricName("0abc.def")).isTrue();
  }
}
