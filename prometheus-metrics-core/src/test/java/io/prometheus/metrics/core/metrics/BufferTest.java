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
import java.util.concurrent.atomic.AtomicLong;
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
    List<Double> replayedObservations = new ArrayList<>();
    AtomicBoolean timedOut = new AtomicBoolean(false);

    Thread runner =
        new Thread(
            () -> {
              try {
                buffer.run(
                    expectedCount -> {
                      spinWaitStarted.countDown();
                      return false;
                    },
                    () -> new CounterSnapshot.CounterDataPointSnapshot(0, Labels.EMPTY, null, 0),
                    replayedObservations::add);
              } catch (IllegalStateException expected) {
                timedOut.set(true);
              }
            },
            "buffer-timeout-runner");
    runner.setDaemon(true);
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
  void fullBufferUnblocksAppenderWhenGenerationIsDeactivated() throws InterruptedException {
    CountDownLatch runStarted = new CountDownLatch(1);
    CountDownLatch secondAppenderEntered = new CountDownLatch(1);
    AtomicLong beforeAppendCount = new AtomicLong();
    AtomicReference<Boolean> appended = new AtomicReference<>();
    AtomicBoolean timedOut = new AtomicBoolean();
    Buffer buffer =
        new Buffer(
            TimeUnit.MILLISECONDS.toNanos(250),
            1,
            () -> {
              if (beforeAppendCount.incrementAndGet() == 2) {
                secondAppenderEntered.countDown();
              }
            });
    Thread runner =
        new Thread(
            () -> {
              try {
                buffer.run(
                    ignored -> {
                      runStarted.countDown();
                      return false;
                    },
                    () -> new CounterSnapshot.CounterDataPointSnapshot(0, Labels.EMPTY, null, 0),
                    ignored -> {});
              } catch (IllegalStateException expected) {
                timedOut.set(true);
              }
            },
            "buffer-full-runner");
    runner.setDaemon(true);
    runner.start();
    assertThat(runStarted.await(5, TimeUnit.SECONDS)).isTrue();
    assertThat(buffer.append(1.0)).isTrue();

    Thread appender = new Thread(() -> appended.set(buffer.append(2.0)), "buffer-full-appender");
    appender.setDaemon(true);
    appender.start();
    assertThat(secondAppenderEntered.await(5, TimeUnit.SECONDS)).isTrue();
    runner.join(5_000);
    appender.join(5_000);

    assertThat(timedOut).isTrue();
    assertThat(appender.isAlive()).isFalse();
    assertThat(appended).hasValue(false);
  }

  @Test
  void interruptedAppenderLeavesBoundedBufferWait() throws InterruptedException {
    CountDownLatch runStarted = new CountDownLatch(1);
    CountDownLatch secondAppenderEntered = new CountDownLatch(1);
    AtomicLong beforeAppendCount = new AtomicLong();
    AtomicBoolean interrupted = new AtomicBoolean();
    AtomicReference<Boolean> appended = new AtomicReference<>();
    Buffer buffer =
        new Buffer(
            TimeUnit.SECONDS.toNanos(1),
            1,
            () -> {
              if (beforeAppendCount.incrementAndGet() == 2) {
                secondAppenderEntered.countDown();
              }
            });
    Thread runner =
        new Thread(
            () -> {
              try {
                buffer.run(
                    ignored -> {
                      runStarted.countDown();
                      return false;
                    },
                    () -> new CounterSnapshot.CounterDataPointSnapshot(0, Labels.EMPTY, null, 0),
                    ignored -> {});
              } catch (IllegalStateException expected) {
                // The runner is only used to hold the generation open for this test.
              }
            },
            "buffer-interrupt-runner");
    runner.setDaemon(true);
    runner.start();
    assertThat(runStarted.await(5, TimeUnit.SECONDS)).isTrue();
    assertThat(buffer.append(1.0)).isTrue();

    Thread appender =
        new Thread(
            () -> {
              appended.set(buffer.append(2.0));
              interrupted.set(Thread.currentThread().isInterrupted());
            },
            "buffer-interrupt-appender");
    appender.setDaemon(true);
    appender.start();
    assertThat(secondAppenderEntered.await(5, TimeUnit.SECONDS)).isTrue();
    appender.interrupt();
    appender.join(5_000);
    runner.join(5_000);

    assertThat(appender.isAlive()).isFalse();
    assertThat(appended).hasValue(false);
    assertThat(interrupted).isTrue();
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
                    ignored -> {}),
            "buffer-stalled-runner");
    runner.setDaemon(true);
    runner.start();
    assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();
    Thread appender = new Thread(() -> appended.set(buffer.append(1.0)), "buffer-stalled-appender");
    appender.setDaemon(true);
    appender.start();
    assertThat(stalled.await(5, TimeUnit.SECONDS)).isTrue();
    proceed.countDown();
    runner.join(5_000);
    assertThat(runner.isAlive()).isFalse();
    release.countDown();
    appender.join(5_000);
    assertThat(appended).hasValue(false);
  }

  @Test
  void lateAppenderCannotBeAddedToTheNextGeneration() throws InterruptedException {
    CountDownLatch firstRunStarted = new CountDownLatch(1);
    CountDownLatch firstRunMayFinish = new CountDownLatch(1);
    CountDownLatch stalled = new CountDownLatch(1);
    CountDownLatch release = new CountDownLatch(1);
    CountDownLatch secondRunStarted = new CountDownLatch(1);
    AtomicBoolean appended = new AtomicBoolean();
    AtomicLong completedObservations = new AtomicLong();
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
    Thread firstRun =
        new Thread(
            () ->
                buffer.run(
                    ignored -> {
                      firstRunStarted.countDown();
                      return firstRunMayFinish.getCount() == 0;
                    },
                    () -> new CounterSnapshot.CounterDataPointSnapshot(0, Labels.EMPTY, null, 0),
                    ignored -> {}),
            "buffer-first-runner");
    firstRun.setDaemon(true);
    firstRun.start();
    assertThat(firstRunStarted.await(5, TimeUnit.SECONDS)).isTrue();

    Thread appender =
        new Thread(
            () -> {
              appended.set(buffer.append(1.0));
              if (!appended.get()) {
                buffer.observeDirect(
                    () -> {
                      completedObservations.incrementAndGet();
                      return null;
                    });
              }
            },
            "buffer-late-appender");
    appender.setDaemon(true);
    appender.start();
    assertThat(stalled.await(5, TimeUnit.SECONDS)).isTrue();

    firstRunMayFinish.countDown();
    firstRun.join(5_000);
    assertThat(firstRun.isAlive()).isFalse();

    Thread secondRun =
        new Thread(
            () ->
                buffer.run(
                    expectedCount -> {
                      secondRunStarted.countDown();
                      return completedObservations.get() >= expectedCount;
                    },
                    () -> new CounterSnapshot.CounterDataPointSnapshot(0, Labels.EMPTY, null, 0),
                    ignored -> {}),
            "buffer-second-runner");
    secondRun.setDaemon(true);
    secondRun.start();
    assertThat(secondRunStarted.await(5, TimeUnit.SECONDS)).isTrue();
    release.countDown();
    appender.join(5_000);
    secondRun.join(5_000);

    assertThat(appender.isAlive()).isFalse();
    assertThat(secondRun.isAlive()).isFalse();
    assertThat(appended).isFalse();
    assertThat(completedObservations).hasValue(1);
  }
}
