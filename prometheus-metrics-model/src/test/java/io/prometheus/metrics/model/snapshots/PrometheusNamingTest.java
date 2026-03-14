package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.escapeName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.isValidLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeMetricName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeUnitName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.validateMetricName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.validateUnitName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.prometheus.metrics.config.EscapingScheme;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PrometheusNamingTest {

  @Test
  void testSanitizeMetricName() {
    assertThat(sanitizeMetricName("my_counter_total")).isEqualTo("my_counter_total");
    assertThat(sanitizeMetricName("jvm.info")).isEqualTo("jvm.info");
    assertThat(sanitizeMetricName("jvm_info")).isEqualTo("jvm_info");
    assertThat(sanitizeMetricName("a.b")).isEqualTo("a.b");
    assertThat(sanitizeMetricName("_total")).isEqualTo("_total");
    assertThat(sanitizeMetricName("total")).isEqualTo("total");
    assertThat(sanitizeMetricName("my_events_created")).isEqualTo("my_events_created");
    assertThat(sanitizeMetricName("my_histogram_bucket")).isEqualTo("my_histogram_bucket");
  }

  @Test
  void testSanitizeMetricNameWithUnit() {
    assertThat(prometheusName(sanitizeMetricName("def", Unit.RATIO)))
        .isEqualTo("def_" + Unit.RATIO);
    assertThat(prometheusName(sanitizeMetricName("my_counter_total", Unit.RATIO)))
        .isEqualTo("my_counter_total_" + Unit.RATIO);
    assertThat(sanitizeMetricName("jvm.info", Unit.RATIO)).isEqualTo("jvm.info_" + Unit.RATIO);
    assertThat(sanitizeMetricName("_total", Unit.RATIO)).isEqualTo("_total_" + Unit.RATIO);
    assertThat(sanitizeMetricName("total", Unit.RATIO)).isEqualTo("total_" + Unit.RATIO);
  }

  @Test
  void testSanitizeLabelName() {
    assertThat(prometheusName(sanitizeLabelName("0abc.def"))).isEqualTo("_abc_def");
    assertThat(prometheusName(sanitizeLabelName("_abc"))).isEqualTo("_abc");
    assertThat(prometheusName(sanitizeLabelName("__abc"))).isEqualTo("_abc");
    assertThat(prometheusName(sanitizeLabelName("___abc"))).isEqualTo("_abc");
    assertThat(prometheusName(sanitizeLabelName("_.abc"))).isEqualTo("_abc");
    assertThat(sanitizeLabelName("abc.def")).isEqualTo("abc.def");
    assertThat(sanitizeLabelName("abc.def2")).isEqualTo("abc.def2");
  }

  @Test
  void testValidateUnitName() {
    assertThat(validateUnitName("")).isNotNull();

    assertThat(validateUnitName("seconds")).isNull();
    assertThat(validateUnitName("2")).isNull();
    assertThat(validateUnitName("total")).isNull();
    assertThat(validateUnitName("info")).isNull();
    assertThat(validateUnitName("created")).isNull();
    assertThat(validateUnitName("bucket")).isNull();
  }

  @Test
  void testSanitizeUnitName() {
    assertThat(sanitizeUnitName("seconds")).isEqualTo("seconds");
    assertThat(sanitizeUnitName("m/s")).isEqualTo("m_s");
    assertThat(sanitizeUnitName("2")).isEqualTo("2");
    assertThat(sanitizeUnitName("total")).isEqualTo("total");
    assertThat(sanitizeUnitName("info")).isEqualTo("info");
    assertThat(sanitizeUnitName("created")).isEqualTo("created");
    assertThat(sanitizeUnitName("bucket")).isEqualTo("bucket");
  }

  @Test
  void testInvalidUnitName_percent() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> sanitizeUnitName("%"));
  }

  @Test
  void testEmptyUnitName() {
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
}
