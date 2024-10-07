package io.prometheus.metrics.instrumentation.dropwizard5.labels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MapperConfigTest {
  @Test
  public void setMatch_WHEN_ExpressionMatchesPattern_AllGood() {
    final MapperConfig mapperConfig = new MapperConfig();
    mapperConfig.setMatch("com.company.meter.*");
    assertThat(mapperConfig.getMatch()).isEqualTo("com.company.meter.*");
  }

  @Test
  public void setMatch_WHEN_ExpressionDoesnNotMatchPattern_ThrowException() {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> new MapperConfig().setMatch("com.company.meter.**.yay"));
  }

  @Test
  public void setLabels_WHEN_ExpressionMatchesPattern_AllGood() {
    final MapperConfig mapperConfig = new MapperConfig();
    final Map<String, String> labels = new HashMap<>();
    labels.put("valid", "${0}");
    mapperConfig.setLabels(labels);
    assertThat(mapperConfig.getLabels()).isEqualTo(labels);
  }

  @Test
  public void setLabels_WHEN_ExpressionDoesnNotMatchPattern_ThrowException() {
    final MapperConfig mapperConfig = new MapperConfig();
    final Map<String, String> labels = new HashMap<>();
    labels.put("valid", "${0}");
    labels.put("not valid", "${0}");
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> mapperConfig.setLabels(labels));
  }

  @Test
  public void toString_WHEN_EmptyConfig_AllGood() {
    final MapperConfig mapperConfig = new MapperConfig();
    assertThat(mapperConfig).hasToString("MapperConfig{match=null, name=null, labels={}}");
  }

  @Test
  public void toString_WHEN_FullyConfigured_AllGood() {
    final MapperConfig mapperConfig = new MapperConfig();
    mapperConfig.setMatch("com.company.meter.*.foo");
    mapperConfig.setName("foo");
    mapperConfig.setLabels(Collections.singletonMap("type", "${0}"));
    assertThat(mapperConfig)
        .hasToString("MapperConfig{match=com.company.meter.*.foo, name=foo, labels={type=${0}}}");
  }
}
