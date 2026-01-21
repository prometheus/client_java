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
    Map<Object, Object> properties = new HashMap<>();
    properties.put("foo", "5");

    assertThat(Util.loadOptionalDuration("foo", properties)).isEqualTo(Duration.ofSeconds(5));
  }

  @Test
  void loadOptionalDuration_zero() {
    Map<Object, Object> properties = new HashMap<>();
    properties.put("foo", "0");

    assertThat(Util.loadOptionalDuration("foo", properties)).isNull();
  }

  @Test
  void loadOptionalDuration_missing() {
    Map<Object, Object> properties = new HashMap<>();

    assertThat(Util.loadOptionalDuration("foo", properties)).isNull();
  }

  @Test
  void loadOptionalDuration_negative_throws() {
    Map<Object, Object> properties = new HashMap<>();
    properties.put("foo", "-1");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> Util.loadOptionalDuration("foo", properties))
        .withMessage("foo: Expecting value >= 0. Found: -1");
  }

  @Test
  void loadOptionalDuration_invalidNumber_throws() {
    Map<Object, Object> properties = new HashMap<>();
    properties.put("foo", "abc");

    assertThatExceptionOfType(PrometheusPropertiesException.class)
        .isThrownBy(() -> Util.loadOptionalDuration("foo", properties))
        .withMessage("foo=abc: Expecting long value");
  }
}
