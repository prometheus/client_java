# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-30T04:17:31Z
- **Commit:** [`6d27435`](https://github.com/prometheus/client_java/commit/6d274350ef8c6bacee3dab4d77608b2f9b8bbe39)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 590.15M | ± 11885.20K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 361.50M | ± 552.72K | ops/s |
| prometheusLabelValuesInc | 124.46M | ± 2055.75K | ops/s |
| prometheusLabelValuesIncSingleThread | 61.02M | ± 3074.52K | ops/s |
| prometheusInc | 69.73K | ± 1.31K | ops/s |
| prometheusNoLabelsInc | 58.98K | ± 1.64K | ops/s |
| prometheusAdd | 52.58K | ± 2.58K | ops/s |
| codahaleIncNoLabels | 50.85K | ± 1.35K | ops/s |
| openTelemetryBoundInc | 40.21K | ± 617.45 | ops/s |
| openTelemetryBoundAdd | 34.04K | ± 503.09 | ops/s |
| openTelemetryIncNoLabels | 24.77K | ± 1.24K | ops/s |
| openTelemetryInc | 24.41K | ± 389.49 | ops/s |
| openTelemetryAdd | 20.63K | ± 331.11 | ops/s |
| simpleclientInc | 6.89K | ± 80.64 | ops/s |
| simpleclientAdd | 6.75K | ± 220.66 | ops/s |
| simpleclientNoLabelsInc | 6.68K | ± 88.46 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.74K | ± 163.11 | ops/s |
| prometheusClassic | 7.82K | ± 976.13 | ops/s |
| prometheusClassicSingleThread | 4.89K | ± 22.60 | ops/s |
| simpleclient | 4.71K | ± 62.91 | ops/s |
| openTelemetryClassic | 4.52K | ± 387.44 | ops/s |
| openTelemetryBoundClassic | 3.17K | ± 714.12 | ops/s |
| prometheusNative | 3.16K | ± 112.11 | ops/s |
| openTelemetryBoundExponential | 1.10K | ± 55.34 | ops/s |
| openTelemetryExponential | 1.01K | ± 58.69 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 34.03K | ± 418.50 | ops/s |
| openMetricsWriteToNull | 33.75K | ± 346.94 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 545.82K | ± 5.09K | ops/s |
| prometheusWriteToByteArray | 536.53K | ± 3.47K | ops/s |
| openMetricsWriteToNull | 530.84K | ± 5.63K | ops/s |
| openMetricsWriteToByteArray | 525.58K | ± 7.66K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.018 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.045 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.028 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.023 | — | — |
| CounterBenchmark.openTelemetryInc | 0.038 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.038 | — | — |
| CounterBenchmark.prometheusAdd | 0.070 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.053 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.062 | — | — |
| CounterBenchmark.simpleclientAdd | 0.139 | — | — |
| CounterBenchmark.simpleclientInc | 0.135 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.140 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.311 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.851 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.211 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.926 | — | — |
| HistogramBenchmark.prometheusClassic | 0.479 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.651 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.597 | — | — |
| HistogramBenchmark.prometheusNative | 335793.171 | — | — |
| HistogramBenchmark.simpleclient | 0.200 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.104 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.104 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18504.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18469.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50852.022   ± 1346.921  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      20632.126    ± 331.105  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      34043.157    ± 503.088  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      40211.032    ± 617.446  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      24411.395    ± 389.486  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      24771.353   ± 1236.190  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      52581.785   ± 2582.178  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  590150867.875 ± 11885204.479  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  361495118.907 ± 552721.265  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      69726.872   ± 1309.654  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  124456534.587 ± 2055753.468  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   61015832.095 ± 3074524.794  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      58975.591   ± 1638.075  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6745.969    ± 220.658  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6894.059     ± 80.641  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6682.211     ± 88.460  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       3167.216    ± 714.118  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1098.565     ± 55.338  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4522.038    ± 387.436  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15       1012.757     ± 58.689  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7816.502    ± 976.126  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12737.561    ± 163.112  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4885.058     ± 22.595  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3157.929    ± 112.108  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4706.764     ± 62.912  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      33749.452    ± 346.937  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      34028.013    ± 418.500  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     525580.179   ± 7662.265  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     530841.219   ± 5627.723  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     536532.112   ± 3470.193  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     545815.296   ± 5090.494  ops/s
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
