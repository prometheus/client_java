package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OpenMetrics2PropertiesTest {

  @Test
  void load() {
    OpenMetrics2Properties properties =
        load(
            new HashMap<>(
                Map.of(
                    "io.prometheus.open_metrics2.content_negotiation",
                    "true",
                    "io.prometheus.open_metrics2.disable_suffix_appending",
                    "true",
                    "io.prometheus.open_metrics2.composite_values",
                    "true",
                    "io.prometheus.open_metrics2.exemplar_compliance",
                    "true",
                    "io.prometheus.open_metrics2.native_histograms",
                    "true")));
    assertThat(properties.getContentNegotiation()).isTrue();
    assertThat(properties.getDisableSuffixAppending()).isTrue();
    assertThat(properties.getCompositeValues()).isTrue();
    assertThat(properties.getExemplarCompliance()).isTrue();
    assertThat(properties.getNativeHistograms()).isTrue();
  }

  @Test
  void loadInvalidValue() {
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.open_metrics2.content_negotiation", "invalid"))))
        .withMessage(
            "io.prometheus.open_metrics2.content_negotiation: Expecting 'true' or 'false'. Found:"
                + " invalid");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.open_metrics2.disable_suffix_appending", "invalid"))))
        .withMessage(
            "io.prometheus.open_metrics2.disable_suffix_appending: Expecting 'true' or 'false'."
                + " Found: invalid");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.open_metrics2.composite_values", "invalid"))))
        .withMessage(
            "io.prometheus.open_metrics2.composite_values: Expecting 'true' or 'false'. Found:"
                + " invalid");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.open_metrics2.exemplar_compliance", "invalid"))))
        .withMessage(
            "io.prometheus.open_metrics2.exemplar_compliance: Expecting 'true' or 'false'. Found:"
                + " invalid");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.open_metrics2.native_histograms", "invalid"))))
        .withMessage(
            "io.prometheus.open_metrics2.native_histograms: Expecting 'true' or 'false'. Found:"
                + " invalid");
  }

  private static OpenMetrics2Properties load(Map<String, String> map) {
    Map<Object, Object> regularProperties = new HashMap<>(map);
    PropertySource propertySource = new PropertySource(regularProperties);
    return OpenMetrics2Properties.load(propertySource);
  }

  @Test
  void builder() {
    OpenMetrics2Properties properties =
        OpenMetrics2Properties.builder()
            .contentNegotiation(true)
            .disableSuffixAppending(true)
            .compositeValues(false)
            .exemplarCompliance(true)
            .nativeHistograms(false)
            .build();
    assertThat(properties.getContentNegotiation()).isTrue();
    assertThat(properties.getDisableSuffixAppending()).isTrue();
    assertThat(properties.getCompositeValues()).isFalse();
    assertThat(properties.getExemplarCompliance()).isTrue();
    assertThat(properties.getNativeHistograms()).isFalse();
  }

  @Test
  void builderEnableAll() {
    OpenMetrics2Properties properties = OpenMetrics2Properties.builder().enableAll().build();
    assertThat(properties.getContentNegotiation()).isTrue();
    assertThat(properties.getDisableSuffixAppending()).isTrue();
    assertThat(properties.getCompositeValues()).isTrue();
    assertThat(properties.getExemplarCompliance()).isTrue();
    assertThat(properties.getNativeHistograms()).isTrue();
  }

  @Test
  void defaultValues() {
    OpenMetrics2Properties properties = OpenMetrics2Properties.builder().build();
    assertThat(properties.getContentNegotiation()).isFalse();
    assertThat(properties.getDisableSuffixAppending()).isFalse();
    assertThat(properties.getCompositeValues()).isFalse();
    assertThat(properties.getExemplarCompliance()).isFalse();
    assertThat(properties.getNativeHistograms()).isFalse();
  }

  @Test
  void partialConfiguration() {
    OpenMetrics2Properties properties =
        OpenMetrics2Properties.builder().contentNegotiation(true).compositeValues(true).build();
    assertThat(properties.getContentNegotiation()).isTrue();
    assertThat(properties.getDisableSuffixAppending()).isFalse();
    assertThat(properties.getCompositeValues()).isTrue();
    assertThat(properties.getExemplarCompliance()).isFalse();
    assertThat(properties.getNativeHistograms()).isFalse();
  }
}
