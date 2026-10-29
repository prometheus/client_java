# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-10-29T04:17:47Z
- **Commit:** [`c3c4958`](https://github.com/prometheus/client_java/commit/c3c4958edded42e6550ba5907db4295ad07b68b1)
- **JDK:** 25.0.5 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 1/4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 7.0.0-1017-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusCachedLabelValuesInc | 555.28M | ± 9522.30K | ops/s |
| prometheusCachedLabelValuesIncSingleThread | 334.31M | ± 498.91K | ops/s |
| prometheusLabelValuesInc | 114.11M | ± 2445.17K | ops/s |
| prometheusLabelValuesIncSingleThread | 58.31M | ± 385.33K | ops/s |
| prometheusInc | 65.05K | ± 1.92K | ops/s |
| prometheusNoLabelsInc | 56.75K | ± 979.68 | ops/s |
| codahaleIncNoLabels | 49.38K | ± 1.66K | ops/s |
| prometheusAdd | 48.94K | ± 2.47K | ops/s |
| openTelemetryBoundInc | 37.65K | ± 652.42 | ops/s |
| openTelemetryBoundAdd | 32.03K | ± 528.59 | ops/s |
| openTelemetryIncNoLabels | 22.47K | ± 1.26K | ops/s |
| openTelemetryInc | 21.95K | ± 1.88K | ops/s |
| openTelemetryAdd | 19.36K | ± 282.81 | ops/s |
| simpleclientInc | 6.50K | ± 91.07 | ops/s |
| simpleclientNoLabelsInc | 6.28K | ± 89.17 | ops/s |
| simpleclientAdd | 6.06K | ± 289.05 | ops/s |

### HistogramBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusClassicPerThread | 11.99K | ± 185.62 | ops/s |
| prometheusClassic | 6.78K | ± 747.12 | ops/s |
| openTelemetryClassic | 5.66K | ± 768.26 | ops/s |
| openTelemetryBoundClassic | 4.64K | ± 2.49K | ops/s |
| prometheusClassicSingleThread | 4.52K | ± 19.59 | ops/s |
| simpleclient | 4.40K | ± 76.24 | ops/s |
| prometheusNative | 2.55K | ± 246.88 | ops/s |
| openTelemetryExponential | 967.89 | ± 61.43 | ops/s |
| openTelemetryBoundExponential | 941.40 | ± 107.03 | ops/s |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToNull | 30.71K | ± 452.92 | ops/s |
| openMetricsWriteToNull | 29.25K | ± 1.01K | ops/s |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units |
|:----------|------:|------:|:------|
| prometheusWriteToByteArray | 514.72K | ± 15.24K | ops/s |
| prometheusWriteToNull | 513.17K | ± 6.74K | ops/s |
| openMetricsWriteToNull | 494.65K | ± 9.73K | ops/s |
| openMetricsWriteToByteArray | 490.61K | ± 8.10K | ops/s |

## Allocation per operation

JMH GC profiler `gc.alloc.rate.norm`, in bytes per benchmark operation (lower is better).
Delta is PR minus base, shown only for matching benchmark configurations. Values are descriptive, not statistical regression verdicts; — means unavailable or not comparable. Each benchmark defines its own operation.

| Benchmark | PR B/op | Base B/op | Delta B/op |
|:----------|--------:|----------:|-----------:|
| CounterBenchmark.codahaleIncNoLabels | 0.019 | — | — |
| CounterBenchmark.openTelemetryAdd | 0.048 | — | — |
| CounterBenchmark.openTelemetryBoundAdd | 0.029 | — | — |
| CounterBenchmark.openTelemetryBoundInc | 0.025 | — | — |
| CounterBenchmark.openTelemetryInc | 0.043 | — | — |
| CounterBenchmark.openTelemetryIncNoLabels | 0.042 | — | — |
| CounterBenchmark.prometheusAdd | 0.075 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesInc | 0.000 | — | — |
| CounterBenchmark.prometheusCachedLabelValuesIncSingleThread | 0.000 | — | — |
| CounterBenchmark.prometheusInc | 0.057 | — | — |
| CounterBenchmark.prometheusLabelValuesInc | 48.000 | — | — |
| CounterBenchmark.prometheusLabelValuesIncSingleThread | 48.000 | — | — |
| CounterBenchmark.prometheusNoLabelsInc | 0.065 | — | — |
| CounterBenchmark.simpleclientAdd | 0.155 | — | — |
| CounterBenchmark.simpleclientInc | 0.144 | — | — |
| CounterBenchmark.simpleclientNoLabelsInc | 0.149 | — | — |
| HistogramBenchmark.openTelemetryBoundClassic | 0.252 | — | — |
| HistogramBenchmark.openTelemetryBoundExponential | 1.006 | — | — |
| HistogramBenchmark.openTelemetryClassic | 0.171 | — | — |
| HistogramBenchmark.openTelemetryExponential | 0.974 | — | — |
| HistogramBenchmark.prometheusClassic | 0.550 | — | — |
| HistogramBenchmark.prometheusClassicPerThread | 0.673 | — | — |
| HistogramBenchmark.prometheusClassicSingleThread | 0.645 | — | — |
| HistogramBenchmark.prometheusNative | 417713.472 | — | — |
| HistogramBenchmark.simpleclient | 0.214 | — | — |
| HistogramTextFormatBenchmark.openMetricsWriteToNull | 43648.121 | — | — |
| HistogramTextFormatBenchmark.prometheusWriteToNull | 43648.115 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToByteArray | 18424.001 | — | — |
| TextFormatUtilBenchmark.openMetricsWriteToNull | 18442.668 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToByteArray | 18458.668 | — | — |
| TextFormatUtilBenchmark.prometheusWriteToNull | 18466.668 | — | — |

### Raw Results

```text
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49383.985   ± 1659.820  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      19360.177    ± 282.809  ops/s
CounterBenchmark.openTelemetryBoundAdd              thrpt   15      32028.681    ± 528.591  ops/s
CounterBenchmark.openTelemetryBoundInc              thrpt   15      37651.227    ± 652.417  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      21953.419   ± 1883.349  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      22467.038   ± 1260.487  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48942.799   ± 2469.913  ops/s
CounterBenchmark.prometheusCachedLabelValuesInc     thrpt   15  555276672.095 ± 9522299.487  ops/s
CounterBenchmark.prometheusCachedLabelValuesIncSingleThread  thrpt   15  334314897.052 ± 498909.232  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65047.201   ± 1920.240  ops/s
CounterBenchmark.prometheusLabelValuesInc           thrpt   15  114113837.061 ± 2445173.943  ops/s
CounterBenchmark.prometheusLabelValuesIncSingleThread  thrpt   15   58307348.712 ± 385334.155  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56754.727    ± 979.677  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6057.188    ± 289.053  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6495.029     ± 91.073  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6276.553     ± 89.173  ops/s
HistogramBenchmark.openTelemetryBoundClassic        thrpt   15       4642.974   ± 2494.717  ops/s
HistogramBenchmark.openTelemetryBoundExponential    thrpt   15        941.396    ± 107.030  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15       5660.809    ± 768.257  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        967.894     ± 61.431  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6784.096    ± 747.122  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      11985.082    ± 185.623  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4523.206     ± 19.590  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2551.355    ± 246.881  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4395.511     ± 76.243  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      29249.331   ± 1010.458  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      30714.065    ± 452.915  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     490606.776   ± 8104.150  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     494647.893   ± 9726.051  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     514722.721  ± 15239.077  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     513169.980   ± 6743.332  ops/s
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
