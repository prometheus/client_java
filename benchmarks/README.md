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

See Javadoc of the benchmark classes:

- [CounterBenchmark](https://github.com/prometheus/client_java/blob/main/benchmarks/src/main/java/io/prometheus/metrics/benchmarks/CounterBenchmark.java) <!-- editorconfig-checker-disable-line -->
- [HistogramBenchmark](https://github.com/prometheus/client_java/blob/main/benchmarks/src/main/java/io/prometheus/metrics/benchmarks/HistogramBenchmark.java) <!-- editorconfig-checker-disable-line -->
- [TextFormatUtilBenchmark](https://github.com/prometheus/client_java/blob/main/benchmarks/src/main/java/io/prometheus/metrics/benchmarks/TextFormatUtilBenchmark.java) <!-- editorconfig-checker-disable-line -->

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
