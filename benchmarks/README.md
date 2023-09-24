Benchmarks
----------

## How to Run

```
java -jar ./benchmarks/target/benchmarks.jar
```

Run only one specific benchmark:

```
java -jar ./benchmarks/target/benchmarks.jar CounterBenchmark
```

## Results

See Javadoc of the benchmark classes:

* [CounterBenchmark](https://github.com/prometheus/client_java/blob/1.0.x/benchmarks/src/main/java/io/prometheus/metrics/benchmarks/CounterBenchmark.java)
* [HistogramBenchmark](https://github.com/prometheus/client_java/blob/1.0.x/benchmarks/src/main/java/io/prometheus/metrics/benchmarks/HistogramBenchmark.java)

## What Prometheus Java client optimizes for

concurrent updates of metrics in multi-threaded applications.
If your application is single-threaded and uses only one processor core, your application isn't performance critical anyway.
If your application is designed to use all available processor cores for maximum performance, then you want a metric library that doesn't slow your application down.
Prometheus client Java metrics support concurrent updates and scrapes. This shows in benchmarks with multiple threads recording data in shared metrics.

## Archive

The `src/main/archive/` directory contains the old benchmarks from 0.16.0 and earlier. It will be removed as soon as all benchmarks are ported to the 1.0.0 release.
