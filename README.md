# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-21T04:32:00Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 66.71K | ± 672.93 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.71K | ± 314.49 | ops/s | 1.2x slower |
| prometheusAdd | 51.17K | ± 427.19 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.95K | ± 514.30 | ops/s | 1.3x slower |
| simpleclientInc | 6.62K | ± 41.50 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.53K | ± 150.91 | ops/s | 10x slower |
| simpleclientAdd | 6.34K | ± 185.39 | ops/s | 11x slower |
| openTelemetryAdd | 1.43K | ± 309.51 | ops/s | 47x slower |
| openTelemetryInc | 1.29K | ± 46.00 | ops/s | 52x slower |
| openTelemetryIncNoLabels | 1.19K | ± 46.38 | ops/s | 56x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.82K | ± 1.32K | ops/s | **fastest** |
| simpleclient | 4.43K | ± 15.29 | ops/s | 1.3x slower |
| prometheusNative | 3.00K | ± 300.51 | ops/s | 1.9x slower |
| openTelemetryClassic | 705.89 | ± 17.79 | ops/s | 8.2x slower |
| openTelemetryExponential | 582.63 | ± 28.56 | ops/s | 10.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToByteArray | 477.46K | ± 3.70K | ops/s | **fastest** |
| prometheusWriteToNull | 467.17K | ± 9.91K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 461.44K | ± 7.30K | ops/s | 1.0x slower |
| prometheusWriteToByteArray | 457.34K | ± 5.14K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49946.887    ± 514.299  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1431.484    ± 309.511  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1291.782     ± 45.996  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1194.929     ± 46.383  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51174.665    ± 427.192  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      66709.393    ± 672.930  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56712.656    ± 314.487  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6344.254    ± 185.386  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6623.222     ± 41.499  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6526.023    ± 150.912  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        705.893     ± 17.790  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        582.631     ± 28.564  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5820.877   ± 1315.329  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3004.760    ± 300.506  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4428.770     ± 15.289  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     477464.604   ± 3696.767  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     461442.598   ± 7301.913  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     457341.922   ± 5136.752  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     467166.675   ± 9907.125  ops/s
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
