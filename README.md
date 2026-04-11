# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-11T04:28:18Z
- **Commit:** [`4b69f40`](https://github.com/prometheus/client_java/commit/4b69f40bd4e616d69468ce99dc4323162287a577)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 62.86K | ± 3.83K | ops/s | **fastest** |
| prometheusNoLabelsInc | 57.15K | ± 111.57 | ops/s | 1.1x slower |
| prometheusAdd | 50.65K | ± 276.25 | ops/s | 1.2x slower |
| codahaleIncNoLabels | 49.07K | ± 2.20K | ops/s | 1.3x slower |
| simpleclientNoLabelsInc | 6.61K | ± 7.91 | ops/s | 9.5x slower |
| simpleclientInc | 6.58K | ± 164.43 | ops/s | 9.5x slower |
| simpleclientAdd | 6.33K | ± 235.72 | ops/s | 9.9x slower |
| openTelemetryInc | 1.28K | ± 106.36 | ops/s | 49x slower |
| openTelemetryAdd | 1.25K | ± 42.57 | ops/s | 50x slower |
| openTelemetryIncNoLabels | 1.19K | ± 121.89 | ops/s | 53x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.91K | ± 2.27K | ops/s | **fastest** |
| simpleclient | 4.48K | ± 42.99 | ops/s | 1.3x slower |
| prometheusNative | 3.05K | ± 300.57 | ops/s | 1.9x slower |
| openTelemetryClassic | 676.41 | ± 68.89 | ops/s | 8.7x slower |
| openTelemetryExponential | 549.40 | ± 19.62 | ops/s | 11x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 494.80K | ± 2.88K | ops/s | **fastest** |
| prometheusWriteToByteArray | 490.09K | ± 2.44K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 480.99K | ± 4.18K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 476.89K | ± 1.81K | ops/s | 1.0x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49070.658   ± 2196.432  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1252.607     ± 42.569  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1276.891    ± 106.358  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1189.318    ± 121.891  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      50654.699    ± 276.249  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      62859.374   ± 3825.845  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      57153.494    ± 111.567  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6332.914    ± 235.718  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6582.393    ± 164.430  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6614.626      ± 7.913  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        676.413     ± 68.890  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        549.396     ± 19.625  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5906.053   ± 2274.519  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3054.143    ± 300.573  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4484.389     ± 42.989  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     476889.069   ± 1811.293  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     480989.082   ± 4182.150  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     490087.239   ± 2436.809  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     494801.015   ± 2881.845  ops/s
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
