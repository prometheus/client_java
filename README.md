# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-02-02T04:29:04Z
- **Commit:** [`c1adde1`](https://github.com/prometheus/client_java/commit/c1adde10a7ee27a48e4a45a6be6e29ed0d096dcf)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 31.59K | ± 39.51 | ops/s | **fastest** |
| prometheusNoLabelsInc | 31.41K | ± 257.79 | ops/s | 1.0x slower |
| codahaleIncNoLabels | 29.34K | ± 1.04K | ops/s | 1.1x slower |
| prometheusAdd | 28.29K | ± 195.68 | ops/s | 1.1x slower |
| simpleclientInc | 7.04K | ± 130.46 | ops/s | 4.5x slower |
| simpleclientNoLabelsInc | 6.91K | ± 269.95 | ops/s | 4.6x slower |
| simpleclientAdd | 6.63K | ± 239.87 | ops/s | 4.8x slower |
| openTelemetryIncNoLabels | 1.51K | ± 86.43 | ops/s | 21x slower |
| openTelemetryInc | 1.39K | ± 38.75 | ops/s | 23x slower |
| openTelemetryAdd | 1.35K | ± 33.99 | ops/s | 23x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.49K | ± 60.64 | ops/s | **fastest** |
| prometheusClassic | 3.20K | ± 147.19 | ops/s | 1.4x slower |
| prometheusNative | 2.09K | ± 190.54 | ops/s | 2.2x slower |
| openTelemetryClassic | 469.42 | ± 33.84 | ops/s | 9.6x slower |
| openTelemetryExponential | 397.49 | ± 8.74 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 335.67K | ± 2.51K | ops/s | **fastest** |
| prometheusWriteToByteArray | 330.36K | ± 3.76K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 307.15K | ± 1.68K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 305.73K | ± 2.46K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      29338.283   ± 1042.774  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1348.114     ± 33.988  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1389.548     ± 38.750  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1505.486     ± 86.430  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28290.687    ± 195.681  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31589.580     ± 39.512  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      31413.033    ± 257.785  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6629.974    ± 239.874  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       7041.191    ± 130.460  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6907.029    ± 269.948  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        469.423     ± 33.842  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        397.486      ± 8.745  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3201.047    ± 147.187  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2086.835    ± 190.541  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4493.842     ± 60.642  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     305726.534   ± 2462.784  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     307149.360   ± 1679.069  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     330362.378   ± 3763.991  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     335665.116   ± 2512.991  ops/s
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
