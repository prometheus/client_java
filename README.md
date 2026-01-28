# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-01-28T04:07:38Z
- **Commit:** [`672672f`](https://github.com/prometheus/client_java/commit/672672fa6177b3aa87a4a91201e6aae274b88a14)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.20K | ± 1.22K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.84K | ± 338.46 | ops/s | 1.1x slower |
| prometheusAdd | 51.64K | ± 244.29 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.60K | ± 1.30K | ops/s | 1.3x slower |
| simpleclientInc | 6.77K | ± 34.78 | ops/s | 9.6x slower |
| simpleclientNoLabelsInc | 6.70K | ± 18.16 | ops/s | 9.7x slower |
| simpleclientAdd | 6.33K | ± 183.64 | ops/s | 10x slower |
| openTelemetryInc | 1.33K | ± 220.09 | ops/s | 49x slower |
| openTelemetryIncNoLabels | 1.32K | ± 52.43 | ops/s | 49x slower |
| openTelemetryAdd | 1.25K | ± 34.62 | ops/s | 52x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.43K | ± 441.75 | ops/s | **fastest** |
| simpleclient | 4.55K | ± 47.83 | ops/s | 1.2x slower |
| prometheusNative | 3.03K | ± 97.11 | ops/s | 1.8x slower |
| openTelemetryClassic | 660.44 | ± 41.17 | ops/s | 8.2x slower |
| openTelemetryExponential | 553.47 | ± 27.60 | ops/s | 9.8x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 541.82K | ± 4.31K | ops/s | **fastest** |
| prometheusWriteToByteArray | 527.12K | ± 8.66K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 511.99K | ± 6.41K | ops/s | 1.1x slower |
| openMetricsWriteToByteArray | 506.09K | ± 3.10K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49603.920   ± 1298.168  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1253.709     ± 34.624  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1330.012    ± 220.090  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1322.783     ± 52.426  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51637.705    ± 244.291  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65203.888   ± 1215.691  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56842.198    ± 338.458  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6327.824    ± 183.645  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6770.680     ± 34.778  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6699.730     ± 18.161  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        660.442     ± 41.171  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        553.469     ± 27.601  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5427.474    ± 441.752  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3025.570     ± 97.108  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4547.828     ± 47.830  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     506086.934   ± 3096.742  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     511988.797   ± 6406.599  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     527122.201   ± 8660.305  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     541815.324   ± 4312.632  ops/s
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
