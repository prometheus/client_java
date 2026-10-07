package io.prometheus.metrics.core.metrics;

import javax.annotation.Nullable;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

@JCStressTest
@Outcome(
    id = "0, 1",
    expect = Expect.ACCEPTABLE,
    desc = "A paused writer is skipped and collected after it resumes.")
@State
public class ClassicOnlyAccumulatorStalledWriterTest {
  private final ClassicOnlyAccumulator accumulator = new ClassicOnlyAccumulator(1);
  private volatile @Nullable Object stalledCell;
  private volatile boolean cellReady;

  @Actor
  public void pauseWriter() {
    stalledCell = ClassicOnlyAccumulatorStressSupport.createStalledCell(accumulator);
    cellReady = true;
  }

  @Actor
  public void snapshot(II_Result result) {
    while (!cellReady) {
      Thread.yield();
    }
    result.r1 = accumulator.snapshot().count > 0 ? 1 : 0;
  }

  @Arbiter
  public void resumeAndCollect(II_Result result) {
    Object cell = stalledCell;
    if (cell == null) {
      throw new AssertionError("Writer did not publish its cell");
    }
    ClassicOnlyAccumulatorStressSupport.releaseStalledCell(cell);
    // The observing actor may have advanced the epoch before creating the stalled cell.
    // Four flips cover both buffer parities and leave the result independent of that race.
    accumulator.snapshot();
    accumulator.snapshot();
    accumulator.snapshot();
    result.r2 = accumulator.snapshot().count > 0 ? 1 : 0;
  }
}
