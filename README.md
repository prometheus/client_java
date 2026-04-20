# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-20T04:32:54Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 59.60K | ± 506.77 | ops/s | **fastest** |
| prometheusAdd | 48.00K | ± 635.65 | ops/s | 1.2x slower |
| prometheusNoLabelsInc | 44.64K | ± 9.13K | ops/s | 1.3x slower |
| codahaleIncNoLabels | 44.13K | ± 417.69 | ops/s | 1.4x slower |
| simpleclientInc | 6.33K | ± 48.96 | ops/s | 9.4x slower |
| simpleclientNoLabelsInc | 6.06K | ± 336.51 | ops/s | 9.8x slower |
| simpleclientAdd | 5.95K | ± 325.66 | ops/s | 10x slower |
| openTelemetryInc | 1.46K | ± 73.97 | ops/s | 41x slower |
| openTelemetryIncNoLabels | 1.40K | ± 129.41 | ops/s | 43x slower |
| openTelemetryAdd | 1.38K | ± 33.60 | ops/s | 43x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.28K | ± 1.66K | ops/s | **fastest** |
| simpleclient | 4.42K | ± 49.89 | ops/s | 1.2x slower |
| prometheusNative | 2.87K | ± 170.20 | ops/s | 1.8x slower |
| openTelemetryClassic | 641.18 | ± 24.19 | ops/s | 8.2x slower |
| openTelemetryExponential | 550.66 | ± 4.45 | ops/s | 9.6x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 559.85K | ± 8.14K | ops/s | **fastest** |
| prometheusWriteToByteArray | 543.35K | ± 3.65K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 537.82K | ± 8.35K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 529.86K | ± 6.08K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      44130.621    ± 417.692  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1381.309     ± 33.599  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1461.873     ± 73.967  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1399.856    ± 129.407  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      47996.212    ± 635.652  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      59600.469    ± 506.769  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      44640.140   ± 9131.535  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       5948.601    ± 325.664  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6332.691     ± 48.963  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6061.300    ± 336.513  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        641.182     ± 24.189  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        550.660      ± 4.451  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5280.371   ± 1659.132  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2868.450    ± 170.203  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4416.754     ± 49.890  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     529861.582   ± 6079.035  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     537824.225   ± 8353.893  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     543349.030   ± 3653.433  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     559853.430   ± 8137.574  ops/s
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
