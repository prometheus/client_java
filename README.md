# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-11T04:21:21Z
- **Commit:** [`e854af4`](https://github.com/prometheus/client_java/commit/e854af48392c5ad5535a153bafa62253d2dced24)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.48K | ± 1.30K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.16K | ± 501.79 | ops/s | 1.1x slower |
| prometheusAdd | 51.49K | ± 429.11 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.61K | ± 1.13K | ops/s | 1.3x slower |
| simpleclientInc | 6.77K | ± 20.58 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.44K | ± 181.76 | ops/s | 10x slower |
| simpleclientAdd | 6.20K | ± 21.45 | ops/s | 11x slower |
| openTelemetryIncNoLabels | 1.32K | ± 188.21 | ops/s | 49x slower |
| openTelemetryAdd | 1.27K | ± 39.01 | ops/s | 52x slower |
| openTelemetryInc | 1.23K | ± 33.09 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.97K | ± 686.24 | ops/s | **fastest** |
| simpleclient | 4.52K | ± 24.96 | ops/s | 1.1x slower |
| prometheusNative | 2.77K | ± 330.47 | ops/s | 1.8x slower |
| openTelemetryClassic | 725.05 | ± 29.35 | ops/s | 6.8x slower |
| openTelemetryExponential | 550.32 | ± 23.83 | ops/s | 9.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 486.25K | ± 3.50K | ops/s | **fastest** |
| prometheusWriteToNull | 484.67K | ± 8.98K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 476.47K | ± 6.71K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 475.27K | ± 4.61K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49610.857   ± 1131.512  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1270.060     ± 39.011  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1227.408     ± 33.093  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1323.102    ± 188.210  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51492.721    ± 429.111  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65475.319   ± 1299.558  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57164.265    ± 501.785  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6196.196     ± 21.455  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6768.212     ± 20.584  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6441.328    ± 181.758  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        725.054     ± 29.355  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        550.319     ± 23.834  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4966.180    ± 686.238  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2769.357    ± 330.475  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4521.342     ± 24.962  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     475268.473   ± 4606.498  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     486248.539   ± 3496.586  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     476466.328   ± 6708.376  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     484674.454   ± 8976.707  ops/s
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
