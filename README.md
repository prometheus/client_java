# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-10T04:38:05Z
- **Commit:** [`565d168`](https://github.com/prometheus/client_java/commit/565d168cac045b3e7516104b3a22af3bc0014832)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 31.12K | ± 1.13K | ops/s | **fastest** |
| prometheusNoLabelsInc | 30.00K | ± 1.68K | ops/s | 1.0x slower |
| codahaleIncNoLabels | 29.32K | ± 1.83K | ops/s | 1.1x slower |
| prometheusAdd | 28.47K | ± 11.68 | ops/s | 1.1x slower |
| simpleclientInc | 6.82K | ± 203.84 | ops/s | 4.6x slower |
| simpleclientNoLabelsInc | 6.66K | ± 20.54 | ops/s | 4.7x slower |
| simpleclientAdd | 6.48K | ± 179.42 | ops/s | 4.8x slower |
| openTelemetryIncNoLabels | 2.73K | ± 281.89 | ops/s | 11x slower |
| openTelemetryInc | 2.61K | ± 86.13 | ops/s | 12x slower |
| openTelemetryAdd | 2.24K | ± 115.22 | ops/s | 14x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.48K | ± 67.01 | ops/s | **fastest** |
| prometheusClassic | 2.90K | ± 507.74 | ops/s | 1.5x slower |
| prometheusNative | 2.23K | ± 366.50 | ops/s | 2.0x slower |
| openTelemetryClassic | 621.75 | ± 34.61 | ops/s | 7.2x slower |
| openTelemetryExponential | 455.42 | ± 67.09 | ops/s | 9.8x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 18.28K | ± 114.18 | ops/s | **fastest** |
| openMetricsWriteToNull | 18.21K | ± 70.68 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 323.47K | ± 1.28K | ops/s | **fastest** |
| prometheusWriteToByteArray | 321.51K | ± 1.07K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 300.91K | ± 1.27K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 298.47K | ± 1.50K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29323.688   ± 1826.726  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2239.808    ± 115.223  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2605.986     ± 86.135  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2727.422    ± 281.891  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28472.056     ± 11.681  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31122.365   ± 1130.248  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      29996.818   ± 1675.394  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6483.760    ± 179.421  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6817.813    ± 203.838  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6658.667     ± 20.543  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        621.749     ± 34.607  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        455.417     ± 67.094  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       2900.262    ± 507.740  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2228.736    ± 366.501  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4480.828     ± 67.012  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18207.574     ± 70.679  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18277.238    ± 114.181  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     298471.887   ± 1495.936  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     300910.240   ± 1268.759  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     321511.212   ± 1065.363  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     323465.103   ± 1283.459  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |
