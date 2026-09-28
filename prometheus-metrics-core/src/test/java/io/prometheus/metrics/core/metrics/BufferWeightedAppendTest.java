package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.jupiter.api.Test;

/** Deterministic coverage of the weighted ticket protocol used by batched observations. */
class BufferWeightedAppendTest {

  private static CounterSnapshot.CounterDataPointSnapshot snapshot(long value) {
    return new CounterSnapshot.CounterDataPointSnapshot(value, Labels.EMPTY, null, 0);
  }

  @Test
  void weightedAppendWithoutActiveGenerationIsDirectAndCountedInFull() {
    Buffer buffer = new Buffer();
    AtomicLong completed = new AtomicLong();
    AtomicLong expected = new AtomicLong(-1);
    List<String> replayed = new ArrayList<>();

    assertThat(buffer.append(1.5, 5)).isFalse();
    buffer.observeDirect(() -> completed.addAndGet(5));

    CounterSnapshot.CounterDataPointSnapshot result =
        buffer.run(
            expectedCount -> {
              expected.set(expectedCount);
              return completed.get() == expectedCount;
            },
            () -> snapshot(completed.get()),
            (value, weight) -> replayed.add(value + "x" + weight));

    // The collector expects the full weight, not one ticket per append() call.
    assertThat(expected).hasValue(5);
    assertThat(result.getValue()).isEqualTo(5);
    assertThat(replayed).isEmpty();
  }

  @Test
  void weightedAppendDuringActiveGenerationIsBufferedAndReplayedWithWeight() throws Exception {
    Buffer buffer = new Buffer();
    AtomicLong completed = new AtomicLong();
    List<String> replayed = new ArrayList<>();
    CountDownLatch snapshotStarted = new CountDownLatch(1);
    CountDownLatch finishSnapshot = new CountDownLatch(1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      Future<CounterSnapshot.CounterDataPointSnapshot> run =
          executor.submit(
              () ->
                  buffer.run(
                      expectedCount -> completed.get() == expectedCount,
                      () -> {
                        snapshotStarted.countDown();
                        await(finishSnapshot);
                        return snapshot(completed.get());
                      },
                      (value, weight) -> {
                        completed.addAndGet(weight);
                        replayed.add(value + "x" + weight);
                      }));
      await(snapshotStarted);

      // Generation is active: singles and batches interleave in one generation. The weights array
      // is allocated lazily by the first batch and must back-fill the singles before it.
      assertThat(buffer.append(1.0)).isTrue();
      assertThat(buffer.append(2.0, 3)).isTrue();
      assertThat(buffer.append(3.0)).isTrue();
      assertThat(buffer.append(4.0, 1_000_000_000L)).isTrue();

      finishSnapshot.countDown();
      assertThat(run.get(10, TimeUnit.SECONDS).getValue()).isEqualTo(0);
    } finally {
      finishSnapshot.countDown();
      executor.shutdownNow();
    }
    assertThat(replayed).containsExactly("1.0x1", "2.0x3", "3.0x1", "4.0x1000000000");
    assertThat(completed).hasValue(1_000_000_005L);

    // The replayed batches are now part of the live state; the next collection must not wait.
    CounterSnapshot.CounterDataPointSnapshot next =
        buffer.run(
            expectedCount -> completed.get() == expectedCount,
            () -> snapshot(completed.get()),
            (value, weight) -> completed.addAndGet(weight));
    assertThat(next.getValue()).isEqualTo(1_000_000_005L);
  }

  @Test
  void weightsSurviveGenerationGrowth() throws Exception {
    Buffer buffer = new Buffer();
    AtomicLong completed = new AtomicLong();
    List<Long> replayedWeights = new ArrayList<>();
    CountDownLatch snapshotStarted = new CountDownLatch(1);
    CountDownLatch finishSnapshot = new CountDownLatch(1);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      Future<CounterSnapshot.CounterDataPointSnapshot> run =
          executor.submit(
              () ->
                  buffer.run(
                      expectedCount -> completed.get() == expectedCount,
                      () -> {
                        snapshotStarted.countDown();
                        await(finishSnapshot);
                        return snapshot(completed.get());
                      },
                      (value, weight) -> {
                        completed.addAndGet(weight);
                        replayedWeights.add(weight);
                      }));
      await(snapshotStarted);
      // First entry is a batch, then enough singles to force the arrays to grow past the initial
      // 128 slots (twice), then another batch.
      assertThat(buffer.append(0.5, 42)).isTrue();
      for (int i = 0; i < 300; i++) {
        assertThat(buffer.append(i)).isTrue();
      }
      assertThat(buffer.append(0.25, 7)).isTrue();
      finishSnapshot.countDown();
      run.get(10, TimeUnit.SECONDS);
    } finally {
      finishSnapshot.countDown();
      executor.shutdownNow();
    }
    assertThat(replayedWeights).hasSize(302);
    assertThat(replayedWeights.get(0)).isEqualTo(42);
    assertThat(replayedWeights.subList(1, 301)).containsOnly(1L);
    assertThat(replayedWeights.get(301)).isEqualTo(7);
    assertThat(completed).hasValue(42 + 300 + 7);
  }

  /**
   * A batch takes its tickets while generation A is active, but only reads activeGeneration after A
   * finished and B started. B's expected count includes the whole batch, so the batch must be
   * observed directly, not buffered into B. This is the weighted version of the late-appender
   * handoff in {@link BufferTest}.
   */
  @Test
  void batchTicketsClaimedBeforeActivationAreObservedDirectlyNotBuffered() throws Exception {
    long weight = 7;
    CountDownLatch firstSnapshotStarted = new CountDownLatch(1);
    CountDownLatch finishFirstSnapshot = new CountDownLatch(1);
    CountDownLatch ticketsClaimed = new CountDownLatch(1);
    CountDownLatch readGeneration = new CountDownLatch(1);
    CountDownLatch secondRunStarted = new CountDownLatch(1);
    AtomicLong completed = new AtomicLong();
    AtomicLong secondExpectedCount = new AtomicLong();
    AtomicBoolean pauseAppender = new AtomicBoolean(true);
    Buffer buffer =
        new Buffer(
            TimeUnit.SECONDS.toNanos(5),
            16,
            () -> {
              if (pauseAppender.compareAndSet(true, false)) {
                ticketsClaimed.countDown();
                await(readGeneration);
              }
            });
    ExecutorService executor = Executors.newFixedThreadPool(2);
    try {
      Future<CounterSnapshot.CounterDataPointSnapshot> firstRun =
          executor.submit(
              () ->
                  buffer.run(
                      expectedCount -> completed.get() == expectedCount,
                      () -> {
                        firstSnapshotStarted.countDown();
                        await(finishFirstSnapshot);
                        return snapshot(completed.get());
                      },
                      (value, w) -> completed.addAndGet(w)));
      await(firstSnapshotStarted);

      Future<Boolean> appender =
          executor.submit(
              () -> {
                boolean appended = buffer.append(1.0, weight);
                if (!appended) {
                  buffer.observeDirect(() -> completed.addAndGet(weight));
                }
                return appended;
              });
      await(ticketsClaimed);
      finishFirstSnapshot.countDown();
      assertThat(firstRun.get(10, TimeUnit.SECONDS).getValue()).isEqualTo(0);

      Future<CounterSnapshot.CounterDataPointSnapshot> secondRun =
          executor.submit(
              () ->
                  buffer.run(
                      expectedCount -> {
                        secondExpectedCount.set(expectedCount);
                        secondRunStarted.countDown();
                        return completed.get() == expectedCount;
                      },
                      () -> snapshot(completed.get()),
                      (value, w) -> completed.addAndGet(w)));
      await(secondRunStarted);
      // B counted the whole batch, not a single ticket.
      assertThat(secondExpectedCount).hasValue(weight);

      readGeneration.countDown();
      assertThat(secondRun.get(10, TimeUnit.SECONDS).getValue()).isEqualTo(weight);
      assertThat(appender.get(10, TimeUnit.SECONDS)).isFalse();
      assertThat(completed).hasValue(weight);
    } finally {
      finishFirstSnapshot.countDown();
      readGeneration.countDown();
      executor.shutdownNow();
    }
  }

  private static void await(CountDownLatch latch) {
    try {
      assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
  }
}
