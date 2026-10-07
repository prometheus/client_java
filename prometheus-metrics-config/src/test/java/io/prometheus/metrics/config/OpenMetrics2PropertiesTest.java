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
                    "io.prometheus.openmetrics2.enabled",
                    "true",
                    "io.prometheus.openmetrics2.content_negotiation",
                    "true",
                    "io.prometheus.openmetrics2.composite_values",
                    "true",
                    "io.prometheus.openmetrics2.exemplar_compliance",
                    "true",
                    "io.prometheus.openmetrics2.native_histograms",
                    "true",
                    "io.prometheus.openmetrics2.suffixes",
                    "false")));
    assertThat(properties.getEnabled()).isTrue();
    assertThat(properties.getContentNegotiation()).isTrue();
    assertThat(properties.getCompositeValues()).isTrue();
    assertThat(properties.getExemplarCompliance()).isTrue();
    assertThat(properties.getNativeHistograms()).isTrue();
    assertThat(properties.getSuffixes()).isFalse();
  }

  @Test
  void loadInvalidValue() {
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () -> load(new HashMap<>(Map.of("io.prometheus.openmetrics2.enabled", "invalid"))))
        .withMessage("io.prometheus.openmetrics2.enabled: Expecting 'true' or 'false'.");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.openmetrics2.content_negotiation", "invalid"))))
        .withMessage(
            "io.prometheus.openmetrics2.content_negotiation: Expecting 'true' or 'false'.");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.openmetrics2.composite_values", "invalid"))))
        .withMessage("io.prometheus.openmetrics2.composite_values: Expecting 'true' or 'false'.");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.openmetrics2.exemplar_compliance", "invalid"))))
        .withMessage(
            "io.prometheus.openmetrics2.exemplar_compliance: Expecting 'true' or 'false'.");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () ->
                load(
                    new HashMap<>(
                        Map.of("io.prometheus.openmetrics2.native_histograms", "invalid"))))
        .withMessage("io.prometheus.openmetrics2.native_histograms: Expecting 'true' or 'false'.");
    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(
            () -> load(new HashMap<>(Map.of("io.prometheus.openmetrics2.suffixes", "invalid"))))
        .withMessage("io.prometheus.openmetrics2.suffixes: Expecting 'true' or 'false'.");
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
            .enabled(true)
            .contentNegotiation(true)
            .compositeValues(false)
            .exemplarCompliance(true)
            .nativeHistograms(false)
            .suffixes(false)
            .build();
    assertThat(properties.getEnabled()).isTrue();
    assertThat(properties.getContentNegotiation()).isTrue();
    assertThat(properties.getCompositeValues()).isFalse();
    assertThat(properties.getExemplarCompliance()).isTrue();
    assertThat(properties.getNativeHistograms()).isFalse();
    assertThat(properties.getSuffixes()).isFalse();
  }

  @Test
  void builderEnableAll() {
    OpenMetrics2Properties properties = OpenMetrics2Properties.builder().enableAll().build();
    assertThat(properties.getEnabled()).isTrue();
    assertThat(properties.getContentNegotiation()).isTrue();
    assertThat(properties.getCompositeValues()).isTrue();
    assertThat(properties.getExemplarCompliance()).isTrue();
    assertThat(properties.getNativeHistograms()).isTrue();
    assertThat(properties.getSuffixes()).isTrue();
  }

  @Test
  void defaultValues() {
    OpenMetrics2Properties properties = OpenMetrics2Properties.builder().build();
    assertThat(properties.getEnabled()).isFalse();
    assertThat(properties.getContentNegotiation()).isFalse();
    assertThat(properties.getCompositeValues()).isFalse();
    assertThat(properties.getExemplarCompliance()).isFalse();
    assertThat(properties.getNativeHistograms()).isFalse();
    assertThat(properties.getSuffixes()).isTrue();
  }

  @Test
  void partialConfiguration() {
    OpenMetrics2Properties properties =
        OpenMetrics2Properties.builder().contentNegotiation(true).compositeValues(true).build();
    assertThat(properties.getContentNegotiation()).isTrue();
    assertThat(properties.getCompositeValues()).isTrue();
    assertThat(properties.getExemplarCompliance()).isFalse();
    assertThat(properties.getNativeHistograms()).isFalse();
    assertThat(properties.getSuffixes()).isTrue();
  }
}
