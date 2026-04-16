# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-16T04:32:14Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 77.94K | ± 1.01K | ops/s | **fastest** |
| prometheusNoLabelsInc | 66.80K | ± 1.12K | ops/s | 1.2x slower |
| prometheusAdd | 59.58K | ± 6.06K | ops/s | 1.3x slower |
| codahaleIncNoLabels | 57.07K | ± 424.41 | ops/s | 1.4x slower |
| simpleclientInc | 8.17K | ± 51.14 | ops/s | 9.5x slower |
| simpleclientNoLabelsInc | 7.58K | ± 170.05 | ops/s | 10x slower |
| simpleclientAdd | 7.55K | ± 360.33 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 1.83K | ± 91.08 | ops/s | 43x slower |
| openTelemetryAdd | 1.72K | ± 52.99 | ops/s | 45x slower |
| openTelemetryInc | 1.69K | ± 129.01 | ops/s | 46x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.50K | ± 775.30 | ops/s | **fastest** |
| simpleclient | 5.55K | ± 249.55 | ops/s | 1.2x slower |
| prometheusNative | 3.53K | ± 117.63 | ops/s | 1.8x slower |
| openTelemetryClassic | 828.60 | ± 39.98 | ops/s | 7.8x slower |
| openTelemetryExponential | 677.39 | ± 13.37 | ops/s | 9.6x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 672.85K | ± 6.15K | ops/s | **fastest** |
| prometheusWriteToByteArray | 656.66K | ± 6.73K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 644.00K | ± 7.49K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 637.41K | ± 4.91K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      57068.741    ± 424.415  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1722.435     ± 52.986  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1694.816    ± 129.008  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1832.661     ± 91.079  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      59576.742   ± 6057.244  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      77942.123   ± 1013.311  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      66802.747   ± 1123.782  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       7548.125    ± 360.332  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       8171.980     ± 51.140  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       7575.347    ± 170.047  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        828.596     ± 39.982  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        677.389     ± 13.372  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6503.774    ± 775.302  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3530.843    ± 117.626  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       5553.586    ± 249.545  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     637409.196   ± 4910.177  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     644001.000   ± 7485.822  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     656657.552   ± 6728.476  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     672847.885   ± 6147.549  ops/s
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
