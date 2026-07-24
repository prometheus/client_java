package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BufferTest {

  @Test
  void stripeIndexDoesNotOverflowWhenThreadIdNarrowsToIntegerMinValue() {
    assertThat(Buffer.stripeIndex(2_147_483_648L, 3)).isEqualTo(2);
    assertThat(Buffer.stripeIndex(2_147_483_648L, 6)).isEqualTo(2);
    assertThat(Buffer.stripeIndex(2_147_483_648L, 12)).isEqualTo(8);
  }
}
