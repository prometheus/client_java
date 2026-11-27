# JCStress tests

This module contains the JCStress coverage for the concurrent metric
accumulators. It is intentionally separate from the regular unit-test
suite because JCStress uses an isolated, generated test harness.

Build the harness and run the accumulator tests with:

```bash
./mvnw -pl prometheus-metrics-jcstress -am package -DskipTests
java -jar prometheus-metrics-jcstress/target/jcstress.jar \
  -t ClassicOnlyAccumulator -iters 10 -f 1
```

The tests cover epoch publication, delayed writers, cell reclamation and
re-registration, and eventual observation visibility. A snapshot may be
intentionally stale while a writer is paused; the tests accept that outcome
but require the observation to become visible after the writer is released.
