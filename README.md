# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-17T04:25:58Z
- **Commit:** [`1b8f56d`](https://github.com/prometheus/client_java/commit/1b8f56d3f45188829752e0bf3a3558a9228999a5)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.21K | ± 336.21 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.55K | ± 1.13K | ops/s | 1.2x slower |
| prometheusAdd | 51.16K | ± 697.44 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.99K | ± 472.00 | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.68K | ± 35.58 | ops/s | 9.9x slower |
| simpleclientInc | 6.68K | ± 110.12 | ops/s | 9.9x slower |
| simpleclientAdd | 6.44K | ± 172.91 | ops/s | 10x slower |
| openTelemetryAdd | 1.37K | ± 258.56 | ops/s | 48x slower |
| openTelemetryInc | 1.23K | ± 71.13 | ops/s | 54x slower |
| openTelemetryIncNoLabels | 1.20K | ± 60.73 | ops/s | 55x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.60K | ± 1.53K | ops/s | **fastest** |
| simpleclient | 4.52K | ± 33.49 | ops/s | 1.2x slower |
| prometheusNative | 3.01K | ± 227.38 | ops/s | 1.9x slower |
| openTelemetryClassic | 686.72 | ± 85.66 | ops/s | 8.2x slower |
| openTelemetryExponential | 541.65 | ± 31.37 | ops/s | 10x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 478.31K | ± 5.68K | ops/s | **fastest** |
| prometheusWriteToByteArray | 470.36K | ± 9.37K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 461.01K | ± 6.70K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 460.95K | ± 3.10K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49985.886    ± 472.004  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1365.708    ± 258.561  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1225.672     ± 71.125  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1196.011     ± 60.731  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51155.880    ± 697.439  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66205.636    ± 336.209  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56549.091   ± 1126.875  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6437.055    ± 172.915  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6677.101    ± 110.124  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6681.392     ± 35.584  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        686.721     ± 85.663  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        541.647     ± 31.373  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5604.038   ± 1529.559  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3007.017    ± 227.376  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4517.669     ± 33.493  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     460947.539   ± 3101.820  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     461012.427   ± 6702.180  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     470361.241   ± 9365.377  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     478308.480   ± 5677.917  ops/s
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
