# Client Benchmarks

This module contains microbenchmarks for client instrumentation operations.

## Result Overview

The main outcomes of the benchmarks:
* Simpleclient Counters/Gauges have similar performance to Codahale Counters.
* The original client is much slower than the Simpleclient or Codahale, especially when used concurrently.
* Codahale Meters are slower than Codahale/Simpleclient Counters
* Codahale and original client Summaries are 10x slower than other metrics.
* Simpleclient `Gauge.Child.set` is relatively slow, especially when done concurrently.
* Label lookups in both Prometheus clients is relatively slow.

Accordingly, in terms of client instrumentation performance I suggest the following:
* It's cheap to extensively instrument your code with Simpleclient Counters/Gauges/Summaries without labels, or Codahale Counters.
* Avoid Codahale Meters, in favour of Codahale/Simpleclient Counters and calculating the rate in your monitoring system (e.g. the `rate()` function in Prometheus).
* Avoid original client Summaries and Codahale Histograms/Timers on high update rate code paths.
* Avoid the original client.
* For high update rate (&gt;1000 per second) prometheus metrics using labels, you should cache the Child. Java 8 may make this better due to an improved ConcurrentHashMap implementation.
* If a use case appears for high update rate use of SimpleClient's `Gauge.Child.set`, we should alter `DoubleAdder` to more efficiently handle this use case.

## Benchmark Results

These benchmarks were run using JMH on a 2-core MacBook Pro with a 2.5GHz i5 processor, 
with Oracle Java 64 1.7.0\_51.

### Counters
    java -jar target/benchmarks.jar CounterBenchmark -wi 5 -i 5 -f 1 -t 1
    i.p.b.CounterBenchmark.codahaleCounterIncBenchmark                    avgt        5   11.554 ± 0.251  ns/op
    i.p.b.CounterBenchmark.codahaleMeterMarkBenchmark                     avgt        5   75.305 ± 7.147  ns/op
    i.p.b.CounterBenchmark.prometheusCounterChildIncBenchmark             avgt        5   13.249 ± 0.029  ns/op
    i.p.b.CounterBenchmark.prometheusCounterIncBenchmark                  avgt        5  127.397 ± 4.072  ns/op
    i.p.b.CounterBenchmark.prometheusSimpleCounterChildIncBenchmark       avgt        5   12.989 ± 0.285  ns/op
    i.p.b.CounterBenchmark.prometheusSimpleCounterIncBenchmark            avgt        5   54.822 ± 7.994  ns/op
    i.p.b.CounterBenchmark.prometheusSimpleCounterNoLabelsIncBenchmark    avgt        5   13.131 ± 1.661  ns/op

    java -jar target/benchmarks.jar CounterBenchmark -wi 5 -i 5 -f 1 -t 2
    i.p.b.CounterBenchmark.codahaleCounterIncBenchmark                    avgt        5   16.707 ±  2.116  ns/op
    i.p.b.CounterBenchmark.codahaleMeterMarkBenchmark                     avgt        5  107.346 ± 23.127  ns/op
    i.p.b.CounterBenchmark.prometheusCounterChildIncBenchmark             avgt        5   41.912 ± 18.167  ns/op
    i.p.b.CounterBenchmark.prometheusCounterIncBenchmark                  avgt        5  170.860 ±  5.110  ns/op
    i.p.b.CounterBenchmark.prometheusSimpleCounterChildIncBenchmark       avgt        5   17.782 ±  2.764  ns/op
    i.p.b.CounterBenchmark.prometheusSimpleCounterIncBenchmark            avgt        5   89.656 ±  4.577  ns/op
    i.p.b.CounterBenchmark.prometheusSimpleCounterNoLabelsIncBenchmark    avgt        5   16.109 ±  1.723  ns/op

    java -jar target/benchmarks.jar CounterBenchmark -wi 5 -i 5 -f 1 -t 4
    i.p.b.CounterBenchmark.codahaleCounterIncBenchmark                    avgt        5   17.628 ±  0.501  ns/op
    i.p.b.CounterBenchmark.codahaleMeterMarkBenchmark                     avgt        5  121.836 ± 15.888  ns/op
    i.p.b.CounterBenchmark.prometheusCounterChildIncBenchmark             avgt        5  377.916 ±  7.965  ns/op
    i.p.b.CounterBenchmark.prometheusCounterIncBenchmark                  avgt        5  250.919 ±  2.728  ns/op
    i.p.b.CounterBenchmark.prometheusSimpleCounterChildIncBenchmark       avgt        5   18.055 ±  1.391  ns/op
    i.p.b.CounterBenchmark.prometheusSimpleCounterIncBenchmark            avgt        5  120.543 ±  1.770  ns/op
    i.p.b.CounterBenchmark.prometheusSimpleCounterNoLabelsIncBenchmark    avgt        5   19.334 ±  1.471  ns/op

### Gauges

Codahale lacks a metric with a `set` method, so we'll compare to `Counter` which has `inc` and `dec`.

    java -jar target/benchmarks.jar GaugeBenchmark -wi 5 -i 5 -f 1 -t 1
    i.p.b.GaugeBenchmark.codahaleCounterDecBenchmark                  avgt        5   11.620 ± 0.288  ns/op
    i.p.b.GaugeBenchmark.codahaleCounterIncBenchmark                  avgt        5   11.718 ± 0.333  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeChildDecBenchmark             avgt        5   13.358 ± 0.554  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeChildIncBenchmark             avgt        5   13.268 ± 0.276  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeChildSetBenchmark             avgt        5   11.624 ± 0.210  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeDecBenchmark                  avgt        5  125.058 ± 2.764  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeIncBenchmark                  avgt        5  127.814 ± 7.741  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeSetBenchmark                  avgt        5  127.899 ± 6.690  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeChildDecBenchmark       avgt        5   12.961 ± 0.393  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeChildIncBenchmark       avgt        5   12.932 ± 0.212  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeChildSetBenchmark       avgt        5   36.672 ± 1.112  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeDecBenchmark            avgt        5   54.677 ± 3.704  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeIncBenchmark            avgt        5   53.278 ± 1.104  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeSetBenchmark            avgt        5   79.724 ± 2.723  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsDecBenchmark    avgt        5   12.957 ± 0.437  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsIncBenchmark    avgt        5   12.932 ± 0.284  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsSetBenchmark    avgt        5   40.235 ± 1.735  ns/op

    java -jar target/benchmarks.jar GaugeBenchmark -wi 5 -i 5 -f 1 -t 2
    i.p.b.GaugeBenchmark.codahaleCounterDecBenchmark                  avgt        5   17.443 ±  4.819  ns/op
    i.p.b.GaugeBenchmark.codahaleCounterIncBenchmark                  avgt        5   14.882 ±  2.875  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeChildDecBenchmark             avgt        5   45.206 ± 29.575  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeChildIncBenchmark             avgt        5   46.657 ± 33.518  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeChildSetBenchmark             avgt        5   21.810 ±  9.370  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeDecBenchmark                  avgt        5  177.370 ±  2.477  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeIncBenchmark                  avgt        5  172.136 ±  3.056  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeSetBenchmark                  avgt        5  186.791 ±  7.996  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeChildDecBenchmark       avgt        5   15.978 ±  2.762  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeChildIncBenchmark       avgt        5   15.457 ±  1.052  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeChildSetBenchmark       avgt        5  156.604 ± 10.953  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeDecBenchmark            avgt        5  107.134 ± 33.620  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeIncBenchmark            avgt        5   89.362 ± 16.608  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeSetBenchmark            avgt        5  163.823 ± 25.270  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsDecBenchmark    avgt        5   16.380 ±  1.915  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsIncBenchmark    avgt        5   17.042 ±  1.113  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsSetBenchmark    avgt        5  164.930 ±  2.565  ns/op

    java -jar target/benchmarks.jar GaugeBenchmark -wi 5 -i 5 -f 1 -t 4
    i.p.b.GaugeBenchmark.codahaleCounterDecBenchmark                  avgt        5   17.291 ±  1.769  ns/op
    i.p.b.GaugeBenchmark.codahaleCounterIncBenchmark                  avgt        5   17.445 ±  0.709  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeChildDecBenchmark             avgt        5  389.411 ± 13.078  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeChildIncBenchmark             avgt        5  399.549 ± 29.274  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeChildSetBenchmark             avgt        5  123.700 ±  3.894  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeDecBenchmark                  avgt        5  244.741 ± 22.477  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeIncBenchmark                  avgt        5  243.525 ±  6.332  ns/op
    i.p.b.GaugeBenchmark.prometheusGaugeSetBenchmark                  avgt        5  252.363 ±  2.664  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeChildDecBenchmark       avgt        5   18.330 ±  2.673  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeChildIncBenchmark       avgt        5   20.633 ±  1.219  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeChildSetBenchmark       avgt        5  335.455 ±  4.562  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeDecBenchmark            avgt        5  116.432 ±  4.793  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeIncBenchmark            avgt        5  129.390 ±  2.360  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeSetBenchmark            avgt        5  613.186 ± 20.548  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsDecBenchmark    avgt        5   19.765 ±  3.189  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsIncBenchmark    avgt        5   19.589 ±  1.634  ns/op
    i.p.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsSetBenchmark    avgt        5  307.238 ±  1.918  ns/op

### Summaries

The simpleclient `Summary` doesn't have percentiles, so it's not an apples to
apples comparison. The closest to the original client's `Summary` is Codahale's 
`Timer`, but that includes timing calls so we compare with `Histogram` instead.

    java -jar target/benchmarks.jar SummaryBenchmark -wi 5 -i 5 -f 1 -t 1
    i.p.b.SummaryBenchmark.codahaleHistogramBenchmark                  avgt        5   187.192 ±  12.304  ns/op
    i.p.b.SummaryBenchmark.prometheusSimpleSummaryBenchmark            avgt        5    61.140 ±   1.298  ns/op
    i.p.b.SummaryBenchmark.prometheusSimpleSummaryChildBenchmark       avgt        5    15.322 ±   0.266  ns/op
    i.p.b.SummaryBenchmark.prometheusSimpleSummaryNoLabelsBenchmark    avgt        5    15.750 ±   0.476  ns/op
    i.p.b.SummaryBenchmark.prometheusSummaryBenchmark                  avgt        5   988.597 ± 343.221  ns/op
    i.p.b.SummaryBenchmark.prometheusSummaryChildBenchmark             avgt        5  1165.270 ± 916.324  ns/op

    java -jar target/benchmarks.jar SummaryBenchmark -wi 5 -i 5 -f 1 -t 2
    i.p.b.SummaryBenchmark.codahaleHistogramBenchmark                  avgt        5   243.749 ±   37.080  ns/op
    i.p.b.SummaryBenchmark.prometheusSimpleSummaryBenchmark            avgt        5   119.753 ±   15.056  ns/op
    i.p.b.SummaryBenchmark.prometheusSimpleSummaryChildBenchmark       avgt        5    32.614 ±   15.976  ns/op
    i.p.b.SummaryBenchmark.prometheusSimpleSummaryNoLabelsBenchmark    avgt        5    32.627 ±    8.943  ns/op
    i.p.b.SummaryBenchmark.prometheusSummaryBenchmark                  avgt        5  2021.984 ±  561.545  ns/op
    i.p.b.SummaryBenchmark.prometheusSummaryChildBenchmark             avgt        5  2338.371 ± 1515.886  ns/op

    java -jar target/benchmarks.jar SummaryBenchmark -wi 5 -i 5 -f 1 -t 4
    i.p.b.SummaryBenchmark.codahaleHistogramBenchmark                  avgt        5   559.505 ±    5.169  ns/op
    i.p.b.SummaryBenchmark.prometheusSimpleSummaryBenchmark            avgt        5   137.072 ±   12.044  ns/op
    i.p.b.SummaryBenchmark.prometheusSimpleSummaryChildBenchmark       avgt        5    44.228 ±    0.697  ns/op
    i.p.b.SummaryBenchmark.prometheusSimpleSummaryNoLabelsBenchmark    avgt        5    41.223 ±    1.978  ns/op
    i.p.b.SummaryBenchmark.prometheusSummaryBenchmark                  avgt        5  4023.354 ± 1122.317  ns/op
    i.p.b.SummaryBenchmark.prometheusSummaryChildBenchmark             avgt        5  4606.571 ± 3392.565  ns/op

Note the high error bars for the original client, it got slower with each iteration
so I suspect a flaw in the test setup.


