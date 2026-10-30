package io.prometheus.metrics.core.metrics;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.JD_Result;

@JCStressTest
@Outcome(
    id = "2, 3.0",
    expect = Expect.ACCEPTABLE,
    desc = "Concurrent observations are visible with coherent count and sum.")
@State
public class ClassicOnlyAccumulatorNoLossTest {
  private final ClassicOnlyAccumulator accumulator = new ClassicOnlyAccumulator(1);

  @Actor
  public void observeOne() {
    accumulator.observe(0, 1.0);
  }

  @Actor
  public void observeTwo() {
    accumulator.observe(0, 2.0);
  }

  @Arbiter
  public void collect(JD_Result result) {
    accumulator.snapshot();
    ClassicOnlyAccumulator.Snapshot snapshot = accumulator.snapshot();
    result.r1 = snapshot.count;
    result.r2 = snapshot.sum;
  }
}
