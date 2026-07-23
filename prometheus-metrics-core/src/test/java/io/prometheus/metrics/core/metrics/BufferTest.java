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
import java.util.concurrent.atomic.AtomicReference;
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

  @Test
  void stalledAppenderAfterStripeActivationDoesNotBlockRun() throws InterruptedException {
    CountDownLatch started = new CountDownLatch(1);
    CountDownLatch proceed = new CountDownLatch(1);
    CountDownLatch stalled = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    AtomicReference<Boolean> appended = new AtomicReference<>();
    Buffer buffer =
        new Buffer(
            TimeUnit.SECONDS.toNanos(1),
            16,
            () -> {
              stalled.countDown();
              try {
                release.await();
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            });
    Thread runner =
        new Thread(
            () ->
                buffer.run(
                    ignored -> {
                      started.countDown();
                      return proceed.getCount() == 0;
                    },
                    () -> new CounterSnapshot.CounterDataPointSnapshot(0, Labels.EMPTY, null, 0),
                    ignored -> {}));
    runner.start();
    assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
    Thread appender = new Thread(() -> appended.set(buffer.append(1.0)));
    appender.start();
    assertThat(stalled.await(5, TimeUnit.SECONDS)).isTrue();
    proceed.countDown();
    runner.join(5_000);
    assertThat(runner.isAlive()).isFalse();
    release.countDown();
    appender.join(5_000);
    assertThat(appended).hasValue(false);
  }
}
