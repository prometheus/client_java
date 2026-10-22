# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-22T04:24:23Z
- **Commit:** [`ec63f9b`](https://github.com/prometheus/client_java/commit/ec63f9be83f5f079424be2a29f319f7175f54c9e)
- **JDK:** 25.0.4.1 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 677.40M | ± 14202.18K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 370.60M | ± 347.04K | ops/s |
| prometheusLabelValuesInc | 138.96M | ± 2441.32K | ops/s |
| prometheusLabelValuesIncSingleThread | 68.29M | ± 320.07K | ops/s |
| prometheusInc | 77.79K | ± 886.90 | ops/s |
| prometheusNoLabelsInc | 66.78K | ± 181.40 | ops/s |
| prometheusAdd | 62.75K | ± 1.07K | ops/s |
| codahaleIncNoLabels | 57.00K | ± 160.22 | ops/s |
| openTelemetryBoundInc | 44.85K | ± 109.09 | ops/s |
| openTelemetryBoundAdd | 38.81K | ± 501.41 | ops/s |
| openTelemetryIncNoLabels | 27.76K | ± 744.86 | ops/s |
| openTelemetryInc | 27.47K | ± 40.48 | ops/s |
| openTelemetryAdd | 23.99K | ± 197.64 | ops/s |
| simpleclientInc | 7.85K | ± 119.18 | ops/s |
| simpleclientNoLabelsInc | 7.56K | ± 58.69 | ops/s |
| simpleclientAdd | 7.25K | ± 437.39 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 17.54K | ± 93.92 | ops/s |
| prometheusClassic | 9.72K | ± 1.76K | ops/s |
| openTelemetryBoundClassic | 7.53K | ± 1.34K | ops/s |
| prometheusClassicSingleThread | 7.11K | ± 62.89 | ops/s |
| openTelemetryClassic | 6.70K | ± 1.94K | ops/s |
| simpleclient | 5.77K | ± 112.18 | ops/s |
| prometheusNative | 3.65K | ± 257.98 | ops/s |
| openTelemetryBoundExponential | 939.98 | ± 39.42 | ops/s |
| openTelemetryExponential | 922.78 | ± 28.88 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 40.86K | ± 155.32 | ops/s |
| openMetricsWriteToNull | 40.47K | ± 555.52 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 781.48K | ± 7.12K | ops/s |
| prometheusWriteToByteArray | 771.33K | ± 20.63K | ops/s |
| openMetricsWriteToNull | 723.82K | ± 6.29K | ops/s |
| openMetricsWriteToByteArray | 704.66K | ± 15.31K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.017 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.039 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.024 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.021 | — | — |
| CounterBenchmark.openTelemetryInc | 0.034 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.034 | — | — |
| CounterBenchmark.prometheusAdd | 0.059 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.047 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.056 | — | — |
| CounterBenchmark.simpleclientAdd | 0.130 | — | — |
| CounterBenchmark.simpleclientInc | 0.120 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.125 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.130 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.006 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.152 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.027 | — | — |
| HistogramBenchmark.prometheusClassic | 0.390 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.463 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.411 | — | — |
| HistogramBenchmark.prometheusNative | 417713.017 | — | — |
| HistogramBenchmark.simpleclient | 0.164 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.087 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.086 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18466.668 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18496.001 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      57003.775    ± 160.216  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      23993.181    ± 197.637  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      38810.892    ± 501.414  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      44847.472    ± 109.091  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      27465.163     ± 40.478  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      27758.730    ± 744.863  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      62746.162   ± 1073.978  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  677395408.320 ± 14202179.512  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  370604914.474 ± 347035.649  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      77786.525    ± 886.902  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  138960380.758 ± 2441319.496  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   68293126.092 ± 320065.645  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66776.829    ± 181.399  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7253.695    ± 437.391  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7849.733    ± 119.180  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7555.961     ± 58.689  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       7534.611   ± 1340.868  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        939.976     ± 39.420  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       6699.848   ± 1940.293  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        922.783     ± 28.885  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       9723.575   ± 1763.674  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      17537.769     ± 93.923  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       7110.223     ± 62.885  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3648.998    ± 257.982  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5769.025    ± 112.179  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      40466.083    ± 555.517  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      40861.304    ± 155.318  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     704663.984  ± 15309.333  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     723816.591   ± 6288.487  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     771326.417  ± 20627.162  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     781478.548   ± 7123.110  ops/s
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
