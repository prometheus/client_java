# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-06-14T04:40:18Z
- **Commit:** [`9672749`](https://github.com/prometheus/client_java/commit/9672749085f9029ccb7328b3e88e8e78fa29e402)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** Intel(R) Xeon(R) Platinum 8370C CPU @ 2.80GHz, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 31.55K | ± 19.84 | ops/s | **fastest** |
| prometheusNoLabelsInc | 29.99K | ± 1.20K | ops/s | 1.1x slower |
| codahaleIncNoLabels | 28.75K | ± 875.14 | ops/s | 1.1x slower |
| prometheusAdd | 28.42K | ± 56.37 | ops/s | 1.1x slower |
| simpleclientInc | 6.84K | ± 211.32 | ops/s | 4.6x slower |
| simpleclientNoLabelsInc | 6.60K | ± 28.05 | ops/s | 4.8x slower |
| simpleclientAdd | 6.18K | ± 77.74 | ops/s | 5.1x slower |
| openTelemetryInc | 2.65K | ± 203.58 | ops/s | 12x slower |
| openTelemetryIncNoLabels | 2.36K | ± 127.48 | ops/s | 13x slower |
| openTelemetryAdd | 2.13K | ± 211.22 | ops/s | 15x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| simpleclient | 4.37K | ± 158.34 | ops/s | **fastest** |
| prometheusClassic | 3.25K | ± 346.43 | ops/s | 1.3x slower |
| prometheusNative | 2.15K | ± 298.52 | ops/s | 2.0x slower |
| openTelemetryClassic | 618.85 | ± 26.97 | ops/s | 7.1x slower |
| openTelemetryExponential | 448.71 | ± 17.31 | ops/s | 9.7x slower |

### HistogramTextFormatBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 18.27K | ± 139.56 | ops/s | **fastest** |
| openMetricsWriteToNull | 18.25K | ± 114.68 | ops/s | 1.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 322.94K | ± 1.62K | ops/s | **fastest** |
| prometheusWriteToByteArray | 321.17K | ± 1.76K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 300.96K | ± 2.51K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 299.76K | ± 2.30K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      28747.468    ± 875.139  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       2126.579    ± 211.222  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       2648.779    ± 203.580  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       2362.039    ± 127.476  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      28422.698     ± 56.370  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      31553.145     ± 19.843  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      29990.064   ± 1196.456  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6179.215     ± 77.740  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6837.271    ± 211.316  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6604.377     ± 28.045  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        618.851     ± 26.968  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        448.709     ± 17.308  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       3254.839    ± 346.432  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2150.395    ± 298.518  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4367.080    ± 158.335  ops/s
HistogramTextFormatBenchmark.openMetricsWriteToNull  thrpt   15      18246.041    ± 114.681  ops/s
HistogramTextFormatBenchmark.prometheusWriteToNull  thrpt   15      18271.953    ± 139.561  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     299758.378   ± 2300.531  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     300964.908   ± 2506.558  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     321170.275   ± 1755.184  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     322940.404   ± 1622.286  ops/s
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
