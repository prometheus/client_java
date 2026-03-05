# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-05T04:23:38Z
- **Commit:** [`947f5d1`](https://github.com/prometheus/client_java/commit/947f5d13c70d37e356732c179e67c4630f719ca0)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 62.91K | ± 3.58K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.06K | ± 450.65 | ops/s | 1.1x slower |
| prometheusAdd | 51.25K | ± 609.41 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 45.21K | ± 8.60K | ops/s | 1.4x slower |
| simpleclientInc | 6.70K | ± 129.12 | ops/s | 9.4x slower |
| simpleclientNoLabelsInc | 6.68K | ± 12.76 | ops/s | 9.4x slower |
| simpleclientAdd | 6.40K | ± 165.78 | ops/s | 9.8x slower |
| openTelemetryAdd | 1.41K | ± 287.59 | ops/s | 45x slower |
| openTelemetryIncNoLabels | 1.29K | ± 148.88 | ops/s | 49x slower |
| openTelemetryInc | 1.27K | ± 29.36 | ops/s | 50x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.78K | ± 710.18 | ops/s | **fastest** |
| simpleclient | 4.54K | ± 41.00 | ops/s | 1.1x slower |
| prometheusNative | 2.87K | ± 283.48 | ops/s | 1.7x slower |
| openTelemetryClassic | 685.18 | ± 39.22 | ops/s | 7.0x slower |
| openTelemetryExponential | 574.33 | ± 23.43 | ops/s | 8.3x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 499.31K | ± 4.03K | ops/s | **fastest** |
| openMetricsWriteToNull | 491.24K | ± 3.12K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 489.53K | ± 6.88K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 488.55K | ± 1.26K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      45208.815   ± 8598.626  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1406.462    ± 287.590  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1265.477     ± 29.361  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1285.847    ± 148.882  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51245.897    ± 609.408  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      62906.850   ± 3577.974  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57057.119    ± 450.653  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6400.956    ± 165.783  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6696.363    ± 129.117  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6680.639     ± 12.763  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        685.176     ± 39.221  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        574.333     ± 23.433  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4778.571    ± 710.181  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2868.472    ± 283.481  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4538.626     ± 41.001  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     488546.389   ± 1258.737  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     491244.285   ± 3117.834  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     489529.479   ± 6880.555  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     499311.276   ± 4034.850  ops/s
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
