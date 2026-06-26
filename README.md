# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-26T04:37:50Z
- **Commit:** [`08cf925`](https://github.com/prometheus/client_java/commit/08cf925b564247a497437e29e4a64ebb335cd328)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.79K | ± 67.69 | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.77K | ± 373.96 | ops/s | 1.2x slower |
| prometheusAdd | 51.07K | ± 689.81 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 47.62K | ± 2.10K | ops/s | 1.4x slower |
| simpleclientInc | 6.57K | ± 39.14 | ops/s | 10x slower |
| simpleclientAdd | 6.48K | ± 49.37 | ops/s | 10x slower |
| simpleclientNoLabelsInc | 6.37K | ± 29.77 | ops/s | 10x slower |
| openTelemetryIncNoLabels | 3.34K | ± 253.99 | ops/s | 20x slower |
| openTelemetryInc | 2.92K | ± 196.23 | ops/s | 23x slower |
| openTelemetryAdd | 2.85K | ± 198.17 | ops/s | 23x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 4.90K | ± 1.08K | ops/s | **fastest** |
| simpleclient | 4.34K | ± 49.61 | ops/s | 1.1x slower |
| prometheusNative | 3.05K | ± 342.77 | ops/s | 1.6x slower |
| openTelemetryClassic | 747.91 | ± 11.61 | ops/s | 6.6x slower |
| openTelemetryExponential | 587.47 | ± 72.64 | ops/s | 8.3x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| openMetricsWriteToNull | 24.43K | ± 331.03 | ops/s | **fastest** |
| prometheusWriteToNull | 23.77K | ± 366.02 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 512.33K | ± 9.23K | ops/s | **fastest** |
| prometheusWriteToByteArray | 504.90K | ± 3.31K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 487.74K | ± 3.05K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 480.77K | ± 7.79K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47619.184   ± 2099.899  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2852.261    ± 198.175  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2921.836    ± 196.228  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3336.650    ± 253.989  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51067.018    ± 689.807  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65791.651     ± 67.692  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56770.406    ± 373.964  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6484.325     ± 49.366  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6566.594     ± 39.137  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6373.239     ± 29.770  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        747.907     ± 11.606  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        587.468     ± 72.640  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       4901.719   ± 1081.375  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3046.570    ± 342.769  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4344.545     ± 49.611  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      24428.936    ± 331.025  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      23766.051    ± 366.017  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     480766.407   ± 7787.188  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     487744.514   ± 3045.716  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     504896.686   ± 3310.375  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     512329.894   ± 9225.785  ops/s
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
