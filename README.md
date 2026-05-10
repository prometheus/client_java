# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-10T04:36:25Z
- **Commit:** [`11cb921`](https://github.com/prometheus/client_java/commit/11cb921cdea4789cf86ca903867ce9e3e5debe9e)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 58.99K | ± 787.40 | ops/s | **fastest** |
| prometheusNoLabelsInc | 52.44K | ± 525.96 | ops/s | 1.1x slower |
| prometheusAdd | 46.76K | ± 4.78K | ops/s | 1.3x slower |
| codahaleIncNoLabels | 44.13K | ± 304.51 | ops/s | 1.3x slower |
| simpleclientInc | 6.13K | ± 59.21 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 5.93K | ± 22.17 | ops/s | 10.0x slower |
| simpleclientAdd | 5.87K | ± 545.18 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 4.63K | ± 1.09K | ops/s | 13x slower |
| openTelemetryAdd | 4.49K | ± 859.13 | ops/s | 13x slower |
| openTelemetryInc | 4.30K | ± 479.71 | ops/s | 14x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.14K | ± 1.78K | ops/s | **fastest** |
| simpleclient | 4.30K | ± 85.16 | ops/s | 1.4x slower |
| prometheusNative | 2.89K | ± 209.00 | ops/s | 2.1x slower |
| openTelemetryClassic | 714.20 | ± 9.83 | ops/s | 8.6x slower |
| openTelemetryExponential | 543.16 | ± 23.18 | ops/s | 11x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.45K | ± 181.46 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.27K | ± 256.61 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 585.81K | ± 1.10K | ops/s | **fastest** |
| prometheusWriteToByteArray | 560.16K | ± 7.69K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 547.31K | ± 2.56K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 534.58K | ± 3.96K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44133.488    ± 304.508  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       4490.724    ± 859.127  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4295.302    ± 479.707  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4631.385   ± 1088.389  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      46758.082   ± 4775.127  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      58994.520    ± 787.396  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      52442.546    ± 525.959  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5865.505    ± 545.183  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6129.672     ± 59.212  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5925.503     ± 22.170  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        714.196      ± 9.832  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        543.162     ± 23.177  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6143.262   ± 1783.445  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2887.893    ± 209.005  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4300.983     ± 85.158  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27272.158    ± 256.605  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27452.611    ± 181.463  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     534581.246   ± 3964.979  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     547306.877   ± 2559.618  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     560156.845   ± 7690.598  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     585807.839   ± 1100.075  ops/s
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
