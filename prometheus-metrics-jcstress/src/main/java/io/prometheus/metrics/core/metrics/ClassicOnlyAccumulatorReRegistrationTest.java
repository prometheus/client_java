package io.prometheus.metrics.core.metrics;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.J_Result;

@JCStressTest
@Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "A reclaimed thread cell is re-registered without losing observations.")
@State
public class ClassicOnlyAccumulatorReRegistrationTest {
  private final ClassicOnlyAccumulator accumulator = new ClassicOnlyAccumulator(1);

  @Actor
  public void observeReclaimAndReuse() {
    accumulator.observe(0, 1.0);
    accumulator.snapshot();
    accumulator.observe(0, 2.0);
  }

  @Actor
  public void concurrentSnapshots() {
    // Keep a second actor so the test still runs through JCStress's actor/arbiter protocol. The
    // reclamation and re-registration happen on one recording thread; concurrent snapshots are
    // covered by the stalled-writer and no-loss tests.
  }

  @Arbiter
  public void collect(J_Result result) {
    accumulator.snapshot();
    accumulator.snapshot();
    result.r1 = accumulator.snapshot().count;
  }
}
