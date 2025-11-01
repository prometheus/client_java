package io.prometheus.metrics.model.snapshots;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
class PrometheusNamingTest {

  @Test
  public void testSanitizeMetricName() {
    assertThat(PrometheusNaming.prometheusName(PrometheusNaming.sanitizeMetricName("0abc.def")))
        .isEqualTo("_abc_def");
    assertThat(PrometheusNaming.prometheusName(PrometheusNaming.sanitizeMetricName("___ab.:c0")))
        .isEqualTo("___ab__c0");
    assertThat(PrometheusNaming.sanitizeMetricName("my_prefix/my_metric"))
        .isEqualTo("my_prefix_my_metric");
    assertThat(
            PrometheusNaming.prometheusName(
                PrometheusNaming.sanitizeMetricName("my_counter_total")))
        .isEqualTo("my_counter");
    assertThat(PrometheusNaming.sanitizeMetricName("jvm.info")).isEqualTo("jvm");
    assertThat(PrometheusNaming.sanitizeMetricName("jvm_info")).isEqualTo("jvm");
    assertThat(PrometheusNaming.sanitizeMetricName("jvm.info")).isEqualTo("jvm");
    assertThat(PrometheusNaming.sanitizeMetricName("a.b")).isEqualTo("a.b");
    assertThat(PrometheusNaming.sanitizeMetricName("_total")).isEqualTo("total");
    assertThat(PrometheusNaming.sanitizeMetricName("total")).isEqualTo("total");
  }

  @Test
  public void testSanitizeMetricNameWithUnit() {
    assertThat(
            PrometheusNaming.prometheusName(
                PrometheusNaming.sanitizeMetricName("0abc.def", Unit.RATIO)))
        .isEqualTo("_abc_def_" + Unit.RATIO);
    assertThat(
            PrometheusNaming.prometheusName(
                PrometheusNaming.sanitizeMetricName("___ab.:c0", Unit.RATIO)))
        .isEqualTo("___ab__c0_" + Unit.RATIO);
    assertThat(PrometheusNaming.sanitizeMetricName("my_prefix/my_metric", Unit.RATIO))
        .isEqualTo("my_prefix_my_metric_" + Unit.RATIO);
    assertThat(
            PrometheusNaming.prometheusName(
                PrometheusNaming.sanitizeMetricName("my_counter_total", Unit.RATIO)))
        .isEqualTo("my_counter_" + Unit.RATIO);
    assertThat(PrometheusNaming.sanitizeMetricName("jvm.info", Unit.RATIO))
        .isEqualTo("jvm_" + Unit.RATIO);
    assertThat(PrometheusNaming.sanitizeMetricName("jvm_info", Unit.RATIO))
        .isEqualTo("jvm_" + Unit.RATIO);
    assertThat(PrometheusNaming.sanitizeMetricName("jvm.info", Unit.RATIO))
        .isEqualTo("jvm_" + Unit.RATIO);
    assertThat(PrometheusNaming.sanitizeMetricName("a.b", Unit.RATIO))
        .isEqualTo("a.b_" + Unit.RATIO);
    assertThat(PrometheusNaming.sanitizeMetricName("_total", Unit.RATIO))
        .isEqualTo("total_" + Unit.RATIO);
    assertThat(PrometheusNaming.sanitizeMetricName("total", Unit.RATIO))
        .isEqualTo("total_" + Unit.RATIO);
  }

  @Test
  public void testSanitizeLabelName() {
    assertThat(PrometheusNaming.prometheusName(PrometheusNaming.sanitizeLabelName("0abc.def")))
        .isEqualTo("_abc_def");
    assertThat(PrometheusNaming.prometheusName(PrometheusNaming.sanitizeLabelName("_abc")))
        .isEqualTo("_abc");
    assertThat(PrometheusNaming.prometheusName(PrometheusNaming.sanitizeLabelName("__abc")))
        .isEqualTo("_abc");
    assertThat(PrometheusNaming.prometheusName(PrometheusNaming.sanitizeLabelName("___abc")))
        .isEqualTo("_abc");
    assertThat(PrometheusNaming.prometheusName(PrometheusNaming.sanitizeLabelName("_.abc")))
        .isEqualTo("_abc");
    assertThat(PrometheusNaming.sanitizeLabelName("abc.def")).isEqualTo("abc.def");
    assertThat(PrometheusNaming.sanitizeLabelName("abc.def2")).isEqualTo("abc.def2");
  }

  @Test
  public void testValidateUnitName() {
    assertThat(PrometheusNaming.validateUnitName("secondstotal")).isNotNull();
    assertThat(PrometheusNaming.validateUnitName("total")).isNotNull();
    assertThat(PrometheusNaming.validateUnitName("seconds_total")).isNotNull();
    assertThat(PrometheusNaming.validateUnitName("_total")).isNotNull();
    assertThat(PrometheusNaming.validateUnitName("")).isNotNull();

    assertThat(PrometheusNaming.validateUnitName("seconds")).isNull();
    assertThat(PrometheusNaming.validateUnitName("2")).isNull();
  }

  @Test
  public void testSanitizeUnitName() {
    assertThat(PrometheusNaming.sanitizeUnitName("seconds")).isEqualTo("seconds");
    assertThat(PrometheusNaming.sanitizeUnitName("seconds_total")).isEqualTo("seconds");
    assertThat(PrometheusNaming.sanitizeUnitName("seconds_total_total")).isEqualTo("seconds");
    assertThat(PrometheusNaming.sanitizeUnitName("m/s")).isEqualTo("m_s");
    assertThat(PrometheusNaming.sanitizeUnitName("secondstotal")).isEqualTo("seconds");
    assertThat(PrometheusNaming.sanitizeUnitName("2")).isEqualTo("2");
  }

  @Test
  public void testInvalidUnitName1() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> PrometheusNaming.sanitizeUnitName("total"));
  }

  @Test
  public void testInvalidUnitName2() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> PrometheusNaming.sanitizeUnitName("_total"));
  }

  @Test
  public void testInvalidUnitName3() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> PrometheusNaming.sanitizeUnitName("%"));
  }

  @Test
  public void testEmptyUnitName() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> PrometheusNaming.sanitizeUnitName(""));
  }

  @Test
  void testValidMetricName() {
    assertThat(PrometheusNaming.isValidMetricName("valid_metric_name")).isTrue();
    assertThat(PrometheusNaming.isValidMetricName("invalid_metric_name_total")).isFalse();
    assertThat(PrometheusNaming.isValidMetricName("0abc.def")).isFalse();
  }

  @Test
  void testValidLabelName() {
    assertThat(PrometheusNaming.isValidLabelName("valid_label_name")).isTrue();
    assertThat(PrometheusNaming.isValidLabelName("0invalid_label_name")).isFalse();
    assertThat(PrometheusNaming.isValidLabelName("invalid-label-name")).isFalse();
  }

  @Test
  void testValidUnitName() {
    assertThat(PrometheusNaming.isValidUnitName("seconds")).isTrue();
    assertThat(PrometheusNaming.isValidUnitName("seconds_total")).isFalse();
    assertThat(PrometheusNaming.isValidUnitName("2")).isTrue();
  }
}
