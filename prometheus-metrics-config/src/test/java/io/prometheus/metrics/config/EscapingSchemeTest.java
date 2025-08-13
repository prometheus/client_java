package io.prometheus.metrics.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

class EscapingSchemeTest {

  @Test
  void forString() {
    assertThat(EscapingScheme.forString("allow-utf-8")).isEqualTo(EscapingScheme.NO_ESCAPING);
    assertThat(EscapingScheme.forString("underscores"))
        .isEqualTo(EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(EscapingScheme.forString("dots")).isEqualTo(EscapingScheme.DOTS_ESCAPING);
    assertThat(EscapingScheme.forString("values"))
        .isEqualTo(EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThatCode(() -> EscapingScheme.forString("unknown"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void fromAcceptHeader() {
    assertThat(EscapingScheme.fromAcceptHeader("application/json; escaping=allow-utf-8"))
        .isEqualTo(EscapingScheme.NO_ESCAPING);
    assertThat(EscapingScheme.fromAcceptHeader("application/json; escaping=underscores"))
        .isEqualTo(EscapingScheme.UNDERSCORE_ESCAPING);
    assertThat(EscapingScheme.fromAcceptHeader("application/json; escaping=dots"))
        .isEqualTo(EscapingScheme.DOTS_ESCAPING);
    assertThat(EscapingScheme.fromAcceptHeader("application/json; escaping=values"))
        .isEqualTo(EscapingScheme.VALUE_ENCODING_ESCAPING);
    assertThat(EscapingScheme.fromAcceptHeader("application/json"))
        .isEqualTo(EscapingScheme.DEFAULT);
  }
}
