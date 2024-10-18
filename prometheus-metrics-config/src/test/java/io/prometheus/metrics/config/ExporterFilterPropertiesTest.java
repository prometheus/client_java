package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExporterFilterPropertiesTest {

  @Test
  void load() {
    ExporterFilterProperties properties =
        load(
            Map.of(
                "io.prometheus.exporter.filter.metricNameMustBeEqualTo", "a,b,c",
                "io.prometheus.exporter.filter.metricNameMustNotBeEqualTo", "d,e,f",
                "io.prometheus.exporter.filter.metricNameMustStartWith", "g,h,i",
                "io.prometheus.exporter.filter.metricNameMustNotStartWith", "j,k,l"));
    assertThat(properties.getAllowedMetricNames()).containsExactly("a", "b", "c");
    assertThat(properties.getExcludedMetricNames()).containsExactly("d", "e", "f");
    assertThat(properties.getAllowedMetricNamePrefixes()).containsExactly("g", "h", "i");
    assertThat(properties.getExcludedMetricNamePrefixes()).containsExactly("j", "k", "l");
  }

  private static ExporterFilterProperties load(Map<String, String> map) {
    return ExporterFilterProperties.load(new HashMap<>(map));
  }

  @Test
  void builder() {
    ExporterFilterProperties properties =
        ExporterFilterProperties.builder()
            .allowedNames("a", "b", "c")
            .excludedNames("d", "e", "f")
            .allowedPrefixes("g", "h", "i")
            .excludedPrefixes("j", "k", "l")
            .build();
    assertThat(properties.getAllowedMetricNames()).containsExactly("a", "b", "c");
    assertThat(properties.getExcludedMetricNames()).containsExactly("d", "e", "f");
    assertThat(properties.getAllowedMetricNamePrefixes()).containsExactly("g", "h", "i");
    assertThat(properties.getExcludedMetricNamePrefixes()).containsExactly("j", "k", "l");
  }
}
