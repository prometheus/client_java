# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-09T04:16:05Z
- **Commit:** [`85d9fb0`](https://github.com/prometheus/client_java/commit/85d9fb0dca9f064c746470e91b924d253abf8d30)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1019-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 286.91M | ± 9929.59K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 93.23M | ± 23.90K | ops/s |
| prometheusLabelValuesInc | 92.50M | ± 805.21K | ops/s |
| prometheusLabelValuesIncSingleThread | 57.07M | ± 645.36K | ops/s |
| prometheusInc | 31.43K | ± 78.29 | ops/s |
| prometheusNoLabelsInc | 30.44K | ± 1.46K | ops/s |
| codahaleIncNoLabels | 29.32K | ± 1.14K | ops/s |
| openTelemetryBoundInc | 27.78K | ± 2.00K | ops/s |
| prometheusAdd | 27.71K | ± 708.42 | ops/s |
| openTelemetryBoundAdd | 27.10K | ± 812.12 | ops/s |
| openTelemetryIncNoLabels | 20.79K | ± 2.15K | ops/s |
| openTelemetryInc | 20.51K | ± 2.13K | ops/s |
| openTelemetryAdd | 16.89K | ± 881.98 | ops/s |
| simpleclientInc | 6.54K | ± 224.58 | ops/s |
| simpleclientAdd | 6.16K | ± 91.57 | ops/s |
| simpleclientNoLabelsInc | 6.08K | ± 134.07 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 7.69K | ± 259.58 | ops/s |
| prometheusClassic | 4.81K | ± 1.67K | ops/s |
| simpleclient | 4.05K | ± 109.40 | ops/s |
| openTelemetryBoundClassic | 3.51K | ± 2.19K | ops/s |
| prometheusClassicSingleThread | 3.23K | ± 80.78 | ops/s |
| openTelemetryClassic | 2.95K | ± 297.42 | ops/s |
| prometheusNative | 2.27K | ± 252.35 | ops/s |
| openTelemetryBoundExponential | 665.30 | ± 101.47 | ops/s |
| openTelemetryExponential | 574.84 | ± 20.55 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 19.72K | ± 176.79 | ops/s |
| openMetricsWriteToNull | 19.60K | ± 147.02 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 337.76K | ± 2.27K | ops/s |
| prometheusWriteToByteArray | 334.29K | ± 3.54K | ops/s |
| openMetricsWriteToNull | 306.77K | ± 6.33K | ops/s |
| openMetricsWriteToByteArray | 300.63K | ± 1.32K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.032 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.056 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.035 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.034 | — | — |
| CounterBenchmark.openTelemetryInc | 0.046 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.045 | — | — |
| CounterBenchmark.prometheusAdd | 0.133 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.118 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.122 | — | — |
| CounterBenchmark.simpleclientAdd | 0.152 | — | — |
| CounterBenchmark.simpleclientInc | 0.143 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.155 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.328 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.435 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.323 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.629 | — | — |
| HistogramBenchmark.prometheusClassic | 0.850 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.063 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.862 | — | — |
| HistogramBenchmark.prometheusNative | 253873.693 | — | — |
| HistogramBenchmark.simpleclient | 0.231 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.180 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.179 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.002 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29323.547   ± 1142.236  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      16892.900    ± 881.985  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      27100.119    ± 812.124  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      27778.271   ± 1999.615  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      20514.246   ± 2132.921  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      20792.002   ± 2147.309  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      27708.228    ± 708.418  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  286907542.447 ± 9929590.499  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15   93232751.166  ± 23895.762  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31430.693     ± 78.289  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15   92503286.527 ± 805206.325  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   57074613.835 ± 645361.646  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      30435.306   ± 1463.845  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6156.526     ± 91.574  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6538.339    ± 224.583  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6084.760    ± 134.072  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3505.186   ± 2193.360  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        665.296    ± 101.467  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       2952.939    ± 297.416  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        574.840     ± 20.554  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4811.955   ± 1674.832  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       7690.895    ± 259.583  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       3228.821     ± 80.784  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2268.257    ± 252.354  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4051.246    ± 109.403  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      19599.638    ± 147.025  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      19721.760    ± 176.788  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     300631.781   ± 1322.071  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     306773.712   ± 6327.539  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     334287.203   ± 3544.818  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     337762.658   ± 2272.288  ops/s
```

## Notes

- **Score** = the JMH primary metric; throughput is higher-is-better and latency is lower-is-better.
- **Error** = 99.9% confidence interval
- Scores for different benchmark methods are not ranked against one another; they may measure different workloads.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter updates and label-value lookup (selected methods only) |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |
