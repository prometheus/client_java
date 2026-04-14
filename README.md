# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-14T04:31:28Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 64.80K | ± 1.97K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.89K | ± 340.12 | ops/s | 1.1x slower |
| prometheusAdd | 50.17K | ± 1.03K | ops/s | 1.3x slower |
| codahaleIncNoLabels | 48.39K | ± 1.59K | ops/s | 1.3x slower |
| simpleclientInc | 6.69K | ± 15.83 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.47K | ± 250.35 | ops/s | 10x slower |
| simpleclientAdd | 6.18K | ± 237.73 | ops/s | 10x slower |
| openTelemetryInc | 1.48K | ± 72.79 | ops/s | 44x slower |
| openTelemetryAdd | 1.41K | ± 274.60 | ops/s | 46x slower |
| openTelemetryIncNoLabels | 1.24K | ± 51.97 | ops/s | 52x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.27K | ± 1.37K | ops/s | **fastest** |
| simpleclient | 4.45K | ± 69.17 | ops/s | 1.2x slower |
| prometheusNative | 3.01K | ± 301.27 | ops/s | 1.8x slower |
| openTelemetryClassic | 689.78 | ± 19.58 | ops/s | 7.6x slower |
| openTelemetryExponential | 569.95 | ± 16.41 | ops/s | 9.2x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 481.34K | ± 3.90K | ops/s | **fastest** |
| prometheusWriteToNull | 479.45K | ± 4.72K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 476.28K | ± 4.31K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 473.06K | ± 7.82K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      48392.735   ± 1585.691  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1409.040    ± 274.603  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1476.869     ± 72.787  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1243.716     ± 51.975  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50171.623   ± 1034.707  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      64800.518   ± 1972.593  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56894.656    ± 340.125  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6177.160    ± 237.727  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6688.031     ± 15.829  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6466.967    ± 250.350  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        689.783     ± 19.579  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        569.947     ± 16.406  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5270.292   ± 1374.303  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3006.283    ± 301.269  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4450.192     ± 69.174  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     473055.194   ± 7822.892  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     481340.763   ± 3897.729  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     476277.627   ± 4305.825  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     479453.540   ± 4717.576  ops/s
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
