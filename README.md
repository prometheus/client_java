# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-01T04:35:06Z
- **Commit:** [`ba050b0`](https://github.com/prometheus/client_java/commit/ba050b022f6e06203df318a7871b744cc4d6a167)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 545.27M | ± 11577.29K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.65M | ± 477.08K | ops/s |
| prometheusLabelValuesInc | 115.04M | ± 600.67K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.34M | ± 336.76K | ops/s |
| prometheusInc | 66.57K | ± 306.62 | ops/s |
| prometheusNoLabelsInc | 56.40K | ± 88.11 | ops/s |
| prometheusAdd | 51.57K | ± 134.28 | ops/s |
| codahaleIncNoLabels | 47.95K | ± 389.28 | ops/s |
| openTelemetryBoundInc | 38.03K | ± 380.79 | ops/s |
| openTelemetryBoundAdd | 31.71K | ± 136.86 | ops/s |
| openTelemetryIncNoLabels | 22.40K | ± 904.64 | ops/s |
| openTelemetryInc | 18.11K | ± 165.69 | ops/s |
| openTelemetryAdd | 15.57K | ± 21.99 | ops/s |
| simpleclientInc | 6.55K | ± 47.89 | ops/s |
| simpleclientNoLabelsInc | 6.35K | ± 25.95 | ops/s |
| simpleclientAdd | 6.29K | ± 265.41 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.06K | ± 32.28 | ops/s |
| prometheusClassic | 6.34K | ± 897.72 | ops/s |
| openTelemetryBoundClassic | 5.73K | ± 960.26 | ops/s |
| prometheusClassicSingleThread | 4.54K | ± 15.25 | ops/s |
| simpleclient | 4.39K | ± 27.99 | ops/s |
| openTelemetryClassic | 4.30K | ± 823.80 | ops/s |
| prometheusNative | 2.95K | ± 356.85 | ops/s |
| openTelemetryBoundExponential | 1.08K | ± 47.72 | ops/s |
| openTelemetryExponential | 783.68 | ± 62.57 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 23.89K | ± 486.89 | ops/s |
| openMetricsWriteToNull | 23.61K | ± 107.38 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 559.20K | ± 2.92K | ops/s |
| prometheusWriteToByteArray | 551.65K | ± 5.22K | ops/s |
| openMetricsWriteToNull | 537.86K | ± 5.21K | ops/s |
| openMetricsWriteToByteArray | 529.80K | ± 2.75K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.060 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.024 | — | — |
| CounterBenchmark.openTelemetryInc | 0.051 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.041 | — | — |
| CounterBenchmark.prometheusAdd | 0.071 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.055 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.148 | — | — |
| CounterBenchmark.simpleclientInc | 0.143 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.147 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.167 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.857 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.224 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.187 | — | — |
| HistogramBenchmark.prometheusClassic | 0.592 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.657 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.642 | — | — |
| HistogramBenchmark.prometheusNative | 335793.274 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.148 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.146 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18429.335 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47946.531    ± 389.281  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15574.189     ± 21.995  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31714.140    ± 136.856  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      38029.398    ± 380.795  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18105.624    ± 165.687  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22397.134    ± 904.641  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51565.012    ± 134.278  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  545265303.026 ± 11577290.850  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334652644.554 ± 477076.705  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66568.741    ± 306.618  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  115041393.458 ± 600674.104  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58342231.064 ± 336759.619  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56402.266     ± 88.111  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6293.170    ± 265.414  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6551.605     ± 47.888  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6350.791     ± 25.951  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       5732.258    ± 960.258  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1084.490     ± 47.716  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4298.461    ± 823.799  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        783.679     ± 62.569  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6340.555    ± 897.724  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12055.085     ± 32.284  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4535.123     ± 15.253  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2947.858    ± 356.846  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4390.110     ± 27.987  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23605.495    ± 107.380  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23893.410    ± 486.895  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     529803.949   ± 2754.434  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     537864.476   ± 5208.965  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     551645.479   ± 5224.983  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     559201.499   ± 2920.656  ops/s
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
