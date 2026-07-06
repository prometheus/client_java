# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-07-06T04:35:46Z
- **Commit:** [`c29367d`](https://github.com/prometheus/client_java/commit/c29367daf4e6aec49eecf321b8e41553c2e194d5)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.81K | ± 1.68K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.86K | ± 372.40 | ops/s | 1.2x slower |
| prometheusAdd | 51.26K | ± 125.00 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 50.26K | ± 184.34 | ops/s | 1.3x slower |
| simpleclientInc | 6.65K | ± 97.11 | ops/s | 9.9x slower |
| simpleclientAdd | 6.41K | ± 89.30 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.33K | ± 28.43 | ops/s | 10x slower |
| openTelemetryAdd | 3.35K | ± 324.16 | ops/s | 20x slower |
| openTelemetryIncNoLabels | 3.10K | ± 127.08 | ops/s | 21x slower |
| openTelemetryInc | 3.01K | ± 62.19 | ops/s | 22x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.72K | ± 841.62 | ops/s | **fastest** |
| simpleclient | 4.41K | ± 16.75 | ops/s | 1.1x slower |
| prometheusNative | 3.16K | ± 122.14 | ops/s | 1.5x slower |
| openTelemetryClassic | 798.18 | ± 32.99 | ops/s | 5.9x slower |
| openTelemetryExponential | 620.92 | ± 80.05 | ops/s | 7.6x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 24.02K | ± 391.76 | ops/s | **fastest** |
| prometheusWriteToNull | 23.54K | ± 904.15 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToByteArray | 507.18K | ± 8.71K | ops/s | **fastest** |
| prometheusWriteToNull | 502.93K | ± 5.08K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 482.38K | ± 3.97K | ops/s | 1.1x slower |
| openMetricsWriteToNull | 482.24K | ± 10.39K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50255.266    ± 184.336  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3349.799    ± 324.163  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3009.719     ± 62.187  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3104.335    ± 127.077  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51258.065    ± 124.996  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65808.902   ± 1676.280  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56864.478    ± 372.401  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6405.381     ± 89.299  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6648.830     ± 97.113  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6325.127     ± 28.428  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        798.179     ± 32.985  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        620.923     ± 80.051  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4715.813    ± 841.624  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3158.645    ± 122.138  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4408.428     ± 16.752  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24022.698    ± 391.762  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23535.774    ± 904.149  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     482376.665   ± 3973.188  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     482243.382  ± 10393.748  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     507175.396   ± 8707.863  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     502927.110   ± 5083.750  ops/s
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
