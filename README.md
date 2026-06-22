# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-22T04:41:25Z
- **Commit:** [`da14412`](https://github.com/prometheus/client_java/commit/da144125367c0410df66120631f6cae5ead25fcb)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.80K | ± 1.23K | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.61K | ± 887.47 | ops/s | 1.2x slower |
| prometheusAdd | 48.21K | ± 1.40K | ops/s | 1.2x slower |
| codahaleIncNoLabels | 43.91K | ± 1.08K | ops/s | 1.4x slower |
| simpleclientInc | 6.16K | ± 56.58 | ops/s | 9.7x slower |
| simpleclientAdd | 6.11K | ± 70.93 | ops/s | 9.8x slower |
| simpleclientNoLabelsInc | 5.83K | ± 71.15 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 5.71K | ± 803.70 | ops/s | 10x slower |
| openTelemetryInc | 3.98K | ± 335.26 | ops/s | 15x slower |
| openTelemetryAdd | 3.85K | ± 940.88 | ops/s | 16x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.41K | ± 79.99 | ops/s | **fastest** |
| prometheusClassic | 4.35K | ± 533.24 | ops/s | 1.0x slower |
| prometheusNative | 2.96K | ± 234.76 | ops/s | 1.5x slower |
| openTelemetryClassic | 732.76 | ± 2.59 | ops/s | 6.0x slower |
| openTelemetryExponential | 549.25 | ± 28.40 | ops/s | 8.0x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.46K | ± 106.42 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.37K | ± 106.06 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 552.91K | ± 12.39K | ops/s | **fastest** |
| prometheusWriteToByteArray | 547.57K | ± 7.84K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 530.97K | ± 5.16K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 512.91K | ± 3.12K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43909.139   ± 1079.789  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3851.468    ± 940.883  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3976.624    ± 335.259  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       5705.692    ± 803.695  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      48211.722   ± 1395.831  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59796.700   ± 1227.395  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51608.018    ± 887.469  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6106.792     ± 70.929  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6156.902     ± 56.584  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5834.098     ± 71.153  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        732.758      ± 2.595  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        549.246     ± 28.400  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4350.269    ± 533.241  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2964.201    ± 234.765  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4406.010     ± 79.993  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27369.818    ± 106.065  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27459.323    ± 106.417  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     512905.438   ± 3120.609  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     530974.331   ± 5156.478  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     547570.220   ± 7843.250  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     552911.099  ± 12389.795  ops/s
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
