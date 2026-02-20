# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-20T04:24:12Z
- **Commit:** [`0d800d0`](https://github.com/prometheus/client_java/commit/0d800d0a91578e48f34909472c183174fdf1d83e)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 58.31K | ± 3.76K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.15K | ± 510.83 | ops/s | 1.1x slower |
| prometheusAdd | 48.70K | ± 267.98 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 37.83K | ± 9.75K | ops/s | 1.5x slower |
| simpleclientNoLabelsInc | 6.40K | ± 18.43 | ops/s | 9.1x slower |
| simpleclientInc | 6.13K | ± 56.21 | ops/s | 9.5x slower |
| simpleclientAdd | 5.86K | ± 255.65 | ops/s | 9.9x slower |
| openTelemetryAdd | 1.58K | ± 123.68 | ops/s | 37x slower |
| openTelemetryInc | 1.41K | ± 86.17 | ops/s | 41x slower |
| openTelemetryIncNoLabels | 1.29K | ± 66.53 | ops/s | 45x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.08K | ± 550.56 | ops/s | **fastest** |
| simpleclient | 4.61K | ± 36.46 | ops/s | 1.1x slower |
| prometheusNative | 3.06K | ± 244.62 | ops/s | 1.7x slower |
| openTelemetryClassic | 653.73 | ± 23.97 | ops/s | 7.8x slower |
| openTelemetryExponential | 532.59 | ± 7.35 | ops/s | 9.5x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 555.21K | ± 3.97K | ops/s | **fastest** |
| prometheusWriteToByteArray | 540.54K | ± 7.82K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 539.68K | ± 2.93K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 522.76K | ± 4.34K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      37834.702   ± 9752.044  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1576.562    ± 123.680  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1409.016     ± 86.173  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1285.320     ± 66.529  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48704.862    ± 267.981  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      58309.356   ± 3763.626  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51147.253    ± 510.833  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5862.129    ± 255.653  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6133.348     ± 56.210  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6399.813     ± 18.434  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        653.727     ± 23.968  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        532.591      ± 7.350  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5079.209    ± 550.556  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3056.597    ± 244.619  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4605.127     ± 36.457  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     522756.706   ± 4340.503  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     539677.894   ± 2931.500  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     540538.366   ± 7823.692  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     555214.758   ± 3968.697  ops/s
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
