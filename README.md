# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-08-10T04:18:55Z
- **Commit:** [`565a583`](https://github.com/prometheus/client_java/commit/565a58396c92ddfbe1b64de37c40a0a8c165a612)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1020-azure

## Results for PR head

### CounterBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusInc | 66.01K | ± 356.53 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.30K | ± 1.02K | ops/s | 1.2x slower |
| prometheusAdd | 51.23K | ± 400.21 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.09K | ± 1.44K | ops/s | 1.3x slower |
| openTelemetryIncNoLabels | 18.68K | ± 180.35 | ops/s | 3.5x slower |
| openTelemetryInc | 14.78K | ± 279.43 | ops/s | 4.5x slower |
| openTelemetryAdd | 12.62K | ± 256.47 | ops/s | 5.2x slower |
| simpleclientInc | 6.53K | ± 45.30 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.39K | ± 28.92 | ops/s | 10x slower |
| simpleclientAdd | 6.24K | ± 337.26 | ops/s | 11x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusClassicPerThread | 12.29K | ± 18.41 | ops/s | **fastest** |
| prometheusClassic | 6.87K | ± 2.05K | ops/s | 1.8x slower |
| prometheusClassicSingleThread | 4.59K | ± 30.49 | ops/s | 2.7x slower |
| simpleclient | 4.46K | ± 67.46 | ops/s | 2.8x slower |
| prometheusNative | 2.73K | ± 310.16 | ops/s | 4.5x slower |
| openTelemetryExponential | 968.85 | ± 27.23 | ops/s | 13x slower |
| openTelemetryClassic | 801.85 | ± 32.75 | ops/s | 15x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| openMetricsWriteToNull | 24.10K | ± 416.83 | ops/s | **fastest** |
| prometheusWriteToNull | 23.68K | ± 1.24K | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | Within run |
|:----------|------:|------:|:------|:-----------|
| prometheusWriteToNull | 457.22K | ± 5.01K | ops/s | **fastest** |
| prometheusWriteToByteArray | 452.17K | ± 4.47K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 439.03K | ± 7.14K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 428.09K | ± 6.82K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49091.878   ± 1437.347  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15      12624.464    ± 256.474  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15      14775.898    ± 279.427  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15      18683.847    ± 180.350  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51228.497    ± 400.210  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66005.991    ± 356.532  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56300.645   ± 1022.901  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6238.067    ± 337.262  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6527.915     ± 45.300  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6386.928     ± 28.921  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        801.851     ± 32.749  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        968.846     ± 27.230  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6865.587   ± 2050.928  ops/s
HistogramBenchmark.prometheusClassicPerThread       thrpt   15      12294.264     ± 18.413  ops/s
HistogramBenchmark.prometheusClassicSingleThread    thrpt   15       4591.421     ± 30.490  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2728.933    ± 310.165  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4460.994     ± 67.461  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24102.243    ± 416.829  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23679.075   ± 1240.411  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     428086.169   ± 6821.671  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     439034.683   ± 7140.446  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     452171.475   ± 4465.153  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     457217.298   ± 5013.181  ops/s
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
