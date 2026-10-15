# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-15T04:23:45Z
- **Commit:** [`d418a89`](https://github.com/prometheus/client_java/commit/d418a89b075944fe5da2c7dd8aeb72d9297ffd02)
- **JDK:** 25.0.4.1 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1012-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 552.58M | ± 10861.48K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.52M | ± 319.09K | ops/s |
| prometheusLabelValuesInc | 115.05M | ± 1937.99K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.26M | ± 149.11K | ops/s |
| prometheusInc | 64.85K | ± 1.79K | ops/s |
| prometheusNoLabelsInc | 56.46K | ± 1.00K | ops/s |
| prometheusAdd | 50.60K | ± 874.39 | ops/s |
| codahaleIncNoLabels | 49.33K | ± 1.11K | ops/s |
| openTelemetryBoundInc | 37.85K | ± 630.03 | ops/s |
| openTelemetryBoundAdd | 31.26K | ± 596.72 | ops/s |
| openTelemetryInc | 23.09K | ± 330.19 | ops/s |
| openTelemetryIncNoLabels | 21.69K | ± 349.85 | ops/s |
| openTelemetryAdd | 19.23K | ± 275.27 | ops/s |
| simpleclientInc | 6.48K | ± 108.87 | ops/s |
| simpleclientAdd | 6.31K | ± 217.58 | ops/s |
| simpleclientNoLabelsInc | 6.23K | ± 119.97 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 12.00K | ± 160.35 | ops/s |
| prometheusClassic | 6.90K | ± 1.21K | ops/s |
| openTelemetryBoundClassic | 6.09K | ± 1.80K | ops/s |
| prometheusClassicSingleThread | 4.51K | ± 91.70 | ops/s |
| simpleclient | 4.43K | ± 82.96 | ops/s |
| openTelemetryClassic | 3.12K | ± 365.21 | ops/s |
| prometheusNative | 2.85K | ± 396.67 | ops/s |
| openTelemetryBoundExponential | 1.00K | ± 79.09 | ops/s |
| openTelemetryExponential | 998.72 | ± 45.84 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| openMetricsWriteToNull | 24.09K | ± 355.68 | ops/s |
| prometheusWriteToNull | 24.05K | ± 879.88 | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 492.21K | ± 9.65K | ops/s |
| prometheusWriteToByteArray | 485.63K | ± 2.79K | ops/s |
| openMetricsWriteToNull | 471.17K | ± 3.70K | ops/s |
| openMetricsWriteToByteArray | 468.86K | ± 10.59K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.049 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.030 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.040 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.043 | — | — |
| CounterBenchmark.prometheusAdd | 0.073 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.057 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.148 | — | — |
| CounterBenchmark.simpleclientInc | 0.144 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.150 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.166 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 0.942 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.307 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.938 | — | — |
| HistogramBenchmark.prometheusClassic | 0.545 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.675 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.649 | — | — |
| HistogramBenchmark.prometheusNative | 417713.318 | — | — |
| HistogramBenchmark.simpleclient | 0.213 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.146 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.147 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18424.001 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18485.335 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18466.668 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49334.552   ± 1112.462  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19229.514    ± 275.271  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      31257.933    ± 596.716  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37847.356    ± 630.025  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      23088.128    ± 330.191  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      21694.252    ± 349.848  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50599.667    ± 874.386  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  552581129.081 ± 10861480.117  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334521989.142 ± 319086.209  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64849.514   ± 1785.043  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  115047408.736 ± 1937987.385  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58259730.203 ± 149107.908  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56459.956   ± 1000.903  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6314.818    ± 217.577  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6477.143    ± 108.875  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6233.553    ± 119.966  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       6092.711   ± 1800.472  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15       1003.089     ± 79.091  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       3117.472    ± 365.212  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        998.718     ± 45.843  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6898.200   ± 1206.044  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12001.468    ± 160.347  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4505.734     ± 91.703  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2850.049    ± 396.666  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4429.675     ± 82.958  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24087.039    ± 355.683  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      24050.583    ± 879.877  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     468860.287  ± 10589.136  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     471168.216   ± 3695.096  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     485634.320   ± 2786.952  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     492210.155   ± 9649.960  ops/s
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
