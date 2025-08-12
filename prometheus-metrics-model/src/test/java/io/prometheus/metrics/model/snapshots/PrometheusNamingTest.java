package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.isValidLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.prometheusName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeLabelName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeMetricName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.sanitizeUnitName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.validateMetricName;
import static io.prometheus.metrics.model.snapshots.PrometheusNaming.validateUnitName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
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
}
