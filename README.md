# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-04-26T04:33:19Z
- **Commit:** [`dec8e5b`](https://github.com/prometheus/client_java/commit/dec8e5b15a1c48c54be6b81517f2cb334bc0ee60)
- **JDK:** 25.0.3 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.17.0-1010-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.47K | ± 1.55K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.97K | ± 535.91 | ops/s | 1.1x slower |
| prometheusAdd | 51.33K | ± 544.47 | ops/s | 1.3x slower |
| codahaleIncNoLabels | 49.51K | ± 568.23 | ops/s | 1.3x slower |
| simpleclientInc | 6.63K | ± 120.69 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.28K | ± 114.70 | ops/s | 10x slower |
| simpleclientAdd | 6.24K | ± 176.91 | ops/s | 10x slower |
| openTelemetryInc | 3.70K | ± 381.29 | ops/s | 18x slower |
| openTelemetryAdd | 3.20K | ± 270.92 | ops/s | 20x slower |
| openTelemetryIncNoLabels | 3.17K | ± 97.42 | ops/s | 21x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.45K | ± 1.21K | ops/s | **fastest** |
| simpleclient | 4.41K | ± 10.33 | ops/s | 1.2x slower |
| prometheusNative | 2.84K | ± 383.12 | ops/s | 1.9x slower |
| openTelemetryClassic | 722.17 | ± 12.72 | ops/s | 7.5x slower |
| openTelemetryExponential | 607.95 | ± 63.26 | ops/s | 9.0x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 481.51K | ± 1.71K | ops/s | **fastest** |
| prometheusWriteToByteArray | 478.46K | ± 2.94K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 459.84K | ± 7.95K | ops/s | 1.0x slower |
| openMetricsWriteToNull | 456.83K | ± 3.55K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      49509.152    ± 568.232  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       3203.655    ± 270.923  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       3696.930    ± 381.295  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       3170.427     ± 97.423  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      51328.798    ± 544.472  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65465.936   ± 1548.003  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56970.423    ± 535.906  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6235.212    ± 176.911  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6627.632    ± 120.693  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6276.771    ± 114.704  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        722.171     ± 12.718  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        607.955     ± 63.255  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5445.820   ± 1210.174  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       2841.002    ± 383.125  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4408.716     ± 10.328  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     459840.198   ± 7952.017  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     456827.893   ± 3548.879  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     478460.917   ± 2942.733  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     481512.320   ± 1705.228  ops/s
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
