package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

class MetricsPropertiesTest {
  @Test
  void builder() {
    assertThat(MetricsProperties.builder().exemplarsEnabled(true).build().getExemplarsEnabled())
        .isTrue();
    assertThat(
            MetricsProperties.builder().histogramNativeOnly(true).build().getHistogramNativeOnly())
        .isTrue();
    assertThat(
            MetricsProperties.builder()
                .histogramClassicOnly(true)
                .build()
                .getHistogramClassicOnly())
        .isTrue();
    assertThat(
            MetricsProperties.builder()
                .histogramClassicUpperBounds(1.0, 2.0)
                .build()
                .getHistogramClassicUpperBounds())
        .containsExactly(1.0, 2.0);

    assertThat(MetricsProperties.builder().summaryQuantiles(0.1, 0.2).build().getSummaryQuantiles())
        .containsExactly(0.1, 0.2);
    assertThat(
            MetricsProperties.builder().summaryMaxAgeSeconds(1L).build().getSummaryMaxAgeSeconds())
        .isOne();
    assertThat(
            MetricsProperties.builder()
                .summaryQuantiles(0.2)
                .summaryQuantileErrors(1.0)
                .build()
                .getSummaryQuantileErrors())
        .containsExactly(1.0);
    assertThat(
            MetricsProperties.builder()
                .summaryNumberOfAgeBuckets(1)
                .build()
                .getSummaryNumberOfAgeBuckets())
        .isOne();
  }

  @Test
  void nativeBuilder() {
    assertThat(
            MetricsProperties.builder()
                .histogramNativeInitialSchema(1)
                .build()
                .getHistogramNativeInitialSchema())
        .isOne();
    assertThat(
            MetricsProperties.builder()
                .histogramNativeMinZeroThreshold(.1)
                .build()
                .getHistogramNativeMinZeroThreshold())
        .isEqualTo(.1);
    assertThat(
            MetricsProperties.builder()
                .histogramNativeMaxZeroThreshold(.1)
                .build()
                .getHistogramNativeMaxZeroThreshold())
        .isEqualTo(.1);
    assertThat(
            MetricsProperties.builder()
                .histogramNativeMaxNumberOfBuckets(1)
                .build()
                .getHistogramNativeMaxNumberOfBuckets())
        .isOne();
    assertThat(
            MetricsProperties.builder()
                .histogramNativeResetDurationSeconds(1L)
                .build()
                .getHistogramNativeResetDurationSeconds())
        .isOne();

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> MetricsProperties.builder().histogramNativeInitialSchema(10).build())
        .withMessage(
            ".histogramNativeInitialSchema: Expecting number between -4 and +8. Found: 10");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> MetricsProperties.builder().histogramNativeMinZeroThreshold(-1.0).build())
        .withMessage(".histogramNativeMinZeroThreshold: Expecting value >= 0. Found: -1.0");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> MetricsProperties.builder().histogramNativeMaxZeroThreshold(-1.0).build())
        .withMessage(".histogramNativeMaxZeroThreshold: Expecting value >= 0. Found: -1.0");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> MetricsProperties.builder().histogramNativeMaxNumberOfBuckets(0).build())
        .withMessage(".histogramNativeMaxNumberOfBuckets: Expecting value > 0. Found: 0");
  }
}
