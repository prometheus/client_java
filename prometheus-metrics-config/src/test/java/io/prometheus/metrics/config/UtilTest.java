package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UtilTest {
  @Test
  void loadOptionalDuration_positive() {
    Map<Object, Object> regularProperties = new HashMap<>(Map.of("foo", "5"));
    PropertySource propertySource =
        new PropertySource(new HashMap<>(), new HashMap<>(), regularProperties);

    assertThat(Util.loadOptionalDuration("", "foo", propertySource))
        .isEqualTo(Duration.ofSeconds(5));
  }

  @Test
  void loadOptionalDuration_zero() {
    Map<Object, Object> regularProperties = new HashMap<>(Map.of("foo", "0"));
    PropertySource propertySource =
        new PropertySource(new HashMap<>(), new HashMap<>(), regularProperties);

    assertThat(Util.loadOptionalDuration("", "foo", propertySource)).isNull();
  }

  @Test
  void loadOptionalDuration_missing() {
    Map<Object, Object> regularProperties = new HashMap<>();
    PropertySource propertySource =
        new PropertySource(new HashMap<>(), new HashMap<>(), regularProperties);

    assertThat(Util.loadOptionalDuration("", "foo", propertySource)).isNull();
  }

  @Test
  void loadOptionalDuration_negative_throws() {
    Map<Object, Object> regularProperties = new HashMap<>(Map.of("foo", "-1"));
    PropertySource propertySource =
        new PropertySource(new HashMap<>(), new HashMap<>(), regularProperties);

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> Util.loadOptionalDuration("", "foo", propertySource))
        .withMessage("foo: Expecting value >= 0. Found: -1");
  }

  @Test
  void loadOptionalDuration_invalidNumber_throws() {
    Map<Object, Object> regularProperties = new HashMap<>(Map.of("foo", "abc"));
    PropertySource propertySource =
        new PropertySource(new HashMap<>(), new HashMap<>(), regularProperties);

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> Util.loadOptionalDuration("", "foo", propertySource))
        .withMessage("foo=abc: Expecting long value");
  }
}
