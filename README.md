# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-19T04:37:47Z
- **Commit:** [`8c254dd`](https://github.com/prometheus/client_java/commit/8c254dd5ac96f1d53ffc4d59a163c5b1d19f9531)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 60.12K | ± 1.08K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.11K | ± 490.35 | ops/s | 1.2x slower |
| prometheusAdd | 48.00K | ± 818.04 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 43.23K | ± 1.29K | ops/s | 1.4x slower |
| simpleclientInc | 6.16K | ± 60.42 | ops/s | 9.8x slower |
| simpleclientAdd | 6.09K | ± 36.67 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 5.89K | ± 51.47 | ops/s | 10x slower |
| openTelemetryInc | 4.87K | ± 1.14K | ops/s | 12x slower |
| openTelemetryIncNoLabels | 4.72K | ± 629.87 | ops/s | 13x slower |
| openTelemetryAdd | 3.96K | ± 1.17K | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.53K | ± 1.54K | ops/s | **fastest** |
| simpleclient | 4.56K | ± 120.79 | ops/s | 1.2x slower |
| prometheusNative | 2.90K | ± 185.16 | ops/s | 1.9x slower |
| openTelemetryClassic | 715.37 | ± 13.11 | ops/s | 7.7x slower |
| openTelemetryExponential | 541.60 | ± 6.27 | ops/s | 10x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.33K | ± 481.53 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.30K | ± 257.23 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 584.29K | ± 4.62K | ops/s | **fastest** |
| prometheusWriteToByteArray | 566.50K | ± 9.24K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 549.46K | ± 3.57K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 532.21K | ± 2.48K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43228.600   ± 1292.298  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3963.390   ± 1168.145  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4868.186   ± 1141.375  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4718.161    ± 629.869  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      47999.092    ± 818.043  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      60123.616   ± 1082.822  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51111.784    ± 490.348  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6087.650     ± 36.671  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6160.910     ± 60.419  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5891.065     ± 51.467  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        715.366     ± 13.110  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        541.596      ± 6.267  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5530.923   ± 1538.435  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2898.402    ± 185.163  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4559.073    ± 120.786  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27304.946    ± 257.226  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27334.584    ± 481.526  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     532207.360   ± 2483.341  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     549462.620   ± 3568.377  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     566497.496   ± 9240.956  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     584294.657   ± 4617.009  ops/s
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
