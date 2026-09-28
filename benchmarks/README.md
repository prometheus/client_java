# Benchmarks

## How to Run

### Running benchmarks

Run benchmarks and update the results in the Javadoc of the benchmark classes:

```shell
mise run update-benchmarks
```

### Different benchmark configurations

The full benchmark suite takes approximately 2 hours with JMH defaults.
For faster iterations, use these preset configurations:

| Command                       | Duration | Use Case                                 |
| ----------------------------- | -------- | ---------------------------------------- |
| `mise run benchmark:quick`    | ~10 min  | Quick smoke test during development      |
| `mise run benchmark:standard` | ~60 min  | CI/nightly runs with good accuracy       |
| `mise run benchmark:full`     | ~2 hours | Full JMH defaults for release validation |

### Running benchmarks manually

```shell
java -jar ./benchmarks/target/benchmarks.jar
```

Run only one specific benchmark:

```shell
java -jar ./benchmarks/target/benchmarks.jar CounterBenchmark
```

### Custom JMH arguments

You can pass custom JMH arguments:

```shell
# Quick run: 1 fork, 1 warmup iteration, 3 measurement iterations
mise run update-benchmarks -- --jmh-args "-f 1 -wi 1 -i 3"

# Standard CI: 3 forks, 3 warmup iterations, 5 measurement iterations
mise run update-benchmarks -- --jmh-args "-f 3 -wi 3 -i 5"
```

JMH parameter reference:

- `-f N`: Number of forks (JVM restarts)
- `-wi N`: Number of warmup iterations
- `-i N`: Number of measurement iterations
- `-w Ns`: Warmup iteration time (default: 10s)
- `-r Ns`: Measurement iteration time (default: 10s)

## Results

### Pull request benchmarks

The `benchmark` label runs the PR head and base on the same runner for each topic.
PR runs select only client_java counter and histogram methods (`prometheus*`), plus
the exposition benchmarks. OpenTelemetry, Codahale, and legacy simpleclient methods
are excluded from PR runs, but remain available in the full/local and nightly suites.
OpenMetrics exposition remains included: it is a client_java output format.

The `CounterBenchmark.prometheusLabelValuesInc*` methods repeatedly look up an
existing label combination and increment it. The matching
`prometheusCachedLabelValuesInc*` methods increment a cached data point instead.
Both have one-thread and four-thread variants sharing a counter. Each invocation
performs one metric update, so throughput and GC profiler allocation in B/op are
per update, unlike older benchmarks that batch updates in a loop.

Run just the lookup and cached variants with allocation profiling:

```shell
./mvnw -pl benchmarks -am package -DskipTests
java -jar benchmarks/target/benchmarks.jar \
  'CounterBenchmark[.]prometheus(Cached)?LabelValuesInc.*' \
  -f 3 -wi 3 -i 5 -prof gc
```

The PR report shows allocation separately from throughput. Allocation deltas are
descriptive, not statistical verdicts, and require matching base/head configurations.
New benchmarks initially have head-only results. To evaluate a production change,
run the same benchmark source and JVM configuration against both implementations.

See Javadoc of the benchmark classes:

- [CounterBenchmark](https://github.com/prometheus/client_java/blob/main/benchmarks/src/main/java/io/prometheus/metrics/benchmarks/CounterBenchmark.java)
- [HistogramBenchmark](https://github.com/prometheus/client_java/blob/main/benchmarks/src/main/java/io/prometheus/metrics/benchmarks/HistogramBenchmark.java)
- [TextFormatUtilBenchmark](https://github.com/prometheus/client_java/blob/main/benchmarks/src/main/java/io/prometheus/metrics/benchmarks/TextFormatUtilBenchmark.java)

## What Prometheus Java client optimizes for

concurrent updates of metrics in multi-threaded applications.
If your application is single-threaded and uses only one processor core, your application isn't
performance critical anyway.
If your application is designed to use all available processor cores for maximum performance, then
you want a metric library that doesn't slow your
application down.
Prometheus client Java metrics support concurrent updates and scrapes. This shows in benchmarks with
multiple threads recording data in shared
metrics.

## Test the benchmark creation script

To test the benchmark creation script, run:

```shell
python ./.mise/tasks/test_update-benchmarks.py
```

## Archive

The `src/main/archive/` directory contains the old benchmarks from 0.16.0 and earlier. It will be
removed as soon as all benchmarks are ported to the 1.0.0 release.
