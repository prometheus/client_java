# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-14T04:22:08Z
- **Commit:** [`043fc57`](https://github.com/prometheus/client_java/commit/043fc5742752fdc2f67f0219418030a190c53bde)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.14K | ± 1.36K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.72K | ± 683.83 | ops/s | 1.2x slower |
| prometheusAdd | 51.46K | ± 197.14 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.18K | ± 845.00 | ops/s | 1.4x slower |
| simpleclientInc | 6.71K | ± 137.13 | ops/s | 9.9x slower |
| simpleclientAdd | 6.37K | ± 244.17 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.36K | ± 63.07 | ops/s | 10x slower |
| openTelemetryAdd | 1.30K | ± 77.74 | ops/s | 51x slower |
| openTelemetryInc | 1.24K | ± 76.21 | ops/s | 53x slower |
| openTelemetryIncNoLabels | 1.21K | ± 85.32 | ops/s | 55x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.82K | ± 1.21K | ops/s | **fastest** |
| simpleclient | 4.54K | ± 32.73 | ops/s | 1.3x slower |
| prometheusNative | 2.79K | ± 322.45 | ops/s | 2.1x slower |
| openTelemetryClassic | 714.91 | ± 25.19 | ops/s | 8.1x slower |
| openTelemetryExponential | 525.67 | ± 14.14 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 495.79K | ± 1.72K | ops/s | **fastest** |
| prometheusWriteToByteArray | 490.10K | ± 2.28K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 490.02K | ± 3.95K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 479.70K | ± 8.00K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48177.401    ± 845.000  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1299.826     ± 77.743  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1241.597     ± 76.207  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1205.449     ± 85.323  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51458.338    ± 197.143  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66138.658   ± 1358.984  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56720.622    ± 683.832  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6369.930    ± 244.169  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6707.069    ± 137.129  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6361.368     ± 63.074  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        714.909     ± 25.193  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        525.665     ± 14.142  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5822.024   ± 1209.637  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2792.652    ± 322.453  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4542.301     ± 32.727  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     479704.655   ± 8000.808  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     490022.413   ± 3947.537  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     490103.162   ± 2284.270  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     495785.913   ± 1720.803  ops/s
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
