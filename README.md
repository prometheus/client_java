# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-11-03T04:15:06Z
- **Commit:** [`7911c52`](https://github.com/prometheus/client_java/commit/7911c52e92b69b379bedc02d19a11c09d484eb64)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 9V45 96-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 714.36M | ± 20713.44K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 217.18M | ± 4391.31K | ops/s |
| prometheusLabelValuesInc | 200.25M | ± 8181.58K | ops/s |
| prometheusLabelValuesIncSingleThread | 98.37M | ± 1166.45K | ops/s |
| prometheusNoLabelsInc | 66.22K | ± 2.03K | ops/s |
| prometheusInc | 65.69K | ± 181.24 | ops/s |
| codahaleIncNoLabels | 60.63K | ± 2.93K | ops/s |
| prometheusAdd | 57.70K | ± 555.32 | ops/s |
| openTelemetryBoundInc | 54.19K | ± 442.43 | ops/s |
| openTelemetryBoundAdd | 48.65K | ± 562.04 | ops/s |
| openTelemetryIncNoLabels | 35.68K | ± 1.06K | ops/s |
| openTelemetryInc | 34.51K | ± 2.69K | ops/s |
| openTelemetryAdd | 29.57K | ± 1.44K | ops/s |
| simpleclientInc | 10.85K | ± 87.12 | ops/s |
| simpleclientNoLabelsInc | 10.71K | ± 164.07 | ops/s |
| simpleclientAdd | 10.42K | ± 259.95 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 15.09K | ± 265.03 | ops/s |
| prometheusClassic | 8.64K | ± 2.44K | ops/s |
| simpleclient | 7.35K | ± 188.49 | ops/s |
| openTelemetryClassic | 7.02K | ± 1.33K | ops/s |
| prometheusClassicSingleThread | 4.51K | ± 58.43 | ops/s |
| prometheusNative | 4.47K | ± 179.00 | ops/s |
| openTelemetryBoundClassic | 4.23K | ± 34.59 | ops/s |
| openTelemetryBoundExponential | 1.20K | ± 22.68 | ops/s |
| openTelemetryExponential | 1.11K | ± 54.84 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 36.68K | ± 306.76 | ops/s |
| openMetricsWriteToNull | 36.51K | ± 492.03 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToByteArray | 886.56K | ± 14.79K | ops/s |
| prometheusWriteToNull | 820.13K | ± 33.00K | ops/s |
| openMetricsWriteToNull | 791.39K | ± 47.79K | ops/s |
| openMetricsWriteToByteArray | 724.34K | ± 16.78K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.016 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.032 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.019 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.017 | — | — |
| CounterBenchmark.openTelemetryInc | 0.028 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.026 | — | — |
| CounterBenchmark.prometheusAdd | 0.064 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.056 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.056 | — | — |
| CounterBenchmark.simpleclientAdd | 0.091 | — | — |
| CounterBenchmark.simpleclientInc | 0.087 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.088 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.224 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.791 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.139 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.854 | — | — |
| HistogramBenchmark.prometheusClassic | 0.461 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.538 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.633 | — | — |
| HistogramBenchmark.prometheusNative | 335792.848 | — | — |
| HistogramBenchmark.simpleclient | 0.130 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.097 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.096 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18477.334 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18477.334 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      60627.273   ± 2926.028  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      29573.049   ± 1435.816  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      48653.828    ± 562.041  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      54191.189    ± 442.426  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      34506.572   ± 2686.505  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      35682.673   ± 1063.306  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      57698.238    ± 555.318  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  714364012.643 ± 20713437.239  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  217180222.450 ± 4391313.362  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65687.392    ± 181.236  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  200249351.812 ± 8181578.788  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   98372057.088 ± 1166447.168  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66219.283   ± 2032.213  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15      10421.218    ± 259.954  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15      10850.092     ± 87.122  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15      10709.923    ± 164.074  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4227.933     ± 34.585  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1195.976     ± 22.681  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       7015.605   ± 1333.862  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15       1108.224     ± 54.840  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       8642.037   ± 2436.439  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      15093.836    ± 265.034  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4506.611     ± 58.426  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       4469.404    ± 178.996  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       7347.962    ± 188.489  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      36512.798    ± 492.030  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      36682.349    ± 306.758  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     724344.490  ± 16784.200  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     791390.798  ± 47793.929  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     886564.771  ± 14787.535  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     820132.301  ± 32996.862  ops/s
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
