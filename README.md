# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-07T04:33:21Z
- **Commit:** [`565a583`](https://github.com/prometheus/client_java/commit/565a58396c92ddfbe1b64de37c40a0a8c165a612)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 65.55K | ± 1.70K | ops/s | **fastest** |
| prometheusNoLabelsInc | 55.38K | ± 2.42K | ops/s | 1.2x slower |
| prometheusAdd | 51.42K | ± 244.05 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.10K | ± 719.40 | ops/s | 1.4x slower |
| openTelemetryIncNoLabels | 18.50K | ± 101.23 | ops/s | 3.5x slower |
| openTelemetryInc | 14.75K | ± 356.81 | ops/s | 4.4x slower |
| openTelemetryAdd | 12.91K | ± 75.25 | ops/s | 5.1x slower |
| simpleclientInc | 6.48K | ± 94.54 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.37K | ± 30.56 | ops/s | 10x slower |
| simpleclientAdd | 6.27K | ± 272.01 | ops/s | 10x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.27K | ± 21.65 | ops/s | **fastest** |
| prometheusClassic | 5.48K | ± 1.25K | ops/s | 2.2x slower |
| prometheusClassicSingleThread | 4.56K | ± 48.81 | ops/s | 2.7x slower |
| simpleclient | 4.37K | ± 25.03 | ops/s | 2.8x slower |
| prometheusNative | 3.02K | ± 412.94 | ops/s | 4.1x slower |
| openTelemetryClassic | 809.03 | ± 93.28 | ops/s | 15x slower |
| openTelemetryExponential | 742.60 | ± 144.58 | ops/s | 17x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 23.39K | ± 205.57 | ops/s | **fastest** |
| openMetricsWriteToNull | 23.29K | ± 825.25 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 495.29K | ± 4.01K | ops/s | **fastest** |
| prometheusWriteToByteArray | 493.79K | ± 2.58K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 483.28K | ± 3.93K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 472.74K | ± 4.03K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47095.554    ± 719.396  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12912.993     ± 75.248  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14752.911    ± 356.814  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18499.008    ± 101.234  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51420.848    ± 244.049  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65548.862   ± 1703.845  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55376.179   ± 2420.433  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6273.118    ± 272.005  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6478.360     ± 94.544  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6367.140     ± 30.563  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        809.033     ± 93.283  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        742.603    ± 144.577  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5477.974   ± 1251.922  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12273.761     ± 21.646  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4559.435     ± 48.806  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3020.207    ± 412.945  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4371.466     ± 25.028  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      23292.628    ± 825.251  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23393.684    ± 205.569  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     472737.418   ± 4031.123  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     483283.061   ± 3931.252  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     493793.945   ± 2578.792  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     495292.962   ± 4007.076  ops/s
```

## Notes

- **Score** = Throughput in operations per second (higher is better)
- **Error** = 99.9% confidence interval
- **Within run** compares benchmarks in the same result set, not against the base commit.

## Benchmark Descriptions

| Benchmark | Description |
|:----------|:------------|
| **CounterBenchmark** | Counter increment performance: Prometheus, OpenTelemetry, simpleclient, Codahale |
| **HistogramBenchmark** | Histogram observation performance (classic vs native/exponential) |
| **TextFormatUtilBenchmark** | Metric exposition format writing speed |
