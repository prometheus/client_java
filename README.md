# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-05T04:36:18Z
- **Commit:** [`9e9deb6`](https://github.com/prometheus/client_java/commit/9e9deb6b9e591c2a62b12882d8a987f120661b71)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 290.89M | ± 7249.37K | ops/s |
| prometheusLabelValuesInc | 93.56M | ± 894.96K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 93.37M | ± 14.26K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.02M | ± 604.74K | ops/s |
| prometheusNoLabelsInc | 31.50K | ± 17.52 | ops/s |
| prometheusInc | 30.82K | ± 1.07K | ops/s |
| codahaleIncNoLabels | 29.09K | ± 491.68 | ops/s |
| openTelemetryBoundInc | 28.40K | ± 1.11K | ops/s |
| prometheusAdd | 28.16K | ± 557.86 | ops/s |
| openTelemetryBoundAdd | 26.02K | ± 1.29K | ops/s |
| openTelemetryIncNoLabels | 22.56K | ± 309.55 | ops/s |
| openTelemetryInc | 17.18K | ± 198.59 | ops/s |
| openTelemetryAdd | 15.30K | ± 89.64 | ops/s |
| simpleclientInc | 6.88K | ± 54.22 | ops/s |
| simpleclientNoLabelsInc | 6.62K | ± 73.15 | ops/s |
| simpleclientAdd | 6.54K | ± 295.76 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 7.82K | ± 67.04 | ops/s |
| simpleclient | 4.46K | ± 37.99 | ops/s |
| prometheusClassic | 3.33K | ± 287.66 | ops/s |
| openTelemetryClassic | 3.26K | ± 1.42K | ops/s |
| prometheusClassicSingleThread | 3.23K | ± 74.75 | ops/s |
| openTelemetryBoundClassic | 3.07K | ± 246.65 | ops/s |
| prometheusNative | 2.05K | ± 138.49 | ops/s |
| openTelemetryBoundExponential | 562.61 | ± 53.65 | ops/s |
| openTelemetryExponential | 494.96 | ± 51.47 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 18.13K | ± 158.92 | ops/s |
| openMetricsWriteToNull | 18.10K | ± 250.96 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 333.78K | ± 3.19K | ops/s |
| prometheusWriteToByteArray | 330.05K | ± 2.41K | ops/s |
| openMetricsWriteToNull | 310.64K | ± 2.02K | ops/s |
| openMetricsWriteToByteArray | 310.12K | ± 2.64K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.032 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.061 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.036 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.033 | — | — |
| CounterBenchmark.openTelemetryInc | 0.054 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.041 | — | — |
| CounterBenchmark.prometheusAdd | 0.131 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.119 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.117 | — | — |
| CounterBenchmark.simpleclientAdd | 0.142 | — | — |
| CounterBenchmark.simpleclientInc | 0.135 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.140 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.310 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.666 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.319 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.899 | — | — |
| HistogramBenchmark.prometheusClassic | 1.116 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 1.041 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.875 | — | — |
| HistogramBenchmark.prometheusNative | 335793.826 | — | — |
| HistogramBenchmark.simpleclient | 0.210 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.193 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.193 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.002 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.002 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18448.002 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29085.357    ± 491.680  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15297.802     ± 89.644  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      26018.484   ± 1287.425  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      28397.549   ± 1109.346  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      17175.582    ± 198.589  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22561.639    ± 309.550  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28163.960    ± 557.862  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  290889966.961 ± 7249369.604  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15   93365472.913  ± 14262.089  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      30815.260   ± 1073.853  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15   93559989.048 ± 894957.140  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58018130.302 ± 604743.470  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      31503.611     ± 17.523  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6543.694    ± 295.755  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6875.956     ± 54.221  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6617.491     ± 73.154  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3073.443    ± 246.647  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        562.607     ± 53.649  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3259.797   ± 1422.591  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        494.961     ± 51.473  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3327.457    ± 287.657  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15       7823.999     ± 67.036  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       3233.454     ± 74.749  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2048.378    ± 138.486  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4463.388     ± 37.993  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18099.754    ± 250.965  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18134.115    ± 158.919  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     310124.309   ± 2636.991  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     310642.009   ± 2019.297  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     330053.215   ± 2409.619  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     333778.500   ± 3188.906  ops/s
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
