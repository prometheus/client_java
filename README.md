# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-09-22T04:25:15Z
- **Commit:** [`5bf31dc`](https://github.com/prometheus/client_java/commit/5bf31dc287edac5eb052f57a561b61b8a3309133)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1022-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 555.71M | ± 7386.34K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.82M | ± 389.19K | ops/s |
| prometheusLabelValuesInc | 116.81M | ± 783.72K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.55M | ± 174.47K | ops/s |
| prometheusInc | 65.17K | ± 1.29K | ops/s |
| prometheusNoLabelsInc | 56.54K | ± 1.29K | ops/s |
| prometheusAdd | 51.15K | ± 459.77 | ops/s |
| codahaleIncNoLabels | 49.02K | ± 1.32K | ops/s |
| openTelemetryBoundInc | 38.07K | ± 376.01 | ops/s |
| openTelemetryBoundAdd | 31.83K | ± 393.85 | ops/s |
| openTelemetryIncNoLabels | 22.79K | ± 1.05K | ops/s |
| openTelemetryInc | 18.05K | ± 80.63 | ops/s |
| openTelemetryAdd | 15.52K | ± 51.83 | ops/s |
| simpleclientInc | 6.53K | ± 37.64 | ops/s |
| simpleclientNoLabelsInc | 6.24K | ± 155.51 | ops/s |
| simpleclientAdd | 6.24K | ± 336.45 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.02K | ± 54.94 | ops/s |
| openTelemetryBoundClassic | 7.47K | ± 1.58K | ops/s |
| prometheusClassic | 7.20K | ± 64.62 | ops/s |
| openTelemetryClassic | 4.55K | ± 782.58 | ops/s |
| prometheusClassicSingleThread | 4.54K | ± 11.44 | ops/s |
| simpleclient | 4.43K | ± 61.00 | ops/s |
| prometheusNative | 2.71K | ± 321.03 | ops/s |
| openTelemetryBoundExponential | 1.01K | ± 96.70 | ops/s |
| openTelemetryExponential | 858.58 | ± 55.41 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 24.31K | ± 759.54 | ops/s |
| openMetricsWriteToNull | 23.65K | ± 686.14 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 556.13K | ± 3.61K | ops/s |
| prometheusWriteToByteArray | 552.45K | ± 5.65K | ops/s |
| openMetricsWriteToNull | 523.13K | ± 7.63K | ops/s |
| openMetricsWriteToByteArray | 517.28K | ± 6.33K | ops/s |

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
| CounterBenchmark.prometheusAdd | 0.072 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.057 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.150 | — | — |
| CounterBenchmark.simpleclientInc | 0.142 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.149 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.131 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.923 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.209 | — | — |
| HistogramBenchmark.openTelemetryExponential | 1.085 | — | — |
| HistogramBenchmark.prometheusClassic | 0.513 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.661 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.642 | — | — |
| HistogramBenchmark.prometheusNative | 417713.380 | — | — |
| HistogramBenchmark.simpleclient | 0.210 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.148 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.144 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18448.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18466.668 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49019.183   ± 1320.418  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      15524.287     ± 51.828  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31830.088    ± 393.854  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      38068.944    ± 376.013  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      18054.810     ± 80.633  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22787.074   ± 1049.466  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51153.231    ± 459.773  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  555714601.990 ± 7386342.009  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334821439.610 ± 389186.839  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65170.532   ± 1292.200  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  116808790.817 ± 783724.673  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58545445.636 ± 174467.897  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56536.867   ± 1289.511  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6236.623    ± 336.446  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6533.896     ± 37.640  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6241.186    ± 155.515  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       7470.144   ± 1581.372  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1007.992     ± 96.700  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       4552.675    ± 782.582  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        858.581     ± 55.409  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       7203.995     ± 64.616  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12023.638     ± 54.941  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4538.118     ± 11.435  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2709.153    ± 321.034  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4432.775     ± 60.996  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23653.463    ± 686.140  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24310.775    ± 759.544  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     517279.304   ± 6326.127  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     523128.378   ± 7629.540  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     552454.209   ± 5654.826  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     556129.521   ± 3605.554  ops/s
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
