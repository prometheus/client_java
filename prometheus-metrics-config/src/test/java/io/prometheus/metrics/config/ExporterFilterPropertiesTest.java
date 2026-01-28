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
                "io.prometheus.exporter.filter.metric_name_must_be_equal_to", "a,b,c",
                "io.prometheus.exporter.filter.metric_name_must_not_be_equal_to", "d,e,f",
                "io.prometheus.exporter.filter.metric_name_must_start_with", "g,h,i",
                "io.prometheus.exporter.filter.metric_name_must_not_start_with", "j,k,l"));
    assertThat(properties.getAllowedMetricNames()).containsExactly("a", "b", "c");
    assertThat(properties.getExcludedMetricNames()).containsExactly("d", "e", "f");
    assertThat(properties.getAllowedMetricNamePrefixes()).containsExactly("g", "h", "i");
    assertThat(properties.getExcludedMetricNamePrefixes()).containsExactly("j", "k", "l");
  }

  private static ExporterFilterProperties load(Map<String, String> map) {
    Map<Object, Object> regularProperties = new HashMap<>(map);
    PropertySource propertySource =
        new PropertySource(new HashMap<>(), new HashMap<>(), regularProperties);
    return ExporterFilterProperties.load(propertySource);
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
