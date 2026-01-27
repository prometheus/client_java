# Prometheus Java Client Benchmarks

## Run Information

- **Date:** 2026-01-27T04:10:02Z
- **Commit:** [`d32fd12`](https://github.com/prometheus/client_java/commit/d32fd1260440996d672c2650d43af3b535a28c32)
- **JDK:** 25.0.2 (OpenJDK 64-Bit Server VM)
- **Benchmark config:** 3 fork(s), 3 warmup, 5 measurement, 4 threads
- **Hardware:** AMD EPYC 7763 64-Core Processor, 4 cores, 16 GB RAM
- **OS:** Linux 6.11.0-1018-azure

## Results

### CounterBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusInc | 65.86K | ± 1.72K | ops/s | **fastest** |
| prometheusNoLabelsInc | 56.56K | ± 1.47K | ops/s | 1.2x slower |
| codahaleIncNoLabels | 47.94K | ± 634.99 | ops/s | 1.4x slower |
| prometheusAdd | 44.05K | ± 11.46K | ops/s | 1.5x slower |
| simpleclientInc | 6.63K | ± 139.72 | ops/s | 9.9x slower |
| simpleclientNoLabelsInc | 6.61K | ± 100.44 | ops/s | 10.0x slower |
| simpleclientAdd | 6.27K | ± 270.19 | ops/s | 11x slower |
| openTelemetryInc | 1.35K | ± 118.86 | ops/s | 49x slower |
| openTelemetryAdd | 1.25K | ± 11.98 | ops/s | 53x slower |
| openTelemetryIncNoLabels | 1.20K | ± 22.48 | ops/s | 55x slower |

### HistogramBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusClassic | 5.30K | ± 89.93 | ops/s | **fastest** |
| simpleclient | 4.53K | ± 37.71 | ops/s | 1.2x slower |
| prometheusNative | 3.13K | ± 180.56 | ops/s | 1.7x slower |
| openTelemetryClassic | 674.05 | ± 23.39 | ops/s | 7.9x slower |
| openTelemetryExponential | 527.67 | ± 33.31 | ops/s | 10x slower |

### TextFormatUtilBenchmark

| Benchmark | Score | Error | Units | |
|:----------|------:|------:|:------|:---|
| prometheusWriteToNull | 548.76K | ± 5.76K | ops/s | **fastest** |
| prometheusWriteToByteArray | 544.79K | ± 2.83K | ops/s | 1.0x slower |
| openMetricsWriteToByteArray | 522.02K | ± 6.36K | ops/s | 1.1x slower |
| openMetricsWriteToNull | 520.44K | ± 4.70K | ops/s | 1.1x slower |

### Raw Results

```
Benchmark                                            Mode  Cnt          Score        Error  Units
CounterBenchmark.codahaleIncNoLabels                thrpt   15      47936.349    ± 634.989  ops/s
CounterBenchmark.openTelemetryAdd                   thrpt   15       1245.442     ± 11.982  ops/s
CounterBenchmark.openTelemetryInc                   thrpt   15       1348.490    ± 118.862  ops/s
CounterBenchmark.openTelemetryIncNoLabels           thrpt   15       1203.795     ± 22.483  ops/s
CounterBenchmark.prometheusAdd                      thrpt   15      44053.179  ± 11463.508  ops/s
CounterBenchmark.prometheusInc                      thrpt   15      65864.382   ± 1724.469  ops/s
CounterBenchmark.prometheusNoLabelsInc              thrpt   15      56561.624   ± 1468.674  ops/s
CounterBenchmark.simpleclientAdd                    thrpt   15       6272.735    ± 270.190  ops/s
CounterBenchmark.simpleclientInc                    thrpt   15       6630.295    ± 139.721  ops/s
CounterBenchmark.simpleclientNoLabelsInc            thrpt   15       6612.285    ± 100.445  ops/s
HistogramBenchmark.openTelemetryClassic             thrpt   15        674.047     ± 23.394  ops/s
HistogramBenchmark.openTelemetryExponential         thrpt   15        527.667     ± 33.314  ops/s
HistogramBenchmark.prometheusClassic                thrpt   15       5303.210     ± 89.927  ops/s
HistogramBenchmark.prometheusNative                 thrpt   15       3132.765    ± 180.556  ops/s
HistogramBenchmark.simpleclient                     thrpt   15       4534.142     ± 37.710  ops/s
TextFormatUtilBenchmark.openMetricsWriteToByteArray  thrpt   15     522019.905   ± 6358.823  ops/s
TextFormatUtilBenchmark.openMetricsWriteToNull      thrpt   15     520443.373   ± 4703.404  ops/s
TextFormatUtilBenchmark.prometheusWriteToByteArray  thrpt   15     544788.931   ± 2832.382  ops/s
TextFormatUtilBenchmark.prometheusWriteToNull       thrpt   15     548755.681   ± 5758.476  ops/s
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
