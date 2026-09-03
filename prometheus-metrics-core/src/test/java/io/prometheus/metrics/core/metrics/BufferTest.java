package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BufferTest {

  @Test
  void bufferIsDeactivatedWhenCreateResultThrows() {
    Buffer buffer = new Buffer();
    assertThat(buffer.append(1.0)).isFalse();

    assertThatThrownBy(
            () ->
                buffer.run(
                    count -> true,
                    () -> {
                      throw new IllegalStateException("failed to create the snapshot");
                    },
                    value -> {}))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("failed to create the snapshot");

    // The buffer has to be inactive again. While it stayed active, append() kept buffering
    // observations instead of recording them, and the next run() never left its wait loop.
    assertThat(buffer.append(2.0)).isFalse();
  }

  @Test
  void stripeIndexDoesNotOverflowWhenThreadIdNarrowsToIntegerMinValue() {
    assertThat(Buffer.stripeIndex(2_147_483_648L, 3)).isEqualTo(2);
    assertThat(Buffer.stripeIndex(2_147_483_648L, 6)).isEqualTo(2);
    assertThat(Buffer.stripeIndex(2_147_483_648L, 12)).isEqualTo(8);
  }
}
