# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-03-04T04:21:38Z
- **Commit:** [`b9906c1`](https://github.com/prometheus/client_java/commit/b9906c11d6b9125b642ffbe6527dfe727880090b)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.14.0-1017-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.80K | ± 1.84K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.11K | ± 417.28 | ops/s | 1.2x slower |
| prometheusAdd | 51.75K | ± 57.95 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.61K | ± 2.40K | ops/s | 1.4x slower |
| simpleclientNoLabelsInc | 6.57K | ± 179.92 | ops/s | 10x slower |
| simpleclientInc | 6.52K | ± 57.64 | ops/s | 10x slower |
| simpleclientAdd | 6.13K | ± 86.69 | ops/s | 11x slower |
| openTelemetryAdd | 1.30K | ± 12.74 | ops/s | 51x slower |
| openTelemetryInc | 1.26K | ± 33.34 | ops/s | 52x slower |
| openTelemetryIncNoLabels | 1.25K | ± 48.08 | ops/s | 52x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.89K | ± 1.89K | ops/s | **fastest** |
| simpleclient | 4.53K | ± 32.43 | ops/s | 1.5x slower |
| prometheusNative | 2.83K | ± 370.99 | ops/s | 2.4x slower |
| openTelemetryClassic | 688.82 | ± 27.45 | ops/s | 10x slower |
| openTelemetryExponential | 566.99 | ± 45.98 | ops/s | 12x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 505.76K | ± 5.82K | ops/s | **fastest** |
| prometheusWriteToByteArray | 496.84K | ± 8.01K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 495.54K | ± 2.74K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 491.66K | ± 6.23K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48605.119   ± 2397.881  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1296.351     ± 12.744  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1259.141     ± 33.344  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1254.621     ± 48.079  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51745.061     ± 57.948  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65801.748   ± 1840.273  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57108.929    ± 417.282  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6126.719     ± 86.691  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6516.020     ± 57.641  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6569.058    ± 179.916  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        688.822     ± 27.452  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        566.986     ± 45.981  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6891.581   ± 1889.173  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2834.625    ± 370.992  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4530.582     ± 32.425  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     491661.795   ± 6225.009  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     495542.548   ± 2742.482  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     496844.423   ± 8008.574  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     505760.818   ± 5816.885  ops/s
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
