# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-17T04:37:05Z
- **Commit:** [`94b33b7`](https://github.com/prometheus/client_java/commit/94b33b7527ce21b12ff2a3f9cd23c63cdb42e274)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 63.47K | ± 2.31K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.25K | ± 961.78 | ops/s | 1.1x slower |
| prometheusAdd | 51.43K | ± 100.91 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 47.28K | ± 630.67 | ops/s | 1.3x slower |
| simpleclientInc | 6.58K | ± 8.84 | ops/s | 9.6x slower |
| simpleclientAdd | 6.47K | ± 21.90 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 6.36K | ± 35.33 | ops/s | 10.0x slower |
| openTelemetryAdd | 3.85K | ± 60.54 | ops/s | 16x slower |
| openTelemetryIncNoLabels | 3.53K | ± 314.72 | ops/s | 18x slower |
| openTelemetryInc | 3.15K | ± 143.43 | ops/s | 20x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.45K | ± 1.13K | ops/s | **fastest** |
| simpleclient | 4.42K | ± 66.86 | ops/s | 1.5x slower |
| prometheusNative | 2.76K | ± 360.68 | ops/s | 2.3x slower |
| openTelemetryClassic | 773.65 | ± 21.90 | ops/s | 8.3x slower |
| openTelemetryExponential | 677.67 | ± 64.78 | ops/s | 9.5x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 23.43K | ± 830.17 | ops/s | **fastest** |
| prometheusWriteToNull | 22.71K | ± 558.30 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 502.07K | ± 5.83K | ops/s | **fastest** |
| prometheusWriteToByteArray | 499.84K | ± 4.65K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 478.85K | ± 4.03K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 478.51K | ± 7.34K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47283.643    ± 630.673  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3849.893     ± 60.536  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3145.213    ± 143.429  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3534.785    ± 314.717  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51428.053    ± 100.910  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      63465.056   ± 2314.262  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56246.350    ± 961.779  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6470.862     ± 21.903  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6578.073      ± 8.841  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6361.376     ± 35.329  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        773.654     ± 21.896  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        677.672     ± 64.779  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6453.030   ± 1126.258  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2757.661    ± 360.679  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4415.642     ± 66.861  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23434.380    ± 830.166  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      22712.167    ± 558.296  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     478853.821   ± 4034.365  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     478505.501   ± 7335.971  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     499844.057   ± 4649.418  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     502066.419   ± 5830.863  ops/s
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
