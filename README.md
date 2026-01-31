# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-01-31T04:18:14Z
- **Commit:** [`421033a`](https://github.com/prometheus/client_java/commit/421033a2f72ef0c52d77fae6eb1b346c81b1fdb3)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.58K | ± 1.70K | ops/s | **fastest** |
| prometheusNoLabelsInc | 55.43K | ± 2.53K | ops/s | 1.2x slower |
| prometheusAdd | 51.48K | ± 158.78 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 50.36K | ± 119.44 | ops/s | 1.3x slower |
| simpleclientInc | 6.78K | ± 21.91 | ops/s | 9.7x slower |
| simpleclientNoLabelsInc | 6.62K | ± 138.80 | ops/s | 9.9x slower |
| simpleclientAdd | 6.28K | ± 232.01 | ops/s | 10x slower |
| openTelemetryAdd | 1.31K | ± 39.98 | ops/s | 50x slower |
| openTelemetryInc | 1.24K | ± 39.70 | ops/s | 53x slower |
| openTelemetryIncNoLabels | 1.21K | ± 32.37 | ops/s | 54x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.35K | ± 387.74 | ops/s | **fastest** |
| simpleclient | 4.59K | ± 23.09 | ops/s | 1.2x slower |
| prometheusNative | 2.99K | ± 129.39 | ops/s | 1.8x slower |
| openTelemetryClassic | 729.70 | ± 44.61 | ops/s | 7.3x slower |
| openTelemetryExponential | 529.64 | ± 19.70 | ops/s | 10x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 537.48K | ± 4.43K | ops/s | **fastest** |
| prometheusWriteToByteArray | 526.68K | ± 7.03K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 510.77K | ± 4.97K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 506.23K | ± 4.41K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      50361.341    ± 119.445  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1311.108     ± 39.978  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1243.989     ± 39.704  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1208.576     ± 32.366  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51483.494    ± 158.785  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65576.021   ± 1704.572  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      55427.419   ± 2530.919  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6279.895    ± 232.008  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6781.589     ± 21.908  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6618.340    ± 138.796  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        729.698     ± 44.614  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        529.644     ± 19.700  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5354.695    ± 387.737  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2993.414    ± 129.395  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4593.905     ± 23.088  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     506233.011   ± 4413.140  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     510773.578   ± 4965.990  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     526679.343   ± 7034.827  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     537475.874   ± 4430.522  ops/s
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
