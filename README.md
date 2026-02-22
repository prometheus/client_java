# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-22T04:27:20Z
- **Commit:** [`f645a80`](https://github.com/prometheus/client_java/commit/f645a80f239985098f703c3a542ba534e28e04de)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.24K | ± 791.42 | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.14K | ± 474.66 | ops/s | 1.1x slower |
| prometheusAdd | 51.13K | ± 250.73 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.57K | ± 1.81K | ops/s | 1.3x slower |
| simpleclientInc | 6.76K | ± 69.40 | ops/s | 9.5x slower |
| simpleclientNoLabelsInc | 6.59K | ± 105.60 | ops/s | 9.7x slower |
| simpleclientAdd | 6.39K | ± 228.34 | ops/s | 10x slower |
| openTelemetryAdd | 1.29K | ± 33.09 | ops/s | 50x slower |
| openTelemetryInc | 1.24K | ± 27.29 | ops/s | 52x slower |
| openTelemetryIncNoLabels | 1.20K | ± 35.80 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.36K | ± 1.44K | ops/s | **fastest** |
| simpleclient | 4.56K | ± 71.16 | ops/s | 1.2x slower |
| prometheusNative | 3.10K | ± 234.09 | ops/s | 1.7x slower |
| openTelemetryClassic | 738.73 | ± 41.03 | ops/s | 7.3x slower |
| openTelemetryExponential | 547.32 | ± 16.89 | ops/s | 9.8x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 499.10K | ± 5.92K | ops/s | **fastest** |
| prometheusWriteToByteArray | 496.43K | ± 1.30K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 490.06K | ± 1.85K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 487.77K | ± 5.15K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49573.804   ± 1808.668  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1288.213     ± 33.092  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1236.325     ± 27.294  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1196.237     ± 35.797  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51130.492    ± 250.734  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64241.562    ± 791.415  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57140.098    ± 474.664  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6391.833    ± 228.335  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6760.484     ± 69.395  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6590.927    ± 105.601  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        738.734     ± 41.030  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        547.322     ± 16.887  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5362.681   ± 1444.129  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3098.440    ± 234.086  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4556.111     ± 71.165  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     487773.129   ± 5146.395  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     490056.893   ± 1851.726  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     496430.853   ± 1304.074  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     499098.054   ± 5920.032  ops/s
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
