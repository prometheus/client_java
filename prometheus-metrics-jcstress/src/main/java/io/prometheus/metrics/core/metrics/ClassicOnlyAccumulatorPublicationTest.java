package io.prometheus.metrics.core.metrics;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

@JCStressTest
@Outcome(id = "1, 1", expect = Expect.ACCEPTABLE, desc = "A completed observation is published to a subsequent snapshot.")
@State
public class ClassicOnlyAccumulatorPublicationTest {
  private final ClassicOnlyAccumulator accumulator = new ClassicOnlyAccumulator(1);
  private volatile boolean observed;

  @Actor
  public void observe() {
    accumulator.observe(0, 1.0);
    observed = true;
  }

  @Actor
  public void snapshot(II_Result result) {
    while (!observed) {
      Thread.yield();
    }
    result.r1 = accumulator.snapshot().count > 0 ? 1 : 0;
  }

  @Arbiter
  public void finalSnapshot(II_Result result) {
    accumulator.snapshot();
    result.r2 = accumulator.snapshot().count > 0 ? 1 : 0;
  }
}
