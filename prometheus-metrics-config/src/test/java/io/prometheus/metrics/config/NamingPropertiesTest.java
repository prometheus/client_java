package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class NamingPropertiesTest {
  @Test
  void testBuilder() {
    NamingProperties properties =
        NamingProperties.builder().validation(ValidationScheme.UTF_8_VALIDATION).build();
    assertThat(properties.getValidationScheme()).isEqualTo(ValidationScheme.UTF_8_VALIDATION);
  }

  @Test
  void parseValidationScheme() {
    assertThat(NamingProperties.parseValidationScheme("utf-8"))
        .isEqualTo(ValidationScheme.UTF_8_VALIDATION);
    assertThat(NamingProperties.parseValidationScheme("legacy"))
        .isEqualTo(ValidationScheme.LEGACY_VALIDATION);
    assertThat(NamingProperties.parseValidationScheme(null))
        .isEqualTo(ValidationScheme.LEGACY_VALIDATION);
    assertThatCode(() -> NamingProperties.parseValidationScheme("unknown"))
        .isInstanceOf(PrometheusPropertiesException.class)
        .hasMessageContaining(
            "Unknown validation scheme: unknown. Valid values are: utf-8, legacy.");
  }
}
