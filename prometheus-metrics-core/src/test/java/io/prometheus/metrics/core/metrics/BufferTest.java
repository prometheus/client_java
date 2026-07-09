package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class BufferTest {

  @Test
  void stripeIndexDoesNotOverflowWhenThreadIdNarrowsToIntegerMinValue() {
    assertThat(Buffer.stripeIndex(2_147_483_648L, 3)).isEqualTo(2);
    assertThat(Buffer.stripeIndex(2_147_483_648L, 6)).isEqualTo(2);
    assertThat(Buffer.stripeIndex(2_147_483_648L, 12)).isEqualTo(8);
  }

  @Test
  void timeoutDeactivatesBufferAndReplaysBufferedObservations() throws InterruptedException {
    Buffer buffer = new Buffer(TimeUnit.SECONDS.toNanos(1));
    CountDownLatch spinWaitStarted = new CountDownLatch(1);
    AtomicBoolean keepWaiting = new AtomicBoolean(true);
    List<Double> replayedObservations = new ArrayList<>();
    AtomicBoolean timedOut = new AtomicBoolean(false);

    Thread runner =
        new Thread(
            () -> {
              try {
                buffer.run(
                    expectedCount -> {
                      spinWaitStarted.countDown();
                      return !keepWaiting.get();
                    },
                    () -> new CounterSnapshot.CounterDataPointSnapshot(0, Labels.EMPTY, null, 0),
                    replayedObservations::add);
              } catch (IllegalStateException expected) {
                timedOut.set(true);
              }
            });
    runner.start();

    assertThat(spinWaitStarted.await(5, TimeUnit.SECONDS)).isTrue();
    assertThat(buffer.append(1.0)).isTrue();
    runner.join(5_000);

    assertThat(timedOut).isTrue();
    assertThat(replayedObservations).containsExactly(1.0);
    assertThat(buffer.append(2.0)).isFalse();
  }

  @Test
  void timeoutDoesNotCreateSnapshot() {
    Buffer buffer = new Buffer(TimeUnit.MILLISECONDS.toNanos(1));

    assertThatExceptionOfType(IllegalStateException.class)
        .isThrownBy(
            () ->
                buffer.run(
                    expectedCount -> false,
                    () -> {
                      throw new AssertionError("snapshot should not be created");
                    },
                    ignored -> {}))
        .withMessage("Timed out while waiting for in-flight observations.");
  }
}
