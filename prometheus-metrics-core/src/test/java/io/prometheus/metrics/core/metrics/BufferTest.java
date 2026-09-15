package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
  void lateAppenderCountedByNextGenerationMustNotBeBufferedAgain() throws Exception {
    assertLateAppenderHandoff(false);
  }

  @Test
  void lateAppenderHandoffUsesAbsoluteStripeCountsAfterReset() throws Exception {
    assertLateAppenderHandoff(true);
  }

  private static void assertLateAppenderHandoff(boolean reset) throws Exception {
    CountDownLatch firstSnapshotStarted = new CountDownLatch(1);
    CountDownLatch finishFirstSnapshot = new CountDownLatch(1);
    CountDownLatch observationCounted = new CountDownLatch(1);
    CountDownLatch readGeneration = new CountDownLatch(1);
    CountDownLatch secondRunStarted = new CountDownLatch(1);
    AtomicLong completedObservations = new AtomicLong();
    AtomicLong secondExpectedCount = new AtomicLong();
    AtomicBoolean pauseFirstAppender = new AtomicBoolean(true);
    Buffer buffer =
        new Buffer(
            TimeUnit.SECONDS.toNanos(5),
            16,
            () -> {
              if (pauseFirstAppender.compareAndSet(true, false)) {
                observationCounted.countDown();
                awaitLatch(readGeneration);
              }
            });
    if (reset) {
      assertThat(buffer.append(1.0)).isFalse();
      buffer.observeDirect(completedObservations::incrementAndGet);
    }
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      Future<CounterSnapshot.CounterDataPointSnapshot> firstRun =
          executor.submit(
              () ->
                  buffer.run(
                      expectedCount -> completedObservations.get() == expectedCount,
                      () -> {
                        firstSnapshotStarted.countDown();
                        awaitLatch(finishFirstSnapshot);
                        CounterSnapshot.CounterDataPointSnapshot snapshot =
                            new CounterSnapshot.CounterDataPointSnapshot(
                                completedObservations.get(), Labels.EMPTY, null, 0);
                        if (reset) {
                          completedObservations.set(0);
                          buffer.reset();
                        }
                        return snapshot;
                      },
                      ignored -> completedObservations.incrementAndGet()));
      awaitLatch(firstSnapshotStarted);

      // Increment while generation A is active, but do not read activeGeneration yet.
      Future<Boolean> appender =
          executor.submit(
              () -> {
                boolean appended = buffer.append(1.0);
                if (!appended) {
                  buffer.observeDirect(completedObservations::incrementAndGet);
                }
                return appended;
              });
      awaitLatch(observationCounted);
      finishFirstSnapshot.countDown();
      assertThat(firstRun.get(10, TimeUnit.SECONDS).getValue()).isEqualTo(reset ? 1 : 0);

      Future<CounterSnapshot.CounterDataPointSnapshot> secondRun =
          executor.submit(
              () ->
                  buffer.run(
                      expectedCount -> {
                        secondExpectedCount.set(expectedCount);
                        secondRunStarted.countDown();
                        return completedObservations.get() == expectedCount;
                      },
                      () ->
                          new CounterSnapshot.CounterDataPointSnapshot(
                              completedObservations.get(), Labels.EMPTY, null, 0),
                      ignored -> completedObservations.incrementAndGet()));
      awaitLatch(secondRunStarted);
      assertThat(secondExpectedCount).hasValue(1);
      // An observation arriving after B's activation still belongs in B's buffer. It must not
      // appear in B's snapshot and must be replayed exactly once before the following collection.
      assertThat(buffer.append(1.0)).isTrue();

      // B includes the paused observation in expectedCount. Buffering it in B would make B wait
      // until its own timeout/replay; it must instead complete via the direct observation path.
      readGeneration.countDown();
      assertThat(secondRun.get(10, TimeUnit.SECONDS).getValue()).isEqualTo(1);
      assertThat(appender.get(10, TimeUnit.SECONDS)).isFalse();
      assertThat(completedObservations).hasValue(2);
      assertThat(
              buffer
                  .run(
                      expectedCount -> completedObservations.get() == expectedCount,
                      () ->
                          new CounterSnapshot.CounterDataPointSnapshot(
                              completedObservations.get(), Labels.EMPTY, null, 0),
                      ignored -> completedObservations.incrementAndGet())
                  .getValue())
          .isEqualTo(2);
    } finally {
      finishFirstSnapshot.countDown();
      readGeneration.countDown();
      executor.shutdownNow();
      assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
    }
  }

  private static void awaitLatch(CountDownLatch latch) {
    try {
      assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }
}
