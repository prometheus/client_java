# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-08T04:40:57Z
- **Commit:** [`de73848`](https://github.com/prometheus/client_java/commit/de738487b85e8f85d8d3d79c54b8d05b739a7e42)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 77.36K | ± 110.20 | ops/s | **fastest** |
| prometheusNoLabelsInc | 66.64K | ± 983.11 | ops/s | 1.2x slower |
| prometheusAdd | 62.72K | ± 1.42K | ops/s | 1.2x slower |
| codahaleIncNoLabels | 57.44K | ± 778.63 | ops/s | 1.3x slower |
| simpleclientInc | 8.00K | ± 10.10 | ops/s | 9.7x slower |
| simpleclientAdd | 7.57K | ± 223.75 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 7.56K | ± 50.34 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 7.15K | ± 1.48K | ops/s | 11x slower |
| openTelemetryInc | 6.26K | ± 1.16K | ops/s | 12x slower |
| openTelemetryAdd | 5.84K | ± 971.73 | ops/s | 13x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.34K | ± 1.16K | ops/s | **fastest** |
| simpleclient | 5.43K | ± 29.90 | ops/s | 1.2x slower |
| prometheusNative | 3.70K | ± 293.40 | ops/s | 1.7x slower |
| openTelemetryClassic | 934.95 | ± 14.18 | ops/s | 6.8x slower |
| openTelemetryExponential | 724.35 | ± 37.44 | ops/s | 8.8x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 35.28K | ± 411.99 | ops/s | **fastest** |
| openMetricsWriteToNull | 34.68K | ± 601.58 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 712.74K | ± 3.68K | ops/s | **fastest** |
| prometheusWriteToByteArray | 697.99K | ± 3.59K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 666.43K | ± 8.58K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 646.13K | ± 1.91K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      57436.604    ± 778.626  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       5843.743    ± 971.727  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       6262.624   ± 1164.302  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       7151.737   ± 1483.450  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      62716.389   ± 1422.084  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      77362.651    ± 110.198  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66637.054    ± 983.112  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7570.631    ± 223.754  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       8003.155     ± 10.099  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7559.796     ± 50.335  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        934.952     ± 14.179  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        724.353     ± 37.439  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6338.199   ± 1162.281  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3697.104    ± 293.403  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5428.039     ± 29.899  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      34684.310    ± 601.585  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      35284.589    ± 411.988  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     646125.039   ± 1909.718  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     666429.913   ± 8581.082  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     697986.329   ± 3593.256  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     712735.514   ± 3681.673  ops/s
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
