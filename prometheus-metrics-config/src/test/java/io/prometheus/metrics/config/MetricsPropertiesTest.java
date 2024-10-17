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

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> MetricsProperties.builder().summaryNumberOfAgeBuckets(0).build())
        .withMessage(".summaryNumberOfAgeBuckets: Expecting value > 0. Found: 0");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> MetricsProperties.builder().summaryQuantiles(2L).build())
        .withMessage(".summaryQuantiles: Expecting 0.0 <= quantile <= 1.0. Found: 2.0");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> MetricsProperties.builder().summaryQuantileErrors(0.9).build())
        .withMessage(
            ".summaryQuantileErrors: Can't configure summaryQuantileErrors without configuring summaryQuantiles");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                MetricsProperties.builder()
                    .summaryQuantiles(0.1)
                    .summaryQuantileErrors(0.1, 0.9)
                    .build())
        .withMessage(".summaryQuantileErrors: must have the same length as summaryQuantiles");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                MetricsProperties.builder()
                    .summaryQuantiles(0.1)
                    .summaryQuantileErrors(-0.9)
                    .build())
        .withMessage(".summaryQuantileErrors: Expecting 0.0 <= error <= 1.0");
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
        .isThrownBy(() -> MetricsProperties.builder().histogramNativeMaxNumberOfBuckets(-1).build())
        .withMessage(".histogramNativeMaxNumberOfBuckets: Expecting value >= 0. Found: -1");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () -> MetricsProperties.builder().histogramNativeResetDurationSeconds(-1L).build())
        .withMessage(".histogramNativeResetDurationSeconds: Expecting value >= 0. Found: -1");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                MetricsProperties.builder()
                    .histogramNativeOnly(true)
                    .histogramClassicOnly(true)
                    .build())
        .withMessage(".histogramNativeOnly and .histogramClassicOnly cannot both be true");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                MetricsProperties.builder()
                    .histogramNativeMinZeroThreshold(0.1)
                    .histogramNativeMaxZeroThreshold(0.01)
                    .build())
        .withMessage(
            ".histogramNativeMinZeroThreshold cannot be greater than .histogramNativeMaxZeroThreshold");
  }
}
