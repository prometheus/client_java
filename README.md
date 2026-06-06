# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-06T04:35:51Z
- **Commit:** [`de73848`](https://github.com/prometheus/client_java/commit/de738487b85e8f85d8d3d79c54b8d05b739a7e42)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1015-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.18K | ± 264.05 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.69K | ± 50.89 | ops/s | 1.1x slower |
| prometheusAdd | 49.31K | ± 589.06 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 43.99K | ± 205.25 | ops/s | 1.3x slower |
| simpleclientInc | 6.12K | ± 58.75 | ops/s | 9.7x slower |
| simpleclientAdd | 6.08K | ± 7.59 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 5.86K | ± 96.64 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 4.62K | ± 936.05 | ops/s | 13x slower |
| openTelemetryInc | 4.60K | ± 965.92 | ops/s | 13x slower |
| openTelemetryAdd | 3.74K | ± 853.94 | ops/s | 16x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.50K | ± 53.33 | ops/s | **fastest** |
| prometheusClassic | 4.27K | ± 667.89 | ops/s | 1.1x slower |
| prometheusNative | 3.06K | ± 119.62 | ops/s | 1.5x slower |
| openTelemetryClassic | 698.96 | ± 34.92 | ops/s | 6.4x slower |
| openTelemetryExponential | 573.61 | ± 44.21 | ops/s | 7.8x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.47K | ± 234.95 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.13K | ± 282.62 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 561.53K | ± 4.00K | ops/s | **fastest** |
| prometheusWriteToByteArray | 550.40K | ± 6.17K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 529.54K | ± 3.74K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 513.44K | ± 11.54K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      43992.996    ± 205.254  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3741.581    ± 853.936  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       4601.117    ± 965.920  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4622.752    ± 936.047  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      49307.128    ± 589.055  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59176.377    ± 264.048  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51686.753     ± 50.892  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6077.433      ± 7.587  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6124.721     ± 58.755  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       5862.459     ± 96.645  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        698.961     ± 34.919  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        573.612     ± 44.212  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4265.517    ± 667.886  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3063.578    ± 119.623  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4497.077     ± 53.326  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27132.191    ± 282.624  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27470.046    ± 234.946  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     513437.161  ± 11543.988  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     529542.021   ± 3737.962  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     550396.539   ± 6165.386  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     561526.519   ± 4003.026  ops/s
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
