# Client Benchmarks

This module contains microbenchmarks for client instrumentation operations.

## Result Overview

The main outcomes of the benchmarks:
* Simpleclient Counters/Gauges have similar performance to Codahale Counters.
* The original client is much slower than the Simpleclient or Codahale, especially when used concurrently.
* Codahale Meters are slower than Codahale/Simpleclient Counters
* Codahale and original client Summaries are 10x slower than other metrics.
* Simpleclient Histograms are 10-100X faster than Codahale and original client Summaries.
* Simpleclient `Gauge.Child.set` is relatively slow, especially when done concurrently.
* Label lookups in both Prometheus clients are relatively slow.
* The `System.currentTimeMillis()` call in the `DefaultExemplarSampler` takes about 17ns on Linux.

Accordingly, in terms of client instrumentation performance I suggest the following:
* It's cheap to extensively instrument your code with Simpleclient Counters/Gauges/Summaries without labels, or Codahale Counters.
* Avoid Codahale Meters, in favour of Codahale/Simpleclient Counters and calculating the rate in your monitoring system (e.g. the `rate()` function in Prometheus).
* Use Simpleclient Histograms rather than original client Summaries and Codahale Histograms/Timers.
* Avoid the original client.
* For high update rate (&gt;1000 per second) prometheus metrics using labels, you should cache the Child. Java 8 may make this better due to an improved ConcurrentHashMap implementation.
* If a use case appears for high update rate use of SimpleClient's `Gauge.Child.set`, we should alter `DoubleAdder` to more efficiently handle this use case.

## Benchmark Results

These benchmarks were run using JMH on a Linux laptop with a 4 Core Intel i7-8550U CPU with OpenJDK 1.8.0_292-b10.

### Counters
    java -jar target/benchmarks.jar CounterBenchmark -wi 5 -i 5 -f 1 -t 1
    i.p.c.b.CounterBenchmark.codahaleCounterIncBenchmark                    avgt        5   8.063 ± 0.440  ns/op
    i.p.c.b.CounterBenchmark.codahaleMeterMarkBenchmark                     avgt        5  38.625 ± 2.111  ns/op
    i.p.c.b.CounterBenchmark.prometheusCounterChildIncBenchmark             avgt        5   9.869 ± 0.252  ns/op
    i.p.c.b.CounterBenchmark.prometheusCounterIncBenchmark                  avgt        5  39.701 ± 1.724  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterChildIncBenchmark       avgt        5   9.915 ± 0.563  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterIncBenchmark            avgt        5  30.765 ± 1.094  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterNoLabelsIncBenchmark    avgt        5   9.715 ± 0.317  ns/op

    java -jar target/benchmarks.jar CounterBenchmark -wi 5 -i 5 -f 1 -t 2
    i.p.c.b.CounterBenchmark.codahaleCounterIncBenchmark                    avgt        5    7.942 ±   0.294  ns/op
    i.p.c.b.CounterBenchmark.codahaleMeterMarkBenchmark                     avgt        5   52.277 ±   1.732  ns/op
    i.p.c.b.CounterBenchmark.prometheusCounterChildIncBenchmark             avgt        5  141.609 ± 195.741  ns/op
    i.p.c.b.CounterBenchmark.prometheusCounterIncBenchmark                  avgt        5   90.233 ±  19.372  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterChildIncBenchmark       avgt        5   10.961 ±   0.844  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterIncBenchmark            avgt        5   43.581 ±   5.921  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterNoLabelsIncBenchmark    avgt        5   10.844 ±   0.193  ns/op

    java -jar target/benchmarks.jar CounterBenchmark -wi 5 -i 5 -f 1 -t 4
    i.p.c.b.CounterBenchmark.codahaleCounterIncBenchmark                    avgt        5    9.617 ±  2.700  ns/op
    i.p.c.b.CounterBenchmark.codahaleMeterMarkBenchmark                     avgt        5   71.150 ± 39.808  ns/op
    i.p.c.b.CounterBenchmark.prometheusCounterChildIncBenchmark             avgt        5  369.650 ±  8.232  ns/op
    i.p.c.b.CounterBenchmark.prometheusCounterIncBenchmark                  avgt        5  401.506 ± 18.896  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterChildIncBenchmark       avgt        5   14.078 ±  0.386  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterIncBenchmark            avgt        5   57.439 ±  5.793  ns/op
    i.p.c.b.CounterBenchmark.prometheusSimpleCounterNoLabelsIncBenchmark    avgt        5   14.173 ±  0.219  ns/op

### Gauges

Codahale lacks a metric with a `set` method, so we'll compare to `Counter` which has `inc` and `dec`.

    java -jar target/benchmarks.jar GaugeBenchmark -wi 5 -i 5 -f 1 -t 1
    i.p.c.b.GaugeBenchmark.codahaleCounterDecBenchmark                  avgt        5   8.062 ± 0.314  ns/op
    i.p.c.b.GaugeBenchmark.codahaleCounterIncBenchmark                  avgt        5   8.154 ± 0.159  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeChildDecBenchmark             avgt        5   9.676 ± 0.763  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeChildIncBenchmark             avgt        5   9.948 ± 0.148  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeChildSetBenchmark             avgt        5   4.695 ± 0.153  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeDecBenchmark                  avgt        5  39.809 ± 2.614  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeIncBenchmark                  avgt        5  39.603 ± 1.681  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeSetBenchmark                  avgt        5  38.823 ± 2.809  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildDecBenchmark       avgt        5  10.088 ± 0.659  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildIncBenchmark       avgt        5   9.702 ± 0.260  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildSetBenchmark       avgt        5   6.503 ± 0.239  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeDecBenchmark            avgt        5  28.471 ± 1.413  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeIncBenchmark            avgt        5  26.624 ± 1.732  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsDecBenchmark    avgt        5   9.677 ± 0.596  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsIncBenchmark    avgt        5   9.614 ± 0.439  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsSetBenchmark    avgt        5   6.562 ± 0.235  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeSetBenchmark            avgt        5  29.216 ± 2.268  ns/op

    java -jar target/benchmarks.jar GaugeBenchmark -wi 5 -i 5 -f 1 -t 2
    i.p.c.b.GaugeBenchmark.codahaleCounterDecBenchmark                  avgt        5    7.947 ±   0.497  ns/op
    i.p.c.b.GaugeBenchmark.codahaleCounterIncBenchmark                  avgt        5    7.957 ±   0.321  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeChildDecBenchmark             avgt        5  149.608 ± 220.760  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeChildIncBenchmark             avgt        5   87.609 ±   5.836  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeChildSetBenchmark             avgt        5   35.629 ±  10.148  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeDecBenchmark                  avgt        5   96.906 ±  23.730  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeIncBenchmark                  avgt        5   95.493 ±   6.731  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeSetBenchmark                  avgt        5   76.563 ±   5.068  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildDecBenchmark       avgt        5   10.438 ±   0.719  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildIncBenchmark       avgt        5   10.359 ±   0.140  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildSetBenchmark       avgt        5   44.248 ±   1.947  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeDecBenchmark            avgt        5   38.577 ±   4.487  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeIncBenchmark            avgt        5   38.113 ±   6.824  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsDecBenchmark    avgt        5   10.509 ±   0.466  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsIncBenchmark    avgt        5   10.449 ±   0.416  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsSetBenchmark    avgt        5   54.079 ±   3.635  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeSetBenchmark            avgt        5   73.451 ±   3.700  ns/op

    java -jar target/benchmarks.jar GaugeBenchmark -wi 5 -i 5 -f 1 -t 4
    i.p.c.b.GaugeBenchmark.codahaleCounterDecBenchmark                  avgt        5    9.102 ±  0.251  ns/op
    i.p.c.b.GaugeBenchmark.codahaleCounterIncBenchmark                  avgt        5   10.050 ±  3.316  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeChildDecBenchmark             avgt        5  350.357 ± 12.734  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeChildIncBenchmark             avgt        5  363.436 ± 37.479  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeChildSetBenchmark             avgt        5   96.036 ±  2.747  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeDecBenchmark                  avgt        5  391.070 ±  9.362  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeIncBenchmark                  avgt        5  401.626 ± 18.952  ns/op
    i.p.c.b.GaugeBenchmark.prometheusGaugeSetBenchmark                  avgt        5  143.673 ±  8.801  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildDecBenchmark       avgt        5   13.550 ±  0.230  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildIncBenchmark       avgt        5   13.405 ±  0.561  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeChildSetBenchmark       avgt        5  118.140 ±  3.803  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeDecBenchmark            avgt        5   52.965 ±  3.219  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeIncBenchmark            avgt        5   50.678 ±  5.462  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsDecBenchmark    avgt        5   13.748 ±  0.507  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsIncBenchmark    avgt        5   13.530 ±  0.414  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeNoLabelsSetBenchmark    avgt        5  128.487 ± 55.744  ns/op
    i.p.c.b.GaugeBenchmark.prometheusSimpleGaugeSetBenchmark            avgt        5  199.036 ± 21.083  ns/op

### Summaries

The simpleclient `Summary` doesn't have percentiles, simpleclient's `Histogram`
offers a way to calculate percentiles on the server side that works with aggregation.
The closest to the original client's `Summary` is Codahale's
`Timer`, but that includes timing calls so we compare with `Histogram` instead.

    java -jar target/benchmarks.jar SummaryBenchmark -wi 5 -i 5 -f 1 -t 1
    i.p.c.b.SummaryBenchmark.codahaleHistogramBenchmark                    avgt        5   111.652 ±    1.521  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramBenchmark            avgt        5    38.672 ±    2.463  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramChildBenchmark       avgt        5    16.992 ±    1.029  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramNoLabelsBenchmark    avgt        5    17.281 ±    0.649  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryBenchmark              avgt        5    31.800 ±    1.357  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryChildBenchmark         avgt        5    10.520 ±    0.251  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryNoLabelsBenchmark      avgt        5    11.004 ±    0.367  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSummaryBenchmark                    avgt        5   937.193 ±  673.097  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSummaryChildBenchmark               avgt        5  1311.323 ± 1060.701  ns/op

    java -jar target/benchmarks.jar SummaryBenchmark -wi 5 -i 5 -f 1 -t 2
    i.p.c.b.SummaryBenchmark.codahaleHistogramBenchmark                    avgt        5   263.326 ±   20.554  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramBenchmark            avgt        5    52.769 ±    3.865  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramChildBenchmark       avgt        5    28.209 ±    0.981  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramNoLabelsBenchmark    avgt        5    29.343 ±    0.838  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryBenchmark              avgt        5    44.794 ±    4.153  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryChildBenchmark         avgt        5    15.099 ±    0.902  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryNoLabelsBenchmark      avgt        5    15.522 ±    0.681  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSummaryBenchmark                    avgt        5  1889.093 ±  968.841  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSummaryChildBenchmark               avgt        5  2695.523 ± 2774.353  ns/op

    java -jar target/benchmarks.jar SummaryBenchmark -wi 5 -i 5 -f 1 -t 4
    i.p.c.b.SummaryBenchmark.codahaleHistogramBenchmark                    avgt        5   798.118 ±   52.079  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramBenchmark            avgt        5    92.614 ±   18.880  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramChildBenchmark       avgt        5    40.509 ±   11.914  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleHistogramNoLabelsBenchmark    avgt        5    37.694 ±    2.205  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryBenchmark              avgt        5    64.552 ±   33.391  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryChildBenchmark         avgt        5    20.459 ±    0.250  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSimpleSummaryNoLabelsBenchmark      avgt        5    21.050 ±    1.599  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSummaryBenchmark                    avgt        5  3968.455 ± 2428.140  ns/op
    i.p.c.b.SummaryBenchmark.prometheusSummaryChildBenchmark               avgt        5  5587.821 ± 4481.684  ns/op

Note the high error bars for the original client, it got slower with each iteration
so I suspect a flaw in the test setup.

### Exemplars

    java -jar target/benchmarks.jar ExemplarsBenchmark
    Benchmark                                               Mode  Samples   Score   Error  Units
    i.p.c.b.ExemplarsBenchmark.testCounter                    avgt      200  27.318 ± 0.347  ns/op
    i.p.c.b.ExemplarsBenchmark.testCounterWithExemplars       avgt      200  45.785 ± 0.177  ns/op
    i.p.c.b.ExemplarsBenchmark.testCounterWithoutExemplars    avgt      200  25.404 ± 0.184  ns/op