# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-05-26T04:38:26Z
- **Commit:** [`5ee188f`](https://github.com/prometheus/client_java/commit/5ee188ff288806f76e53a89d32431a93bb53da11)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 9V74 80-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1013-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 58.45K | ± 964.69 | ops/s | **fastest** |
| prometheusNoLabelsInc | 51.47K | ± 582.64 | ops/s | 1.1x slower |
| prometheusAdd | 46.98K | ± 2.17K | ops/s | 1.2x slower |
| codahaleIncNoLabels | 42.13K | ± 1.57K | ops/s | 1.4x slower |
| simpleclientAdd | 6.14K | ± 103.34 | ops/s | 9.5x slower |
| simpleclientInc | 6.08K | ± 198.19 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 6.05K | ± 199.48 | ops/s | 9.7x slower |
| openTelemetryInc | 5.25K | ± 1.12K | ops/s | 11x slower |
| openTelemetryIncNoLabels | 4.82K | ± 860.30 | ops/s | 12x slower |
| openTelemetryAdd | 3.27K | ± 181.20 | ops/s | 18x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 6.77K | ± 935.88 | ops/s | **fastest** |
| simpleclient | 4.35K | ± 63.21 | ops/s | 1.6x slower |
| prometheusNative | 3.14K | ± 24.80 | ops/s | 2.2x slower |
| openTelemetryClassic | 727.91 | ± 15.90 | ops/s | 9.3x slower |
| openTelemetryExponential | 587.57 | ± 44.41 | ops/s | 12x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 27.52K | ± 158.60 | ops/s | **fastest** |
| openMetricsWriteToNull | 27.19K | ± 303.78 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 581.94K | ± 5.66K | ops/s | **fastest** |
| prometheusWriteToByteArray | 566.69K | ± 5.61K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 549.44K | ± 2.57K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 534.72K | ± 4.10K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      42131.082   ± 1571.209  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3272.036    ± 181.196  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       5254.096   ± 1117.367  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       4820.048    ± 860.305  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      46981.312   ± 2167.226  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      58448.134    ± 964.691  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      51468.628    ± 582.645  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6139.287    ± 103.340  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6079.529    ± 198.192  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6046.614    ± 199.481  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        727.915     ± 15.903  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        587.571     ± 44.410  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       6769.556    ± 935.884  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3136.966     ± 24.805  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4354.040     ± 63.206  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      27188.358    ± 303.781  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      27517.487    ± 158.595  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     534719.907   ± 4097.429  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     549440.566   ± 2569.575  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     566693.401   ± 5611.076  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     581943.730   ± 5658.695  ops/s
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
