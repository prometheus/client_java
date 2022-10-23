# Client Benchmarks

This module contains microbenchmarks for client instrumentation operations.

## Result Overview

The main outcomes of the benchmarks:
* Simpleclient Counters/Gauges have similar performance to Codahale Counters.
* Codahale Meters are slower than Codahale/Simpleclient Counters
* Codahale Summaries are 10x slower than other metrics.
* Simpleclient Histograms are 10-100X faster than Codahale Summaries.
* Simpleclient `Gauge.Child.set` is relatively slow, especially when done concurrently.
* Label lookups in simpleclient are relatively slow.
* The `System.currentTimeMillis()` call in the `DefaultExemplarSampler` takes about 17ns on Linux.

Accordingly, in terms of client instrumentation performance I suggest the following:
* It's cheap to extensively instrument your code with Simpleclient Counters/Gauges/Summaries without labels, or Codahale Counters.
* Avoid Codahale Meters, in favour of Codahale/Simpleclient Counters and calculating the rate in your monitoring system (e.g. the `rate()` function in Prometheus).
* Use Simpleclient Histograms rather than Codahale Histograms/Timers.
* For high update rate (&gt;1000 per second) prometheus metrics using labels, you should cache the Child. Java 8 may make this better due to an improved ConcurrentHashMap implementation.
* If a use case appears for high update rate use of SimpleClient's `Gauge.Child.set`, we should alter `DoubleAdder` to more efficiently handle this use case.

## Benchmark Results

These benchmarks were run using JMH on a Linux laptop with a 4 Core Intel i7-8550U CPU with OpenJDK 1.8.0_292-b10.

### Counters
    java -jar target/benchmarks.jar CounterBenchmark -wi 5 -i 5 -f 1 -t 1
    i.p.c.b.CounterBenchmark.codahaleCounterIncBenchmark                    avgt        5   8.587 ± 0.530  ns/op
    i.p.c.b.CounterBenchmark.codahaleMeterMarkBenchmark                     avgt        5  40.820 ± 5.550  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterChildIncBenchmark       avgt        5  10.387 ± 0.698  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterIncBenchmark            avgt        5  32.357 ± 1.742  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterNoLabelsIncBenchmark    avgt        5  10.102 ± 0.524  ns/op

    java -jar target/benchmarks.jar CounterBenchmark -wi 5 -i 5 -f 1 -t 2
    i.p.c.b.CounterBenchmark.codahaleCounterIncBenchmark                    avgt        5   8.236 ± 0.409  ns/op
    i.p.c.b.CounterBenchmark.codahaleMeterMarkBenchmark                     avgt        5  57.797 ± 4.758  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterChildIncBenchmark       avgt        5  11.123 ± 2.041  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterIncBenchmark            avgt        5  41.512 ± 5.128  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterNoLabelsIncBenchmark    avgt        5  11.455 ± 0.586  ns/op

    java -jar target/benchmarks.jar CounterBenchmark -wi 5 -i 5 -f 1 -t 4
    i.p.c.b.CounterBenchmark.codahaleCounterIncBenchmark                    avgt        5   9.613 ±  1.024  ns/op
    i.p.c.b.CounterBenchmark.codahaleMeterMarkBenchmark                     avgt        5  90.632 ±  6.027  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterChildIncBenchmark       avgt        5  14.857 ±  0.694  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterIncBenchmark            avgt        5  67.335 ± 15.512  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterNoLabelsIncBenchmark    avgt        5  15.808 ±  1.073  ns/op

### Gauges

Codahale lacks a metric with a `set` method, so we'll compare to `Counter` which has `inc` and `dec`.

    java -jar target/benchmarks.jar GaugeBenchmark -wi 5 -i 5 -f 1 -t 1
    i.p.c.b.GaugeBenchmark.codahaleCounterDecBenchmark                  avgt        5   8.476 ± 0.379  ns/op
    i.p.c.b.GaugeBenchmark.codahaleCounterIncBenchmark                  avgt        5   8.566 ± 0.555  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildDecBenchmark       avgt        5  10.532 ± 0.652  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildIncBenchmark       avgt        5  10.112 ± 0.740  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildSetBenchmark       avgt        5   6.833 ± 0.301  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeDecBenchmark            avgt        5  34.962 ± 2.310  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeIncBenchmark            avgt        5  28.474 ± 1.965  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsDecBenchmark    avgt        5  10.183 ± 0.580  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsIncBenchmark    avgt        5  10.061 ± 0.525  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsSetBenchmark    avgt        5   6.790 ± 0.505  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeSetBenchmark            avgt        5  30.993 ± 1.626  ns/op

    java -jar target/benchmarks.jar GaugeBenchmark -wi 5 -i 5 -f 1 -t 2
    i.p.c.b.GaugeBenchmark.codahaleCounterDecBenchmark                  avgt        5   9.249 ± 0.651  ns/op
    i.p.c.b.GaugeBenchmark.codahaleCounterIncBenchmark                  avgt        5   8.266 ± 1.095  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildDecBenchmark       avgt        5  10.185 ± 0.404  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildIncBenchmark       avgt        5  10.669 ± 0.384  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildSetBenchmark       avgt        5  46.205 ± 3.406  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeDecBenchmark            avgt        5  39.633 ± 1.520  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeIncBenchmark            avgt        5  40.184 ± 3.697  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsDecBenchmark    avgt        5  10.955 ± 0.496  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsIncBenchmark    avgt        5  10.877 ± 0.595  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsSetBenchmark    avgt        5  45.394 ± 3.192  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeSetBenchmark            avgt        5  86.524 ± 2.633  ns/op

    java -jar target/benchmarks.jar GaugeBenchmark -wi 5 -i 5 -f 1 -t 4
    i.p.c.b.GaugeBenchmark.codahaleCounterDecBenchmark                  avgt        5    9.347 ±  0.798  ns/op
    i.p.c.b.GaugeBenchmark.codahaleCounterIncBenchmark                  avgt        5   11.991 ±  0.977  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildDecBenchmark       avgt        5   14.019 ±  0.592  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildIncBenchmark       avgt        5   14.870 ±  0.549  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildSetBenchmark       avgt        5  120.478 ± 16.162  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeDecBenchmark            avgt        5   54.028 ±  7.432  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeIncBenchmark            avgt        5   55.782 ±  7.767  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsDecBenchmark    avgt        5   14.083 ±  0.820  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsIncBenchmark    avgt        5   14.275 ±  0.590  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsSetBenchmark    avgt        5  146.348 ± 11.097  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeSetBenchmark            avgt        5  154.449 ± 10.655  ns/op

### Summaries

The simpleclient `Summary` doesn't have percentiles, simpleclient's `Histogram`
offers a way to calculate percentiles on the server side that works with aggregation.
The closest to the original client's `Summary` is Codahale's
`Timer`, but that includes timing calls so we compare with `Histogram` instead.

    java -jar target/benchmarks.jar SummaryBenchmark -wi 5 -i 5 -f 1 -t 1
    i.p.c.b.SummaryBenchmark.codahaleHistogramBenchmark                    avgt        5  116.902 ± 5.797  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramBenchmark            avgt        5   39.897 ± 1.581  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramChildBenchmark       avgt        5   17.692 ± 1.435  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramNoLabelsBenchmark    avgt        5   17.844 ± 1.213  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryBenchmark              avgt        5   32.840 ± 1.453  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryChildBenchmark         avgt        5   10.862 ± 0.874  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryNoLabelsBenchmark      avgt        5   11.290 ± 0.613  ns/op

    java -jar target/benchmarks.jar SummaryBenchmark -wi 5 -i 5 -f 1 -t 2
    i.p.c.b.SummaryBenchmark.codahaleHistogramBenchmark                    avgt        5  326.477 ± 47.550  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramBenchmark            avgt        5   53.194 ±  3.719  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramChildBenchmark       avgt        5   30.660 ±  2.403  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramNoLabelsBenchmark    avgt        5   30.361 ±  1.727  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryBenchmark              avgt        5   47.051 ±  1.927  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryChildBenchmark         avgt        5   15.596 ±  0.660  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryNoLabelsBenchmark      avgt        5   16.168 ±  1.059  ns/op

    java -jar target/benchmarks.jar SummaryBenchmark -wi 5 -i 5 -f 1 -t 4
    i.p.c.b.SummaryBenchmark.codahaleHistogramBenchmark                    avgt        5  820.989 ± 46.036  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramBenchmark            avgt        5   86.183 ± 11.741  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramChildBenchmark       avgt        5   40.051 ±  1.309  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramNoLabelsBenchmark    avgt        5   41.475 ±  4.742  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryBenchmark              avgt        5   63.493 ±  3.490  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryChildBenchmark         avgt        5   21.829 ±  2.226  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryNoLabelsBenchmark      avgt        5   25.150 ±  2.173  ns/op

Note the high error bars for the original client, it got slower with each iteration
so I suspect a flaw in the test setup.

### Exemplars

    java -jar target/benchmarks.jar ExemplarsBenchmark
    Benchmark                                               Mode  Samples   Score   Error  Units
    i.p.c.b.ExemplarsBenchmark.testCounter                    avgt      200  27.318 ± 0.347  ns/op
    i.p.c.b.ExemplarsBenchmark.testCounterWithExemplars       avgt      200  45.785 ± 0.177  ns/op
    i.p.c.b.ExemplarsBenchmark.testCounterWithoutExemplars    avgt      200  25.404 ± 0.184  ns/op
