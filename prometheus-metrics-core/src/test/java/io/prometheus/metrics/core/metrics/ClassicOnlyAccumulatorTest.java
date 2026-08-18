package io.prometheus.metrics.core.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ClassicOnlyAccumulatorTest {

  @Test
  void stalledWriterDoesNotBlockSnapshotAndIsCollectedLater() throws Exception {
    ClassicOnlyAccumulator accumulator = new ClassicOnlyAccumulator(2);
    accumulator.observe(0, 1.0);

    Object cell = onlyCell(accumulator);
    Field writingEpoch = cell.getClass().getDeclaredField("writingEpoch");
    writingEpoch.setAccessible(true);
    writingEpoch.setLong(cell, 0);

    ClassicOnlyAccumulator.Snapshot skipped =
        assertTimeoutPreemptively(Duration.ofMillis(500), accumulator::snapshot);
    assertThat(skipped.count).isZero();

    writingEpoch.setLong(cell, -1);
    // The first post-release snapshot flips to the other buffer. The following one revisits the
    // delayed writer's buffer and must retain its observation.
    accumulator.snapshot();
    ClassicOnlyAccumulator.Snapshot collected = accumulator.snapshot();
    assertThat(collected.count).isEqualTo(1);
    assertThat(collected.sum).isEqualTo(1.0);
  }

  @Test
  void emptyCellsAreReclaimedAndCanBeReused() throws Exception {
    ClassicOnlyAccumulator accumulator = new ClassicOnlyAccumulator(1);
    accumulator.observe(0, 1.0);
    Set<?> cells = cells(accumulator);
    assertThat(cells).hasSize(1);

    accumulator.snapshot();
    assertThat(cells).isEmpty();

    accumulator.observe(0, 2.0);
    assertThat(cells).hasSize(1);
    accumulator.snapshot();
    assertThat(cells).isEmpty();
  }

  @Test
  void concurrentWritersAndSnapshotsPreserveJmmVisibility() throws Exception {
    ClassicOnlyAccumulator accumulator = new ClassicOnlyAccumulator(3);
    int writers = 8;
    int observationsPerWriter = 10_000;
    CountDownLatch start = new CountDownLatch(1);
    ExecutorService executor = Executors.newFixedThreadPool(writers);
    try {
      for (int writer = 0; writer < writers; writer++) {
        int bucket = writer % 3;
        executor.submit(
            () -> {
              start.await();
              for (int i = 0; i < observationsPerWriter; i++) {
                accumulator.observe(bucket, bucket + 1.0);
              }
              return null;
            });
      }
      start.countDown();
      executor.shutdown();
      while (!executor.awaitTermination(10, TimeUnit.MILLISECONDS)) {
        accumulator.snapshot();
      }
    } finally {
      executor.shutdownNow();
    }

    ClassicOnlyAccumulator.Snapshot snapshot = accumulator.snapshot();
    snapshot = accumulator.snapshot();
    assertThat(snapshot.count).isEqualTo(writers * observationsPerWriter);
    assertThat(snapshot.buckets).containsExactly(30_000, 30_000, 20_000);
    assertThat(snapshot.sum).isEqualTo(150_000.0);
  }

  private static Object onlyCell(ClassicOnlyAccumulator accumulator) throws Exception {
    return cells(accumulator).iterator().next();
  }

  @SuppressWarnings("unchecked")
  private static Set<Object> cells(ClassicOnlyAccumulator accumulator) throws Exception {
    Field cells = ClassicOnlyAccumulator.class.getDeclaredField("cells");
    cells.setAccessible(true);
    return (Set<Object>) cells.get(accumulator);
  }
}
