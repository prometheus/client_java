# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-12T04:25:00Z
- **Commit:** [`e854af4`](https://github.com/prometheus/client_java/commit/e854af48392c5ad5535a153bafa62253d2dced24)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.40K | ± 1.76K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.05K | ± 350.57 | ops/s | 1.1x slower |
| prometheusAdd | 51.05K | ± 756.31 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 50.04K | ± 503.04 | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.54K | ± 96.30 | ops/s | 10.0x slower |
| simpleclientInc | 6.48K | ± 25.26 | ops/s | 10x slower |
| simpleclientAdd | 6.15K | ± 60.08 | ops/s | 11x slower |
| openTelemetryAdd | 1.33K | ± 7.44 | ops/s | 49x slower |
| openTelemetryInc | 1.28K | ± 37.68 | ops/s | 51x slower |
| openTelemetryIncNoLabels | 1.18K | ± 18.14 | ops/s | 55x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.88K | ± 873.42 | ops/s | **fastest** |
| simpleclient | 4.55K | ± 42.38 | ops/s | 1.1x slower |
| prometheusNative | 3.06K | ± 278.58 | ops/s | 1.6x slower |
| openTelemetryClassic | 665.48 | ± 34.57 | ops/s | 7.3x slower |
| openTelemetryExponential | 556.03 | ± 3.71 | ops/s | 8.8x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 486.78K | ± 5.92K | ops/s | **fastest** |
| prometheusWriteToByteArray | 486.51K | ± 5.04K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 478.79K | ± 10.95K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 468.36K | ± 10.11K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50037.075    ± 503.043  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1326.563      ± 7.442  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1281.155     ± 37.678  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1183.978     ± 18.139  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51048.818    ± 756.313  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65398.729   ± 1764.299  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57051.430    ± 350.569  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6147.644     ± 60.081  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6483.196     ± 25.257  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6544.332     ± 96.297  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        665.477     ± 34.566  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        556.030      ± 3.711  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4880.364    ± 873.417  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3063.564    ± 278.577  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4554.015     ± 42.377  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     468359.086  ± 10111.224  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     478788.561  ± 10953.688  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     486506.545   ± 5042.155  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     486782.669   ± 5924.613  ops/s
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
