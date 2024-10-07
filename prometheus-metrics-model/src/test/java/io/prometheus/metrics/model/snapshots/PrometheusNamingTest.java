package io.prometheus.metrics.model.snapshots;

import static io.prometheus.metrics.model.snapshots.PrometheusNaming.*;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PrometheusNamingTest {

  @Test
  public void testSanitizeMetricName() {
    assertThat(prometheusName(sanitizeMetricName("0abc.def"))).isEqualTo("_abc_def");
    assertThat(prometheusName(sanitizeMetricName("___ab.:c0"))).isEqualTo("___ab__c0");
    assertThat(sanitizeMetricName("my_prefix/my_metric")).isEqualTo("my_prefix_my_metric");
    assertThat(prometheusName(sanitizeMetricName("my_counter_total"))).isEqualTo("my_counter");
    assertThat(sanitizeMetricName("jvm.info")).isEqualTo("jvm");
    assertThat(sanitizeMetricName("jvm_info")).isEqualTo("jvm");
    assertThat(sanitizeMetricName("jvm.info")).isEqualTo("jvm");
    assertThat(sanitizeMetricName("a.b")).isEqualTo("a.b");
    assertThat(sanitizeMetricName("_total")).isEqualTo("total");
    assertThat(sanitizeMetricName("total")).isEqualTo("total");
  }

  @Test
  public void testSanitizeMetricNameWithUnit() {
    assertThat(prometheusName(sanitizeMetricName("0abc.def", Unit.RATIO)))
        .isEqualTo("_abc_def_" + Unit.RATIO);
    assertThat(prometheusName(sanitizeMetricName("___ab.:c0", Unit.RATIO)))
        .isEqualTo("___ab__c0_" + Unit.RATIO);
    assertThat(sanitizeMetricName("my_prefix/my_metric", Unit.RATIO))
        .isEqualTo("my_prefix_my_metric_" + Unit.RATIO);
    assertThat(prometheusName(sanitizeMetricName("my_counter_total", Unit.RATIO)))
        .isEqualTo("my_counter_" + Unit.RATIO);
    assertThat(sanitizeMetricName("jvm.info", Unit.RATIO)).isEqualTo("jvm_" + Unit.RATIO);
    assertThat(sanitizeMetricName("jvm_info", Unit.RATIO)).isEqualTo("jvm_" + Unit.RATIO);
    assertThat(sanitizeMetricName("jvm.info", Unit.RATIO)).isEqualTo("jvm_" + Unit.RATIO);
    assertThat(sanitizeMetricName("a.b", Unit.RATIO)).isEqualTo("a.b_" + Unit.RATIO);
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

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnitName1() {
    sanitizeUnitName("total");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnitName2() {
    sanitizeUnitName("_total");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnitName3() {
    sanitizeUnitName("%");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyUnitName() {
    sanitizeUnitName("");
  }
}
